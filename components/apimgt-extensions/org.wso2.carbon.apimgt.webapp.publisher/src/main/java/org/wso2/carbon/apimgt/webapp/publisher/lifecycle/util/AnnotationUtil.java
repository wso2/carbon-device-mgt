/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util;

import org.apache.catalina.core.StandardContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scannotation.AnnotationDB;
import org.scannotation.WarUrlFinder;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResource;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceConfiguration;
import org.wso2.carbon.device.mgt.common.Feature;

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
    public static final String DIR_WEB_INF_LIB = "/WEB-INF/lib";
    public static final String STRING_ARR = "string_arr";
    public static final String STRING = "string";

    private StandardContext context;
    private Method[] pathClazzMethods;
    private Class<Path> pathClazz;
    private Class<org.wso2.carbon.apimgt.annotations.device.feature.Feature> featureClazz;
    private ClassLoader classLoader;
    private ServletContext servletContext;


    public AnnotationUtil(final StandardContext context) {
        this.context = context;
        servletContext = context.getServletContext();
        classLoader = servletContext.getClassLoader();
    }

    /**
     * Scan the context for classes with annotations
     * @return
     * @throws IOException
     */
    public Set<String> scanStandardContext(String className) throws IOException {
        Set<String> entityClasses = null;

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
     * @param entityClasses
     * @return
     */
    public List<APIResourceConfiguration> extractAPIInfo(Set<String> entityClasses)
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
                                    Class<Path> apiClazz = (Class<Path>)
                                            classLoader.loadClass(org.wso2.carbon.apimgt.annotations.api.API.class.getName());
                                    Annotation apiAnno = clazz.getAnnotation(apiClazz);

                                    List<APIResource> resourceList = null;
                                    apiResourceConfig = new APIResourceConfiguration();

                                    if (apiAnno != null) {

                                        Method[] apiClazzMethods = apiClazz.getMethods();

                                        if (log.isDebugEnabled()) {
                                            log.debug("Application Context root = " + servletContext.getContextPath());
                                        }

                                        try {
                                            apiResourceConfig.setName(invokeMethod(apiClazzMethods[0], apiAnno, STRING));
                                            apiResourceConfig.setVersion(invokeMethod(apiClazzMethods[2], apiAnno, STRING));
                                            apiResourceConfig.setContext(invokeMethod(apiClazzMethods[1], apiAnno, STRING));

                                            String rootContext = "";

                                            pathClazz = (Class<Path>) classLoader.loadClass(Path.class.getName());
                                            pathClazzMethods = pathClazz.getMethods();

                                            Annotation rootContectAnno = clazz.getAnnotation(pathClazz);
                                            if (rootContectAnno != null) {
                                                rootContext = invokeMethod(pathClazzMethods[0], rootContectAnno, STRING);
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
                                    e.printStackTrace();
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
     * Method identifies the URL templates and context by reading the annotations of a class
     * @param entityClasses
     * @return
     */
    public Map<String,List<Feature>> extractFeatures(Set<String> entityClasses)
            throws ClassNotFoundException {

        Map<String,List<Feature>> features = null;

        if (entityClasses != null && !entityClasses.isEmpty()) {
            features = new HashMap<String,List<Feature>>();
            for (final String className : entityClasses) {

                final Map<String,List<Feature>> featureMap =
                        AccessController.doPrivileged(new PrivilegedAction<Map<String,List<Feature>>>() {
                            public Map<String,List<Feature>> run() {
                                Class<?> clazz = null;
                                Map<String,List<Feature>> featureMap = new HashMap<String, List<Feature>>();
                                try {
                                    clazz = classLoader.loadClass(className);

                                    Class<org.wso2.carbon.apimgt.annotations.device.DeviceType> deviceTypeClazz =
                                            (Class<org.wso2.carbon.apimgt.annotations.device.DeviceType>)
                                            classLoader.loadClass(org.wso2.carbon.apimgt.annotations.device.DeviceType.class.getName());
                                    Annotation deviceTypeAnno = clazz.getAnnotation(deviceTypeClazz);

                                    if(deviceTypeAnno!=null){
                                        Method[] deviceTypeMethod = deviceTypeClazz.getMethods();
                                        String deviceType = invokeMethod(deviceTypeMethod[0], deviceTypeAnno, STRING);


                                        featureClazz = (Class<org.wso2.carbon.apimgt.annotations.device.feature.Feature>)
                                                classLoader.loadClass(org.wso2.carbon.apimgt.annotations.device.feature.Feature.class.getName());

                                        List<Feature> featureList = getFeatures(clazz.getDeclaredMethods());

                                        featureMap.put(deviceType,featureList);
                                    }

                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                                return featureMap;
                            }
                        });

                features.putAll(featureMap);
            }
        }
        return features;
    }


    private List<Feature> getFeatures(Method[] annotatedMethods) throws Throwable {
        List<Feature> featureList = new ArrayList<Feature>();

        Class<FormParam> formParamClazz = (Class<FormParam>) classLoader.loadClass(FormParam.class.getName());
        Method[] formMethods = formParamClazz.getMethods();

        for (Method method : annotatedMethods) {
            Annotation methodAnnotation = method.getAnnotation(featureClazz);

            if (methodAnnotation != null) {

                Annotation[] annotations = method.getDeclaredAnnotations();
                for(int i=0; i<annotations.length; i++){
                    if(annotations[i].annotationType().getName().equals(org.wso2.carbon.apimgt.annotations.device.feature.Feature.class.getName())){
                        Feature feature = new Feature();
                        Method[] featureAnnoMethods = featureClazz.getMethods();
                        Annotation featureAnno = method.getAnnotation(featureClazz);

                        feature.setCode(invokeMethod(featureAnnoMethods[2], featureAnno, STRING));
                        feature.setName(invokeMethod(featureAnnoMethods[1], featureAnno, STRING));
                        feature.setDescription(invokeMethod(featureAnnoMethods[0], featureAnno, STRING));

                        List<Feature.MetadataEntry> metaInfoList = new ArrayList<Feature.MetadataEntry>();

                        Annotation[][] paramAnnotations = method.getParameterAnnotations();

                        for(int j = 0; j < paramAnnotations.length; j++){
                            for(Annotation anno : paramAnnotations[j]){
                                if(anno.annotationType().getName().equals(FormParam.class.getName())){
                                    Feature.MetadataEntry metadataEntry = new Feature.MetadataEntry();
                                    metadataEntry.setId(j);
                                    metadataEntry.setValue(invokeMethod(formMethods[0], anno, STRING));
                                    metaInfoList.add(metadataEntry);
                                }
                            }
                        }
                        feature.setMetadataEntries(metaInfoList);
                        featureList.add(feature);
                    }
                }
            }
        }
        return featureList;
    }


    private List<APIResource> getApiResources(String rootContext, Method[] annotatedMethods) throws Throwable {
        List<APIResource> resourceList;
        resourceList = new ArrayList<APIResource>();
        for (Method method : annotatedMethods) {
            Annotation methodContextAnno = method.getAnnotation(pathClazz);
            if (methodContextAnno != null) {
                String subCtx = invokeMethod(pathClazzMethods[0], methodContextAnno, STRING);
                APIResource resource = new APIResource();
                resource.setUriTemplate(subCtx);

                String serverIP = System.getProperty(SERVER_HOST);
                String httpServerPort = System.getProperty(HTTP_PORT);

                resource.setUri(PROTOCOL_HTTP + "://" + serverIP + ":" + httpServerPort + makeContextURLReady(rootContext) + makeContextURLReady(subCtx));
                resource.setAuthType(AUTH_TYPE);

                Annotation[] annotations = method.getDeclaredAnnotations();
                for(int i=0; i<annotations.length; i++){

                    if(annotations[i].annotationType().getName().equals(GET.class.getName())){
                        resource.setHttpVerb(HttpMethod.GET);
                    }
                    if(annotations[i].annotationType().getName().equals(POST.class.getName())){
                        resource.setHttpVerb(HttpMethod.POST);
                    }
                    if(annotations[i].annotationType().getName().equals(OPTIONS.class.getName())){
                        resource.setHttpVerb(HttpMethod.OPTIONS);
                    }
                    if(annotations[i].annotationType().getName().equals(DELETE.class.getName())){
                        resource.setHttpVerb(HttpMethod.DELETE);
                    }
                    if(annotations[i].annotationType().getName().equals(PUT.class.getName())){
                        resource.setHttpVerb(HttpMethod.PUT);
                    }
                    if(annotations[i].annotationType().getName().equals(Consumes.class.getName())){
                        Class<Consumes> consumesClass = (Class<Consumes>) classLoader.loadClass(Consumes.class.getName());
                        Method[] consumesClassMethods = consumesClass.getMethods();
                        Annotation consumesAnno = method.getAnnotation(consumesClass);
                        resource.setConsumes(invokeMethod(consumesClassMethods[0], consumesAnno, STRING_ARR));
                    }
                    if(annotations[i].annotationType().getName().equals(Produces.class.getName())){
                        Class<Produces> producesClass = (Class<Produces>) classLoader.loadClass(Produces.class.getName());
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

    private String makeContextURLReady(String context){
        if(context != null && !context.equalsIgnoreCase("")){
            if(context.startsWith("/")){
                return context;
            }else{
                return "/"+context;
            }
        }
        return "";
    }

    /**
     * When an annotation and method is passed, this method invokes that executes said method against the annotation
     * @param method
     * @param annotation
     * @param returnType
     * @return
     * @throws Throwable
     */
    private String invokeMethod(Method method, Annotation annotation, String returnType) throws Throwable {
        InvocationHandler methodHandler = Proxy.getInvocationHandler(annotation);
        switch (returnType){
            case STRING:
                return (String) methodHandler.invoke(annotation, method, null);
            case STRING_ARR:
                return ((String[])methodHandler.invoke(annotation, method, null))[0];
            default:
                return null;
        }
    }
}
