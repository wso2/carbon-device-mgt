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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scannotation.AnnotationDB;
import org.scannotation.WarUrlFinder;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResource;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceConfiguration;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

public class AnnotationUtil {

    private static final Log log = LogFactory.getLog(AnnotationUtil.class);

    private static final String PACKAGE_ORG_APACHE = "org.apache";
    private static final String PACKAGE_ORG_CODEHAUS = "org.codehaus";
    private static final String PACKAGE_ORG_SPRINGFRAMEWORK = "org.springframework";

    private static final String AUTH_TYPE = "Any";
    private static final String PROTOCOL_HTTP = "http";
    private static final String SERVER_HOST = "carbon.local.ip";
    private static final String HTTP_PORT = "httpPort";
    private static final String STRING_ARR = "string_arr";
    private static final String STRING = "string";

    private StandardContext context;
    private Method[] pathClazzMethods;
    private Class<Path> pathClazz;
    Class<API> apiClazz;
    private ClassLoader classLoader;
    private ServletContext servletContext;


    public AnnotationUtil(final StandardContext context) {
        this.context = context;
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
        AnnotationDB db = new AnnotationDB();
        db.addIgnoredPackages(PACKAGE_ORG_APACHE);
        db.addIgnoredPackages(PACKAGE_ORG_CODEHAUS);
        db.addIgnoredPackages(PACKAGE_ORG_SPRINGFRAMEWORK);

        URL[] libPath = WarUrlFinder.findWebInfLibClasspaths(servletContext);
        URL classPath = WarUrlFinder.findWebInfClassesPath(servletContext);
        URL[] urls = (URL[]) ArrayUtils.add(libPath, libPath.length, classPath);

        db.scanArchives(urls);

        //Returns a list of classes with given Annotation
        return db.getAnnotationIndex().get(className);
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
                                            // All the apis should map to same root "/"
                                            String rootContext = servletContext.getContextPath();
                                            pathClazz = (Class<Path>) classLoader.loadClass(Path.class.getName());
                                            pathClazzMethods = pathClazz.getMethods();

                                            Annotation rootContectAnno = clazz.getAnnotation(pathClazz);
                                            String subContext = "";
                                            if (rootContectAnno != null) {
                                                subContext = invokeMethod(pathClazzMethods[0], rootContectAnno, STRING);
                                                if (subContext != null && !subContext.isEmpty()) {
                                                    rootContext = rootContext + "/" + subContext;
                                                } else {
                                                    subContext = "";
                                                }
                                                if (log.isDebugEnabled()) {
                                                    log.debug("API Root  Context = " + rootContext);
                                                }
                                            }

                                            Method[] annotatedMethods = clazz.getDeclaredMethods();
                                            resourceList = getApiResources(rootContext, subContext, annotatedMethods);
                                            apiResourceConfig.setResources(resourceList);
                                        } catch (Throwable throwable) {
                                            log.error("Error encountered while scanning for annotations", throwable);
                                        }
                                    }
                                } catch (ClassNotFoundException e) {
                                    log.error("Error when passing the api annotation for device type apis.");
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
     * @param resourceRootContext
     * @param apiRootContext
     * @param annotatedMethods
     * @return
     * @throws Throwable
     */
    private List<APIResource> getApiResources(String resourceRootContext, String apiRootContext,
                                              Method[] annotatedMethods) throws Throwable {
        List<APIResource> resourceList;
        resourceList = new ArrayList<APIResource>();
        for (Method method : annotatedMethods) {
            Annotation methodContextAnno = method.getAnnotation(pathClazz);
            if (methodContextAnno != null) {
                String subCtx = invokeMethod(pathClazzMethods[0], methodContextAnno, STRING);
                APIResource resource = new APIResource();
                resource.setUriTemplate(makeContextURLReady(apiRootContext + subCtx));

                String serverIP = System.getProperty(SERVER_HOST);
                String httpServerPort = System.getProperty(HTTP_PORT);

                resource.setUri(PROTOCOL_HTTP + "://" + serverIP + ":" + httpServerPort + makeContextURLReady(
                        resourceRootContext) + makeContextURLReady(subCtx));
                resource.setAuthType(AUTH_TYPE);

                Annotation[] annotations = method.getDeclaredAnnotations();
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
                }
                resourceList.add(resource);
            }
        }
        return resourceList;
    }

    /**
     * Read Method annotations indicating HTTP Methods
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

    /**
     * Append '/' to the context and make it URL ready
     * @param context
     * @return
     */
    private String makeContextURLReady(String context) {
        if (context != null && !context.equalsIgnoreCase("")) {
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
}
