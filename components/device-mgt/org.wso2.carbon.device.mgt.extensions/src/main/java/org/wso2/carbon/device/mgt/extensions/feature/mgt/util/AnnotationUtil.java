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
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
public class AnnotationUtil {

	private static final Log log = LogFactory.getLog(AnnotationUtil.class);

	private static final String PACKAGE_ORG_APACHE = "org.apache";
	private static final String PACKAGE_ORG_CODEHAUS = "org.codehaus";
	private static final String PACKAGE_ORG_SPRINGFRAMEWORK = "org.springframework";
	public static final String STRING_ARR = "string_arr";
	public static final String STRING = "string";
	private Class<org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature> featureClazz;
	private ClassLoader classLoader;
	private ServletContext servletContext;


	public AnnotationUtil(final StandardContext context) {
		servletContext = context.getServletContext();
		classLoader = servletContext.getClassLoader();
	}

	/**
	 * Scan the context for classes with annotations
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
	 */
	public Map<String, List<Feature>> extractFeatures(Set<String> entityClasses) throws ClassNotFoundException {
		Map<String, List<Feature>> features = null;
		if (entityClasses != null && !entityClasses.isEmpty()) {
			features = new HashMap<>();
			for (final String className : entityClasses) {
				final Map<String, List<Feature>> featureMap =
						AccessController.doPrivileged(new PrivilegedAction<Map<String, List<Feature>>>() {
							public Map<String, List<Feature>> run() {
								Map<String, List<Feature>> featureMap = new HashMap<>();
								try {
									Class<?> clazz = classLoader.loadClass(className);
									Class<DeviceType> deviceTypeClazz = (Class<DeviceType>) classLoader.loadClass(
											DeviceType.class.getName());
									Annotation deviceTypeAnno = clazz.getAnnotation(deviceTypeClazz);
									if (deviceTypeAnno != null) {
										Method[] deviceTypeMethod = deviceTypeClazz.getMethods();
										String deviceType = invokeMethod(deviceTypeMethod[0], deviceTypeAnno, STRING);
										featureClazz =
												(Class<org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations
														.Feature>) classLoader.loadClass(
														org.wso2.carbon.device.mgt.extensions.feature.mgt
																.annotations.Feature.class.getName());
										List<Feature> featureList = getFeatures(clazz.getDeclaredMethods());
										featureMap.put(deviceType, featureList);
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

	private List<Feature> getFeatures(Method[] annotatedMethods) throws Throwable {
		List<Feature> featureList = new ArrayList<>();
		for (Method method : annotatedMethods) {
			Annotation methodAnnotation = method.getAnnotation(featureClazz);
			if (methodAnnotation != null) {
				Annotation[] annotations = method.getDeclaredAnnotations();
				for (int i = 0; i < annotations.length; i++) {
					if (annotations[i].annotationType().getName().equals(
							org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature.class.getName())) {
						Feature feature = new Feature();
						Method[] featureAnnoMethods = featureClazz.getMethods();
						Annotation featureAnno = method.getAnnotation(featureClazz);

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
								case "type":
									feature.setType(invokeMethod(featureAnnoMethods[k], featureAnno, STRING));
									break;
							}
						}
						//Extracting method with which feature is exposed
						if (annotations[i].annotationType().getName().equals(GET.class.getName())) {
							feature.setMethod(HttpMethod.GET);
						}
						if (annotations[i].annotationType().getName().equals(POST.class.getName())) {
							feature.setMethod(HttpMethod.POST);
						}
						if (annotations[i].annotationType().getName().equals(OPTIONS.class.getName())) {
							feature.setMethod(HttpMethod.OPTIONS);
						}
						if (annotations[i].annotationType().getName().equals(DELETE.class.getName())) {
							feature.setMethod(HttpMethod.DELETE);
						}
						if (annotations[i].annotationType().getName().equals(PUT.class.getName())) {
							feature.setMethod(HttpMethod.PUT);
						}
						try {
							Class<FormParam> formParamClazz = (Class<FormParam>) classLoader.loadClass(
									FormParam.class.getName());
							Method[] formMethods = formParamClazz.getMethods();
							//Extract method parameter information and store same as feature meta info
							List<Feature.MetadataEntry> metaInfoList = new ArrayList<>();
							Annotation[][] paramAnnotations = method.getParameterAnnotations();
							for (int j = 0; j < paramAnnotations.length; j++) {
								for (Annotation anno : paramAnnotations[j]) {
									if (anno.annotationType().getName().equals(FormParam.class.getName())) {
										Feature.MetadataEntry metadataEntry = new Feature.MetadataEntry();
										metadataEntry.setId(j);
										metadataEntry.setValue(invokeMethod(formMethods[0], anno, STRING));
										metaInfoList.add(metadataEntry);
									}
								}
							}
							feature.setMetadataEntries(metaInfoList);
						} catch (ClassNotFoundException e) {
							log.debug("No Form Param found for class " + featureClazz.getName());
						}
						featureList.add(feature);
					}
				}
			}
		}
		return featureList;
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
}