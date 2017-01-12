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

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.Swagger;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;

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
import java.util.*;

public class AnnotationProcessor {

    private static final Log log = LogFactory.getLog(AnnotationProcessor.class);

    private static final String PACKAGE_ORG_APACHE = "org.apache";
    private static final String PACKAGE_ORG_CODEHAUS = "org.codehaus";
    private static final String PACKAGE_ORG_SPRINGFRAMEWORK = "org.springframework";
    private static final String WILD_CARD = "/*";
    private static final String URL_SEPARATOR = "/";

    private static final String STRING_ARR = "string_arr";
    private static final String STRING = "string";

    private static final String SWAGGER_ANNOTATIONS_PROPERTIES = "properties";
    private static final String SWAGGER_ANNOTATIONS_EXTENSIONS = "extensions";
    private static final String SWAGGER_ANNOTATIONS_PROPERTIES_VALUE = "value";
    private static final String SWAGGER_ANNOTATIONS_PROPERTIES_NAME = "name";
    private static final String SWAGGER_ANNOTATIONS_PROPERTIES_DESCRIPTION = "description";
    private static final String SWAGGER_ANNOTATIONS_PROPERTIES_KEY = "key";
    private static final String SWAGGER_ANNOTATIONS_PROPERTIES_PERMISSIONS = "permissions";
    private static final String ANNOTATIONS_SCOPES = "scopes";
    private static final String ANNOTATIONS_SCOPE = "scope";
    private static final String DEFAULT_PERM_NAME = "default";
    private static final String DEFAULT_PERM = "/device-mgt";
    private static final String PERMISSION_PREFIX = "/permission/admin";

    private StandardContext context;
    private Method[] pathClazzMethods;
    private Class<Path> pathClazz;
    private ClassLoader classLoader;
    private ServletContext servletContext;
    private Swagger swagger;
    private Class<SwaggerDefinition> apiClazz;
    private Class<Consumes> consumesClass;
    private Class<Produces> producesClass;
    private Class<io.swagger.annotations.ApiOperation> apiOperation;
    private Class<io.swagger.annotations.Authorization> authorizationClass;
    private Class<io.swagger.annotations.AuthorizationScope> authorizationScopeClass;
    private Class<io.swagger.annotations.Extension> extensionClass;
    private Class<io.swagger.annotations.ExtensionProperty> extensionPropertyClass;
    private Class<org.wso2.carbon.apimgt.annotations.api.Scope> scopeClass;
    private Class<org.wso2.carbon.apimgt.annotations.api.Scopes> scopesClass;
    private Map<String, Scope> apiScopes;


    public AnnotationProcessor(final StandardContext context) {
        servletContext = context.getServletContext();
        classLoader = servletContext.getClassLoader();
        try {
            pathClazz = (Class<Path>) classLoader.loadClass(Path.class.getName());
            consumesClass = (Class<Consumes>) classLoader.loadClass(Consumes.class.getName());
            producesClass = (Class<Produces>) classLoader.loadClass(Produces.class.getName());
            apiClazz= (Class<SwaggerDefinition>)classLoader.loadClass((SwaggerDefinition.class.getName()));
            apiOperation = (Class<io.swagger.annotations.ApiOperation>)classLoader
                    .loadClass((io.swagger.annotations.ApiOperation.class.getName()));
            authorizationClass = (Class<io.swagger.annotations.Authorization>)classLoader
                    .loadClass((io.swagger.annotations.Authorization.class.getName()));
            authorizationScopeClass = (Class<io.swagger.annotations.AuthorizationScope>)classLoader
                    .loadClass((io.swagger.annotations.AuthorizationScope.class.getName()));
            extensionClass = (Class<io.swagger.annotations.Extension>)classLoader
                    .loadClass((io.swagger.annotations.Extension.class.getName()));
            extensionPropertyClass = (Class<io.swagger.annotations.ExtensionProperty>)classLoader
                    .loadClass(io.swagger.annotations.ExtensionProperty.class.getName());
            scopeClass = (Class<org.wso2.carbon.apimgt.annotations.api.Scope>) classLoader
                    .loadClass(org.wso2.carbon.apimgt.annotations.api.Scope.class.getName());
            scopesClass = (Class<org.wso2.carbon.apimgt.annotations.api.Scopes>) classLoader
                    .loadClass(org.wso2.carbon.apimgt.annotations.api.Scopes.class.getName());

        } catch (ClassNotFoundException e) {
            log.error("An error has occurred while loading classes ", e);
        }
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
    public List<Permission> extractPermissions(Set<String> entityClasses) {

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
                                    Annotation apiAnno = clazz.getAnnotation(apiClazz);
                                    Annotation scopesAnno = clazz.getAnnotation(scopesClass);
                                    if (scopesAnno != null) {
                                        apiScopes = processAPIScopes(scopesAnno);
                                    }
                                    List<Permission> resourceList;
                                    if (apiAnno != null) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Application Context root = " + servletContext.getContextPath());
                                        }
                                        try {
                                            String rootContext = servletContext.getContextPath();
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
                                    log.error("Error when passing the api annotation for device type apis.", e);
                                } catch (Throwable e) {
                                    log.error("Error when passing the scopes annotation for device type apis.", e);
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
                            equals(io.swagger.annotations.ApiOperation.class.getName())) {
                        this.setPermission(annotations[i], permission);
                    }
                }
                if (permission.getName() == null || permission.getPath() == null) {
                    log.warn("Permission not assigned to the resource url - " + permission.getMethod() + ":"
                                     + permission.getUrl());
                } else {
                    permissions.add(permission);
                }
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
    private static URL findWebInfClassesPath(ServletContext servletContext) {
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

    private void setPermission(Annotation currentMethod, Permission permission) throws Throwable {
        InvocationHandler methodHandler = Proxy.getInvocationHandler(currentMethod);
        Annotation[] extensions = (Annotation[]) methodHandler.invoke(currentMethod,
                apiOperation.getMethod(SWAGGER_ANNOTATIONS_EXTENSIONS, null), null);
        if (extensions != null) {
            methodHandler = Proxy.getInvocationHandler(extensions[0]);
            Annotation[] properties = (Annotation[]) methodHandler.invoke(extensions[0], extensionClass
                    .getMethod(SWAGGER_ANNOTATIONS_PROPERTIES, null), null);
            Scope scope;
            String scopeKey;
            String propertyName;
            for (Annotation property : properties) {
                methodHandler = Proxy.getInvocationHandler(property);
                propertyName = (String) methodHandler.invoke(property, extensionPropertyClass
                        .getMethod(SWAGGER_ANNOTATIONS_PROPERTIES_NAME, null), null);
                if (ANNOTATIONS_SCOPE.equals(propertyName)) {
                     scopeKey = (String) methodHandler.invoke(property, extensionPropertyClass
                            .getMethod(SWAGGER_ANNOTATIONS_PROPERTIES_VALUE, null), null);
                    if (!scopeKey.isEmpty()) {
                        scope = apiScopes.get(scopeKey);
                        if (scope != null) {
                            permission.setName(scope.getName());
                            //TODO: currently permission tree supports only adding one permission per API point.
                            permission.setPath(scope.getRoles().split(" ")[0]);
                        } else {
                            log.warn("No Scope mapping is done for scope key: " + scopeKey);
                            permission.setName(DEFAULT_PERM_NAME);
                            permission.setPath(DEFAULT_PERM);
                        }
                    }
                }
            }
        }
    }

    private Map<String,Scope> processAPIScopes(Annotation annotation) throws Throwable {
        Map<String, Scope> scopes = new HashMap<>();

        InvocationHandler methodHandler = Proxy.getInvocationHandler(annotation);
        Annotation[] annotatedScopes = (Annotation[]) methodHandler.invoke(annotation, scopesClass
                .getMethod(ANNOTATIONS_SCOPES, null), null);

        Scope scope;
        String permissions[];
        StringBuilder aggregatedPermissions;
        for(int i=0; i<annotatedScopes.length; i++){
            aggregatedPermissions = new StringBuilder();
            methodHandler = Proxy.getInvocationHandler(annotatedScopes[i]);
            scope = new Scope();
            scope.setName(invokeMethod(scopeClass
                    .getMethod(SWAGGER_ANNOTATIONS_PROPERTIES_NAME), annotatedScopes[i], STRING));
            scope.setDescription(invokeMethod(scopeClass
                    .getMethod(SWAGGER_ANNOTATIONS_PROPERTIES_DESCRIPTION), annotatedScopes[i], STRING));
            scope.setKey(invokeMethod(scopeClass
                    .getMethod(SWAGGER_ANNOTATIONS_PROPERTIES_KEY), annotatedScopes[i], STRING));
            permissions = (String[])methodHandler.invoke(annotatedScopes[i], scopeClass
                    .getMethod(SWAGGER_ANNOTATIONS_PROPERTIES_PERMISSIONS, null),null);
            for (String permission : permissions) {
                aggregatedPermissions.append(PERMISSION_PREFIX);
                aggregatedPermissions.append(permission);
                aggregatedPermissions.append(" ");
            }
            scope.setRoles(aggregatedPermissions.toString());
            scopes.put(scope.getKey(), scope);
        }
        return scopes;
    }
}
