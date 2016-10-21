/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.extensions.feature.mgt.util;

import org.apache.catalina.core.StandardContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scannotation.AnnotationDB;
import org.scannotation.WarUrlFinder;
import org.wso2.carbon.device.mgt.common.DeviceTypeIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This has the utility function to extract feature information.
 */
public class AnnotationProcessor {

    private static final Log log = LogFactory.getLog(AnnotationProcessor.class);

    private static final String PACKAGE_ORG_APACHE = "org.apache";
    private static final String PACKAGE_ORG_CODEHAUS = "org.codehaus";
    private static final String PACKAGE_ORG_SPRINGFRAMEWORK = "org.springframework";
    private static final String STRING_ARR = "string_arr";
    private static final String STRING = "string";
    private static final String METHOD = "method";
    private Class<org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature>
            featureAnnotationClazz;
    private ClassLoader classLoader;
    private ServletContext servletContext;


    public AnnotationProcessor(final StandardContext context) {
        servletContext = context.getServletContext();
        classLoader = servletContext.getClassLoader();
    }

    /**
     * Scan the context for classes with annotations
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
     */
    public Map<DeviceTypeIdentifier, List<Feature>> extractFeatures(Set<String> entityClasses, final int tenantId,
                                                                    final boolean isSharedWithAllTenants)
            throws ClassNotFoundException {
        Map<DeviceTypeIdentifier, List<Feature>> features = null;
        if (entityClasses != null && !entityClasses.isEmpty()) {
            features = new HashMap<>();
            for (final String className : entityClasses) {
                final Map<DeviceTypeIdentifier, List<Feature>> featureMap =
                        AccessController.doPrivileged(new PrivilegedAction<Map<DeviceTypeIdentifier, List<Feature>>>() {
                            public Map<DeviceTypeIdentifier, List<Feature>> run() {
                                Map<DeviceTypeIdentifier, List<Feature>> featureMap = new HashMap<>();
                                try {
                                    Class<?> clazz = classLoader.loadClass(className);
                                    Class<DeviceType> deviceTypeClazz = (Class<DeviceType>) classLoader.loadClass(
                                            DeviceType.class.getName());
                                    Annotation deviceTypeAnno = clazz.getAnnotation(deviceTypeClazz);
                                    if (deviceTypeAnno != null) {
                                        Method[] deviceTypeMethod = deviceTypeClazz.getMethods();
                                        String deviceType = invokeMethod(deviceTypeMethod[0], deviceTypeAnno, STRING);
                                        featureAnnotationClazz =
                                                (Class<org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations
                                                        .Feature>) classLoader.loadClass(
                                                        org.wso2.carbon.device.mgt.extensions.feature.mgt
                                                                .annotations.Feature.class.getName());
                                        List<Feature> featureList = getFeatures(clazz.getDeclaredMethods());
                                        DeviceTypeIdentifier deviceTypeIdentifier;
                                        if (isSharedWithAllTenants) {
                                            deviceTypeIdentifier = new DeviceTypeIdentifier(deviceType);
                                        } else {
                                            deviceTypeIdentifier = new DeviceTypeIdentifier(deviceType, tenantId);
                                        }
                                        featureMap.put(deviceTypeIdentifier, featureList);
                                    }
                                } catch (Throwable e) {
                                    log.error("Failed to load the annotation from the features in the " +
                                              "class " + className, e);
                                }
                                return featureMap;
                            }
                        });

                features.putAll(featureMap);
            }
        }
        return features;
    }

    private List<Feature> getFeatures(Method[] methodsList) throws Throwable {
        List<Feature> featureList = new ArrayList<>();
        for (Method currentMethod : methodsList) {
            Annotation featureAnnotation = currentMethod.getAnnotation(featureAnnotationClazz);
            if (featureAnnotation != null) {
                Feature feature = new Feature();
                feature = processFeatureAnnotation(feature, currentMethod);
                Annotation[] annotations = currentMethod.getDeclaredAnnotations();
                Feature.MetadataEntry metadataEntry = new Feature.MetadataEntry();
                metadataEntry.setId(-1);
                Map<String, Object> apiParams = new HashMap<>();
                for (int i = 0; i < annotations.length; i++) {
                    Annotation currentAnnotation = annotations[i];
                    processHttpMethodAnnotation(apiParams, currentAnnotation);
                    if (currentAnnotation.annotationType().getName().equals(Path.class.getName())) {
                        String uri = getPathAnnotationValue(currentMethod);
                        apiParams.put("uri", uri);
                    }
                    apiParams = processParamAnnotations(apiParams, currentMethod);
                }
                metadataEntry.setValue(apiParams);
                List<Feature.MetadataEntry> metaInfoList = new ArrayList<>();
                metaInfoList.add(metadataEntry);
                feature.setMetadataEntries(metaInfoList);
                featureList.add(feature);
            }
        }
        return featureList;
    }

    private Map<String, Object> processParamAnnotations(Map<String, Object> apiParams, Method currentMethod)
            throws Throwable{
        try {
            apiParams.put("pathParams", processParamAnnotations(currentMethod, PathParam.class));
            apiParams.put("queryParams", processParamAnnotations(currentMethod, QueryParam.class));
            apiParams.put("formParams", processParamAnnotations(currentMethod, FormParam.class));
        } catch (ClassNotFoundException e) {
            log.debug("No Form Param found for class " + featureAnnotationClazz.getName());
        }
        return apiParams;
    }

    private List<String> processParamAnnotations(Method currentMethod, Class<?> clazz) throws Throwable{
        List<String> params = new ArrayList<>();
        try {
            Class<?> paramClazz = (Class<?>) classLoader.loadClass(clazz.getName());
            Method[] formMethods = paramClazz.getMethods();
            //Extract method parameter information and store same as feature meta info
            Annotation[][] paramAnnotations = currentMethod.getParameterAnnotations();
            Method valueMethod = formMethods[0];
            for (int j = 0; j < paramAnnotations.length; j++) {
                for (Annotation anno : paramAnnotations[j]) {
                    if (anno.annotationType().getName().equals(clazz.getName())) {
                        params.add(invokeMethod(valueMethod, anno, STRING));
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.debug("No "+ clazz.getName() +" Param found for class " + featureAnnotationClazz.getName());
        }
        return params;
    }

    /**
     * Read Method annotations indicating HTTP Methods
     */
    private void processHttpMethodAnnotation(Map<String, Object> apiParams, Annotation currentAnnotation) {
        //Extracting method with which feature is exposed
        if (currentAnnotation.annotationType().getName().equals(GET.class.getName())) {
            apiParams.put(METHOD, HttpMethod.GET);
        } else if (currentAnnotation.annotationType().getName().equals(POST.class.getName())) {
            apiParams.put(METHOD, HttpMethod.POST);
        } else if (currentAnnotation.annotationType().getName().equals(OPTIONS.class.getName())) {
            apiParams.put(METHOD, HttpMethod.OPTIONS);
        } else if (currentAnnotation.annotationType().getName().equals(DELETE.class.getName())) {
            apiParams.put(METHOD, HttpMethod.DELETE);
        } else if (currentAnnotation.annotationType().getName().equals(PUT.class.getName())) {
            apiParams.put(METHOD, HttpMethod.PUT);
        }
    }

    /**
     * Read Feature annotation and Identify Features
     * @param feature
     * @param currentMethod
     * @return
     * @throws Throwable
     */
    private Feature processFeatureAnnotation(Feature feature, Method currentMethod) throws Throwable{
        Method[] featureAnnoMethods = featureAnnotationClazz.getMethods();
        Annotation featureAnno = currentMethod.getAnnotation(featureAnnotationClazz);
        for (int k = 0; k < featureAnnoMethods.length; k++) {
            switch (featureAnnoMethods[k].getName()) {
            case "name":
                feature.setName(invokeMethod(featureAnnoMethods[k], featureAnno, STRING));
                break;
            case "code":
                feature.setCode(invokeMethod(featureAnnoMethods[k], featureAnno, STRING));
                break;
            case "description":
                feature.setDescription(invokeMethod(featureAnnoMethods[k], featureAnno, STRING));
                break;
            }
        }
        return feature;
    }

    /**
     * Get value depicted by Path Annotation
     * @param currentMethod
     * @return
     * @throws Throwable
     */
    public String getPathAnnotationValue(Method currentMethod) throws Throwable{
        String uri = "";
        try {
            Class<Path> pathClazz = (Class<Path>) classLoader.loadClass(Path.class.getName());
            Annotation pathAnnno = currentMethod.getAnnotation(pathClazz);
            Method[] pathMethods = pathClazz.getMethods();
            Method valueMethod = pathMethods[0];
            uri =  invokeMethod(valueMethod, pathAnnno, STRING);
        } catch (ClassNotFoundException e) {
            log.debug("No Path Param found for class " + featureAnnotationClazz.getName());
        }
        return uri;
    }

    /**
     * When an annotation and method is passed, this method invokes that executes said method against the annotation
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
    public static URL findWebInfClassesPath(ServletContext servletContext)
    {
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