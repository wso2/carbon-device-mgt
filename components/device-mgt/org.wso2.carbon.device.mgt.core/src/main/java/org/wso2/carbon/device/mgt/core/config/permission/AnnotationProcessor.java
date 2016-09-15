/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.config.permission;

import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class AnnotationProcessor {

    private static final Log log = LogFactory.getLog(AnnotationProcessor.class);

    private static final String PACKAGE_ORG_APACHE = "org.apache";
    private static final String PACKAGE_ORG_CODEHAUS = "org.codehaus";
    private static final String PACKAGE_ORG_SPRINGFRAMEWORK = "org.springframework";
    private static final String WILD_CARD = "/*";
    private static final String URL_SEPARATOR = "/";

    private static final String STRING_ARR = "string_arr";
    private static final String STRING = "string";

    private Method[] pathClazzMethods;
    private Class<Path> pathClazz;
    Class<API> apiClazz;
    private ClassLoader classLoader;
    private ServletContext servletContext;


    public AnnotationProcessor(final StandardContext context) {
        servletContext = context.getServletContext();
        classLoader = servletContext.getClassLoader();
    }

    /**
     * Scan the context for classes with annotations
     *
     * @return
     * @throws IOException
     */
    public Set<String> scanStandardContext(String className) throws IOException {
        ExtendedAnnotationDB db = new ExtendedAnnotationDB();
        db.addIgnoredPackages(PACKAGE_ORG_APACHE);
        db.addIgnoredPackages(PACKAGE_ORG_CODEHAUS);
        db.addIgnoredPackages(PACKAGE_ORG_SPRINGFRAMEWORK);
        URL classPath = findWebInfClassesPath(servletContext);
        db.scanArchives(classPath);

        //Returns a list of classes with given Annotation
        return db.getAnnotationIndex().get(className);
    }

    /**
     * Method identifies the URL templates and context by reading the annotations of a class
     *
     * @param entityClasses
     * @return
     */
    public List<Permission>
    extractPermissions(Set<String> entityClasses) {

        List<Permission> permissions = new ArrayList<>();

        if (entityClasses != null && !entityClasses.isEmpty()) {

            for (final String className : entityClasses) {

                List<Permission> resourcePermissions =
                        AccessController.doPrivileged(new PrivilegedAction<List<org.wso2.carbon.device.mgt.common.permission.mgt.Permission>>() {
                            public List<org.wso2.carbon.device.mgt.common.permission.mgt.Permission> run() {
                                Class<?> clazz;
                                List<Permission> apiPermissions = new ArrayList<>();
                                try {
                                    clazz = classLoader.loadClass(className);

                                    apiClazz = (Class<API>)
                                            classLoader.loadClass(org.wso2.carbon.apimgt.annotations.api.API
                                                    .class.getName());

                                    Annotation apiAnno = clazz.getAnnotation(apiClazz);
                                    List<Permission> resourceList;

                                    if (apiAnno != null) {

                                        if (log.isDebugEnabled()) {
                                            log.debug("Application Context root = " + servletContext.getContextPath());
                                        }

                                        try {
                                            String rootContext = servletContext.getContextPath();
                                            pathClazz = (Class<Path>) classLoader.loadClass(Path.class.getName());
                                            pathClazzMethods = pathClazz.getMethods();

                                            Annotation rootContectAnno = clazz.getAnnotation(pathClazz);
                                            String subContext = "";
                                            if (rootContectAnno != null) {
                                                subContext = invokeMethod(pathClazzMethods[0], rootContectAnno, STRING);
                                                if (subContext != null && !subContext.isEmpty()) {
                                                    if (subContext.trim().startsWith("/")) {
                                                        rootContext = rootContext + subContext;
                                                    } else {
                                                        rootContext = rootContext + "/" + subContext;
                                                    }
                                                }
                                                if (log.isDebugEnabled()) {
                                                    log.debug("API Root  Context = " + rootContext);
                                                }
                                            }

                                            Method[] annotatedMethods = clazz.getDeclaredMethods();
                                            apiPermissions = getApiResources(rootContext, annotatedMethods);
                                        } catch (Throwable throwable) {
                                            log.error("Error encountered while scanning for annotations", throwable);
                                        }
                                    }
                                } catch (ClassNotFoundException e) {
                                    log.error("Error when passing the api annotation for device type apis.");
                                }
                                return apiPermissions;
                            }
                        });
                permissions.addAll(resourcePermissions);
            }
        }
        return permissions;
    }


    /**
     * Get Resources for each API
     *
     * @param resourceRootContext
     * @param annotatedMethods
     * @return
     * @throws Throwable
     */
    private List<Permission> getApiResources(String resourceRootContext, Method[] annotatedMethods) throws Throwable {

        List<Permission> permissions = new ArrayList<>();
        Permission permission;
        String subCtx;
        for (Method method : annotatedMethods) {
            Annotation[] annotations = method.getDeclaredAnnotations();

            if (isHttpMethodAvailable(annotations)) {
                Annotation methodContextAnno = method.getAnnotation(pathClazz);
                if (methodContextAnno != null) {
                    subCtx = invokeMethod(pathClazzMethods[0], methodContextAnno, STRING);
                } else {
                    subCtx = WILD_CARD;
                }
                permission = new Permission();
                // this check is added to avoid url resolving conflict which happens due
                // to adding of '*' notation for dynamic path variables.
                if (WILD_CARD.equals(subCtx)) {
                    subCtx = makeContextURLReady(resourceRootContext);
                } else {
                    subCtx = makeContextURLReady(resourceRootContext) + makeContextURLReady(subCtx);
                }
                permission.setUrl(replaceDynamicPathVariables(subCtx));
                String httpMethod;
                for (int i = 0; i < annotations.length; i++) {
                    httpMethod = getHTTPMethodAnnotation(annotations[i]);
                    if (httpMethod != null) {
                        permission.setMethod(httpMethod);
                    }
                    if (annotations[i].annotationType().getName().
                            equals(org.wso2.carbon.apimgt.annotations.api.Permission.class.getName())) {
                        this.setPermission(method, permission);
                    }
                }
                permissions.add(permission);

            }
        }
        return permissions;
    }

    /**
     * Read Method annotations indicating HTTP Methods
     *
     * @param annotation
     */
    private String getHTTPMethodAnnotation(Annotation annotation) {
        if (annotation.annotationType().getName().equals(GET.class.getName())) {
            return HttpMethod.GET;
        } else if (annotation.annotationType().getName().equals(POST.class.getName())) {
            return HttpMethod.POST;
        } else if (annotation.annotationType().getName().equals(OPTIONS.class.getName())) {
            return HttpMethod.OPTIONS;
        } else if (annotation.annotationType().getName().equals(DELETE.class.getName())) {
            return HttpMethod.DELETE;
        } else if (annotation.annotationType().getName().equals(PUT.class.getName())) {
            return HttpMethod.PUT;
        }
        return null;
    }

    private boolean isHttpMethodAvailable(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().getName().equals(GET.class.getName())) {
                return true;
            } else if (annotation.annotationType().getName().equals(POST.class.getName())) {
                return true;
            } else if (annotation.annotationType().getName().equals(OPTIONS.class.getName())) {
                return true;
            } else if (annotation.annotationType().getName().equals(DELETE.class.getName())) {
                return true;
            } else if (annotation.annotationType().getName().equals(PUT.class.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Append '/' to the context and make it URL ready
     *
     * @param context
     * @return
     */
    private String makeContextURLReady(String context) {
        if (context != null && !context.isEmpty()) {
            if (context.startsWith("/")) {
                return context;
            } else {
                return "/" + context;
            }
        }
        return "";
    }

    /**
     * When an annotation and method is passed, this method invokes that executes said method against the annotation
     *
     * @param method
     * @param annotation
     * @param returnType
     * @return
     * @throws Throwable
     */
    private String invokeMethod(Method method, Annotation annotation, String returnType) throws Throwable {
        InvocationHandler methodHandler = Proxy.getInvocationHandler(annotation);
        switch (returnType) {
            case STRING:
                return (String) methodHandler.invoke(annotation, method, null);
            case STRING_ARR:
                return ((String[]) methodHandler.invoke(annotation, method, null))[0];
            default:
                return null;
        }
    }


    /**
     * Find the URL pointing to "/WEB-INF/classes"  This method may not work in conjunction with IteratorFactory
     * if your servlet container does not extract the /WEB-INF/classes into a real file-based directory
     *
     * @param servletContext
     * @return null if cannot determin /WEB-INF/classes
     */
    public static URL findWebInfClassesPath(ServletContext servletContext) {
        String path = servletContext.getRealPath("/WEB-INF/classes");
        if (path == null) return null;
        File fp = new File(path);
        if (fp.exists() == false) return null;
        try {
            URI uri = fp.toURI();
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String replaceDynamicPathVariables(String path) {
        StringBuilder replacedPath = new StringBuilder();
        StringTokenizer st = new StringTokenizer(path, URL_SEPARATOR);
        String currentToken;
        while (st.hasMoreTokens()) {
            currentToken = st.nextToken();
            if (currentToken.charAt(0) == '{') {
                if (currentToken.charAt(currentToken.length() - 1) == '}') {
                    replacedPath.append(WILD_CARD);
                }
            } else {
                replacedPath.append(URL_SEPARATOR);
                replacedPath.append(currentToken);
            }
        }
        return replacedPath.toString();
    }

    private void setPermission(Method currentMethod, Permission permission) throws Throwable {
        Class<org.wso2.carbon.apimgt.annotations.api.Permission> permissionClass =
                (Class<org.wso2.carbon.apimgt.annotations.api.Permission>) classLoader.
                        loadClass(org.wso2.carbon.apimgt.annotations.api.Permission.class.getName());
        Annotation permissionAnnotation = currentMethod.getAnnotation(permissionClass);
        if (permissionClass != null) {
            Method[] permissionClassMethods = permissionClass.getMethods();
            for (Method method : permissionClassMethods) {
                switch (method.getName()) {
                    case "name":
                        permission.setName(invokeMethod(method, permissionAnnotation, STRING));
                        break;
                    case "permission":
                        permission.setPath(invokeMethod(method, permissionAnnotation, STRING));
                        break;
                }
            }
        }
    }

}
