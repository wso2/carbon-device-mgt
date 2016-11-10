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

package org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util;

import org.apache.catalina.core.StandardContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherUtil;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResource;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceConfiguration;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

public class AnnotationProcessor {

    private static final Log log = LogFactory.getLog(AnnotationProcessor.class);

    private static final String PACKAGE_ORG_APACHE = "org.apache";
    private static final String PACKAGE_ORG_CODEHAUS = "org.codehaus";
    private static final String PACKAGE_ORG_SPRINGFRAMEWORK = "org.springframework";
    private static final String WILD_CARD = "/*";

    private static final String AUTH_TYPE = "Any";
    private static final String STRING_ARR = "string_arr";
    private static final String STRING = "string";
    private static final String API_CLASS_NAME = org.wso2.carbon.apimgt.annotations.api.API.class.getName();

    Class<API> apiClazz;
    private StandardContext context;
    private Method[] pathClazzMethods;
    private Class<Path> pathClazz;
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
        if (API_CLASS_NAME.equals(className)) {
            ExtendedAnnotationDB db = new ExtendedAnnotationDB();
            db.addIgnoredPackages(PACKAGE_ORG_APACHE);
            db.addIgnoredPackages(PACKAGE_ORG_CODEHAUS);
            db.addIgnoredPackages(PACKAGE_ORG_SPRINGFRAMEWORK);
            URL classPath = findWebInfClassesPath(servletContext);
            db.scanArchives(classPath);

            //Returns a list of classes with given Annotation
            return db.getAnnotationIndex().get(className);
        }
        return null;
    }

    /**
     * Method identifies the URL templates and context by reading the annotations of a class
     *
     * @param entityClasses
     * @return
     */
    public List<APIResourceConfiguration> extractAPIInfo(final ServletContext servletContext, Set<String> entityClasses)
            throws ClassNotFoundException {

        List<APIResourceConfiguration> apiResourceConfigs = new ArrayList<APIResourceConfiguration>();

        if (entityClasses != null && !entityClasses.isEmpty()) {
            for (final String className : entityClasses) {

                APIResourceConfiguration resource =
                        AccessController.doPrivileged(new PrivilegedAction<APIResourceConfiguration>() {
                            public APIResourceConfiguration run() {
                                Class<?> clazz = null;
                                APIResourceConfiguration apiResourceConfig = null;
                                try {
                                    clazz = classLoader.loadClass(className);

                                    apiClazz = (Class<API>)
                                            classLoader.loadClass(org.wso2.carbon.apimgt.annotations.api.API
                                                    .class.getName());

                                    Annotation apiAnno = clazz.getAnnotation(apiClazz);
                                    List<APIResource> resourceList;

                                    if (apiAnno != null) {

                                        if (log.isDebugEnabled()) {
                                            log.debug("Application Context root = " + servletContext.getContextPath());
                                        }

                                        try {
                                            apiResourceConfig = processAPIAnnotation(apiAnno);
                                            String rootContext = servletContext.getContextPath();
                                            pathClazz = (Class<Path>) classLoader.loadClass(Path.class.getName());
                                            pathClazzMethods = pathClazz.getMethods();

                                            Annotation rootContectAnno = clazz.getAnnotation(pathClazz);
                                            String subContext;
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
                                            resourceList = getApiResources(rootContext, annotatedMethods);
                                            apiResourceConfig.setResources(resourceList);
                                        } catch (Throwable throwable) {
                                            log.error("Error encountered while scanning for annotations", throwable);
                                        }
                                    }
                                } catch (ClassNotFoundException e) {
                                    log.error("Error when passing the api annotation for device type apis.", e);
                                }
                                return apiResourceConfig;
                            }
                        });
                apiResourceConfigs.add(resource);
            }
        }
        return apiResourceConfigs;
    }

    /**
     * Iterate API annotation and build API Configuration
     *
     * @param apiAnno
     * @return
     * @throws Throwable
     */
    private APIResourceConfiguration processAPIAnnotation(Annotation apiAnno) throws Throwable {
        Method[] apiClazzMethods = apiClazz.getMethods();
        APIResourceConfiguration apiResourceConfig = new APIResourceConfiguration();
        for (int k = 0; k < apiClazzMethods.length; k++) {
            switch (apiClazzMethods[k].getName()) {
                case "name":
                    apiResourceConfig.setName(invokeMethod(apiClazzMethods[k], apiAnno, STRING));
                    break;
                case "version":
                    apiResourceConfig.setVersion(invokeMethod(apiClazzMethods[k], apiAnno, STRING));
                    break;
                case "context":
                    apiResourceConfig.setContext(invokeMethod(apiClazzMethods[k], apiAnno, STRING));
                    break;
                case "tags":
                    apiResourceConfig.setTags(invokeMethod(apiClazzMethods[k], apiAnno));
                    break;
            }
        }
        return apiResourceConfig;
    }


    /**
     * Get Resources for each API
     *
     * @param resourceRootContext
     * @param annotatedMethods
     * @return
     * @throws Throwable
     */
    private List<APIResource> getApiResources(String resourceRootContext, Method[] annotatedMethods) throws Throwable {
        List<APIResource> resourceList = new ArrayList<>();
        String subCtx = null;
        for (Method method : annotatedMethods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            APIResource resource = new APIResource();

            if (isHttpMethodAvailable(annotations)) {
                Annotation methodContextAnno = method.getAnnotation(pathClazz);
                if (methodContextAnno != null) {
                    subCtx = invokeMethod(pathClazzMethods[0], methodContextAnno, STRING);
                } else {
                    subCtx = WILD_CARD;
                }
                resource.setUriTemplate(makeContextURLReady(subCtx));

                resource.setUri(APIPublisherUtil.getServerBaseUrl() + makeContextURLReady(resourceRootContext) +
                        makeContextURLReady(subCtx));
                resource.setAuthType(AUTH_TYPE);

                for (int i = 0; i < annotations.length; i++) {
                    processHTTPMethodAnnotation(resource, annotations[i]);
                    if (annotations[i].annotationType().getName().equals(Consumes.class.getName())) {
                        Class<Consumes> consumesClass = (Class<Consumes>) classLoader.loadClass(
                                Consumes.class.getName());
                        Method[] consumesClassMethods = consumesClass.getMethods();
                        Annotation consumesAnno = method.getAnnotation(consumesClass);
                        resource.setConsumes(invokeMethod(consumesClassMethods[0], consumesAnno, STRING_ARR));
                    }
                    if (annotations[i].annotationType().getName().equals(Produces.class.getName())) {
                        Class<Produces> producesClass = (Class<Produces>) classLoader.loadClass(
                                Produces.class.getName());
                        Method[] producesClassMethods = producesClass.getMethods();
                        Annotation producesAnno = method.getAnnotation(producesClass);
                        resource.setProduces(invokeMethod(producesClassMethods[0], producesAnno, STRING_ARR));
                    }
                    if (annotations[i].annotationType().getName().equals(org.wso2.carbon.apimgt.annotations.api.Scope.class.getName())) {
                        org.wso2.carbon.apimgt.api.model.Scope scope = this.getScope(method);
                        if (scope != null) {
                            resource.setScope(scope);
                        }
                    }
                }
                resourceList.add(resource);
            }
        }
        return resourceList;
    }

    /**
     * Read Method annotations indicating HTTP Methods
     *
     * @param resource
     * @param annotation
     */
    private void processHTTPMethodAnnotation(APIResource resource, Annotation annotation) {
        if (annotation.annotationType().getName().equals(GET.class.getName())) {
            resource.setHttpVerb(HttpMethod.GET);
        }
        if (annotation.annotationType().getName().equals(POST.class.getName())) {
            resource.setHttpVerb(HttpMethod.POST);
        }
        if (annotation.annotationType().getName().equals(OPTIONS.class.getName())) {
            resource.setHttpVerb(HttpMethod.OPTIONS);
        }
        if (annotation.annotationType().getName().equals(DELETE.class.getName())) {
            resource.setHttpVerb(HttpMethod.DELETE);
        }
        if (annotation.annotationType().getName().equals(PUT.class.getName())) {
            resource.setHttpVerb(HttpMethod.PUT);
        }
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
        if (context != null && context.length() > 0) {
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
     * When an annotation and method is passed, this method invokes that executes said method against the annotation
     */
    private String[] invokeMethod(Method method, Annotation annotation) throws Throwable {
        InvocationHandler methodHandler = Proxy.getInvocationHandler(annotation);
        return ((String[]) methodHandler.invoke(annotation, method, null));
    }

    private org.wso2.carbon.apimgt.api.model.Scope getScope(Method currentMethod) throws Throwable {
        Class<org.wso2.carbon.apimgt.annotations.api.Scope> scopeClass =
                (Class<org.wso2.carbon.apimgt.annotations.api.Scope>) classLoader.
                        loadClass(org.wso2.carbon.apimgt.annotations.api.Scope.class.getName());
        Annotation permissionAnnotation = currentMethod.getAnnotation(scopeClass);
        if (scopeClass != null) {
            Method[] permissionClassMethods = scopeClass.getMethods();
            org.wso2.carbon.apimgt.api.model.Scope scope = new org.wso2.carbon.apimgt.api.model.Scope();
            for (Method method : permissionClassMethods) {
                switch (method.getName()) {
                    case "key":
                        scope.setKey(invokeMethod(method, permissionAnnotation, STRING));
                        break;
                    case "name":
                        scope.setName(invokeMethod(method, permissionAnnotation, STRING));
                        break;
                    case "description":
                        scope.setDescription(invokeMethod(method, permissionAnnotation, STRING));
                        break;
                }
            }
            return scope;
        }
        return null;
    }

    /**
     * Find the URL pointing to "/WEB-INF/classes"  This method may not work in conjunction with IteratorFactory
     * if your servlet container does not extract the /WEB-INF/classes into a real file-based directory
     *
     * @param servletContext
     * @return null if cannot determin /WEB-INF/classes
     */
    private static URL findWebInfClassesPath(ServletContext servletContext) {
        String path = servletContext.getRealPath("/WEB-INF/classes");
        if (path == null) return null;
        File fp = new File(path);
        if (fp.exists() == false) return null;
        try
        {
            URI uri = fp.toURI();
            return uri.toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

}
