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
package org.wso2.carbon.apimgt.webapp.publisher.lifecycle.listener;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.webapp.publisher.APIConfig;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherService;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherUtil;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResource;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceManagementException;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceManager;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.scannotation.AnnotationDB;
import org.scannotation.WarUrlFinder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class APIPublisherLifecycleListener implements LifecycleListener {

    private static final String API_CONFIG_DEFAULT_VERSION = "1.0.0";

    private static final String PARAM_MANAGED_API_ENABLED = "managed-api-enabled";
    private static final String PARAM_MANAGED_API_NAME = "managed-api-name";
    private static final String PARAM_MANAGED_API_VERSION = "managed-api-version";
    private static final String PARAM_MANAGED_API_CONTEXT = "managed-api-context";
    private static final String PARAM_MANAGED_API_ENDPOINT = "managed-api-endpoint";
    private static final String PARAM_MANAGED_API_OWNER = "managed-api-owner";
    private static final String PARAM_MANAGED_API_TRANSPORTS = "managed-api-transports";
    private static final String PARAM_MANAGED_API_IS_SECURED = "managed-api-isSecured";
    private static final String PARAM_MANAGED_API_APPLICATION = "managed-api-application";
    private static final String PARAM_MANAGED_API_CONTEXT_TEMPLATE = "managed-api-context-template";

    private static final String RESOURCE_CONFIG_PATH = "META-INF" + File.separator + "resources.xml";


    private static final Log log = LogFactory.getLog(APIPublisherLifecycleListener.class);

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
            StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
            ServletContext servletContext = context.getServletContext();


            String param = servletContext.getInitParameter(PARAM_MANAGED_API_ENABLED);
            boolean isManagedApi = (param != null && !param.isEmpty()) && Boolean.parseBoolean(param);

            if (isManagedApi) {
                try {
                    List<APIResource> resources = scanStandardContext(context);
                    APIConfig apiConfig = this.buildApiConfig(servletContext, resources);
                    try {
                        apiConfig.init();
                        API api = APIPublisherUtil.getAPI(apiConfig);
                        APIPublisherService apiPublisherService =
                                APIPublisherDataHolder.getInstance().getApiPublisherService();
                        if (apiPublisherService == null) {
                            throw new IllegalStateException("API Publisher service is not initialized properly");
                        }
                        apiPublisherService.publishAPI(api);
                        apiPublisherService.adddSubscriber(apiConfig.getOwner(), "");
                        Subscriber subscriber = new Subscriber(apiConfig.getOwner());
                        Application application = new Application(apiConfig.getApiApplication(), subscriber);
                        application.setTier("Unlimited");
                        application.setGroupId("");
                        int applicationId = apiPublisherService.createApplication(application, apiConfig.getOwner());
                        APIIdentifier subId = api.getId();
                        subId.setTier("Unlimited");
                        apiPublisherService.addSubscription(subId,applicationId, apiConfig.getOwner());
                    } catch (Throwable e) {
                        /* Throwable is caught as none of the RuntimeExceptions that can potentially occur at this point
                        does not seem to be logged anywhere else within the framework */
                        log.error("Error occurred while publishing API '" + apiConfig.getName() + "' with the context '" +
                                apiConfig.getContext() + "' and version '" + apiConfig.getVersion() + "'", e);
                    }
                } catch (IOException e) {
                    //todo: Hacky code.. needs to be refactored!
                }
            }
        }
    }

    private APIConfig buildApiConfig(ServletContext servletContext, List<APIResource> resourceList) {
        APIConfig apiConfig = new APIConfig();

        String name = servletContext.getInitParameter(PARAM_MANAGED_API_NAME);
        if (name == null || name.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-name' attribute is not configured. Therefore, using the default, " +
                        "which is the name of the web application");
            }
            name = servletContext.getServletContextName();
        }
        apiConfig.setName(name);

        String version = servletContext.getInitParameter(PARAM_MANAGED_API_VERSION);
        if (version == null || version.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-version' attribute is not configured. Therefore, using the " +
                        "default, which is '1.0.0'");
            }
            version = API_CONFIG_DEFAULT_VERSION;
        }
        apiConfig.setVersion(version);

        String context = servletContext.getInitParameter(PARAM_MANAGED_API_CONTEXT);
        if (context == null || context.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-context' attribute is not configured. Therefore, using the default, " +
                        "which is the original context assigned to the web application");
            }
            context = servletContext.getContextPath();
        }
        apiConfig.setContext(context);

        String contextTemplate = servletContext.getInitParameter(PARAM_MANAGED_API_CONTEXT_TEMPLATE);
        if (contextTemplate == null || contextTemplate.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-context-template' attribute is not configured. Therefore, using the default, " +
                        "which is the original context template assigned to the web application");
            }
            contextTemplate = servletContext.getContextPath();
        }
        apiConfig.setContextTemplate(contextTemplate);

        String apiApplication = servletContext.getInitParameter(PARAM_MANAGED_API_APPLICATION);
        if (apiApplication == null || apiApplication.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-context-template' attribute is not configured. Therefore, using the default, " +
                        "which is the original context template assigned to the web application");
            }
            apiApplication = servletContext.getContextPath();
        }
        apiConfig.setApiApplication(apiApplication);

        String endpoint = servletContext.getInitParameter(PARAM_MANAGED_API_ENDPOINT);
        if (endpoint == null || endpoint.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-endpoint' attribute is not configured");
            }
            endpoint = APIPublisherUtil.getApiEndpointUrl(context);
        }
        apiConfig.setEndpoint(endpoint);

        String owner = servletContext.getInitParameter(PARAM_MANAGED_API_OWNER);
        if (owner == null || owner.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-owner' attribute is not configured");
            }
        }
        apiConfig.setOwner(owner);

        String isSecuredParam = servletContext.getInitParameter(PARAM_MANAGED_API_IS_SECURED);
        boolean isSecured;
        if (isSecuredParam == null || isSecuredParam.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-isSecured' attribute is not configured. Therefore, using the default, " +
                        "which is 'true'");
            }
            isSecured = false;
        } else {
            isSecured = Boolean.parseBoolean(isSecuredParam);
        }
        apiConfig.setSecured(isSecured);

        String transports = servletContext.getInitParameter(PARAM_MANAGED_API_TRANSPORTS);
        if (transports == null || transports.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-transports' attribute is not configured. Therefore using the default, " +
                        "which is 'https'");
            }
            transports = "https";
        }
        apiConfig.setTransports(transports);

        try {
            APIResourceManager apiResourceManager = APIResourceManager.getInstance();
            apiResourceManager.initializeResources(servletContext.getResourceAsStream(
                    RESOURCE_CONFIG_PATH));
            Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
            for(APIResource apiResource: resourceList){
                URITemplate template = new URITemplate();
                template.setAuthType(apiResource.getAuthType());
                template.setHTTPVerb(apiResource.getHttpVerb());
                template.setResourceURI(apiResource.getUri());
                template.setUriTemplate(apiResource.getUriTemplate());
                uriTemplates.add(template);
            }
            apiConfig.setUriTemplates(uriTemplates);
        } catch (APIResourceManagementException e) {
            log.error("Exception occurred while adding the resources from webapp : "
                              + servletContext.getContextPath(),e);
        }

        return apiConfig;
    }


    public List<APIResource> scanStandardContext(StandardContext context) throws IOException {
        // Set<String> entityClasses = getAnnotatedClassesStandardContext(context, Path.class);
        //todo pack the annotation db with feature
        Set<String> entityClasses = null;
        List<APIResource> resources = null;

        AnnotationDB db = new AnnotationDB();
        db.addIgnoredPackages("org.apache");
        db.addIgnoredPackages("org.codehaus");
        db.addIgnoredPackages("org.springframework");

        final String path = context.getRealPath("/WEB-INF/classes");
        //TODO follow the above line for "WEB-INF/lib" as well

        URL[] libPath = WarUrlFinder.findWebInfLibClasspaths(context.getServletContext());
        URL classPath = WarUrlFinder.findWebInfClassesPath(context.getServletContext());
        URL[] urls = (URL[]) ArrayUtils.add(libPath, libPath.length, classPath);

        db.scanArchives(urls);
        entityClasses = db.getAnnotationIndex().get(Path.class.getName());

        if (entityClasses != null && !entityClasses.isEmpty()) {
            for (String className : entityClasses) {
                try {

                    List<URL> fileUrls = convertToFileUrl(libPath, classPath, context.getServletContext());

                    URLClassLoader cl = new URLClassLoader(fileUrls.toArray(new URL[fileUrls.size()]), this.getClass().getClassLoader());

                    ClassLoader cl2 = context.getServletContext().getClassLoader();
                    Class<?> clazz = cl2.loadClass(className);

                    Class<Path> pathClazz = (Class<Path>) cl2.loadClass(Path.class.getName());
                    resources = extractAPIInfo(context.getServletContext(), clazz, pathClazz);

                } catch (ClassNotFoundException e) {
                    //log the error and continue the loop
                    log.error(e.getMessage(), e);
                }
            }
        }
        return resources;
    }

    private static List<APIResource> extractAPIInfo(ServletContext context, Class<?> clazz, Class<Path> pathClazz) {

        Annotation rootContectAnno = clazz.getAnnotation(pathClazz);
        List<APIResource> resourceList = null;

        log.info("======================== API INFO ======================= ");
        if (context != null) {
            log.info("Application Context root = " + context.getContextPath());
        }

        if (rootContectAnno != null) {
            InvocationHandler handler = Proxy.getInvocationHandler(rootContectAnno);
            Method[] methods = pathClazz.getMethods();
            String root;

            try {
                root = (String) handler.invoke(rootContectAnno, methods[0], null);

                ClassLoader cl = context.getClassLoader();

                resourceList = new ArrayList<APIResource>();

                log.info("#### API Root  Context = " + root);
                List<String> contextList = new ArrayList<String>();
                for (Method method : clazz.getDeclaredMethods()) {
                    Annotation methodContextAnno = method.getAnnotation(pathClazz);
                    if (methodContextAnno != null) {
                        InvocationHandler methodHandler = Proxy.getInvocationHandler(methodContextAnno);
                        String subCtx = (String) methodHandler.invoke(methodContextAnno, methods[0], null);
                        log.info("************** Sub Context : "+subCtx);
                        APIResource resource = new APIResource();
                        resource.setUriTemplate(subCtx);
                        resource.setUri("http://localhost:9763"+root+"/"+subCtx);
                        resource.setAuthType("Any");

                        Annotation[] annotations = method.getDeclaredAnnotations();
                        for(int i=0; i<annotations.length; i++){

                            if(annotations[i].annotationType().getName().equals(GET.class.getName())){
                                log.info("This has a GET Method!");
                                resource.setHttpVerb("GET");
                            }
                            if(annotations[i].annotationType().getName().equals(POST.class.getName())){
                                log.info("This has a POST Method!");
                                resource.setHttpVerb("POST");
                            }
                            if(annotations[i].annotationType().getName().equals(OPTIONS.class.getName())){
                                log.info("This has a OPTIONS Method!");
                                resource.setHttpVerb("OPTIONS");
                            }
                            if(annotations[i].annotationType().getName().equals(DELETE.class.getName())){
                                log.info("This has a DELETE Method!");
                                resource.setHttpVerb("DELETE");
                            }
                            if(annotations[i].annotationType().getName().equals(PUT.class.getName())){
                                log.info("This has a PUT Method!");
                                resource.setHttpVerb("PUT");
                            }
                            if(annotations[i].annotationType().getName().equals(Consumes.class.getName())){
                                Class<Consumes> consumesClass = (Class<Consumes>) cl.loadClass(Consumes.class.getName());
                                Method[] consumesClassMethods = consumesClass.getMethods();
                                Annotation consumesAnno = method.getAnnotation(consumesClass);
                                InvocationHandler consumesHandler = Proxy.getInvocationHandler(consumesAnno);
                                String contentType = ((String[]) consumesHandler.invoke(consumesAnno, consumesClassMethods[0], null))[0];
                                log.info("Consumes : "+contentType);
                            }
                            if(annotations[i].annotationType().getName().equals(Produces.class.getName())){
                                Class<Produces> producesClass = (Class<Produces>) cl.loadClass(Produces.class.getName());
                                Method[] producesClassMethods = producesClass.getMethods();
                                Annotation producesAnno = method.getAnnotation(producesClass);
                                InvocationHandler producesHandler = Proxy.getInvocationHandler(producesAnno);
                                String producesType = ((String[]) producesHandler.invoke(producesAnno, producesClassMethods[0], null))[0];
                                log.info("Produces : "+producesType);
                            }
                        }
                        contextList.add(methods[0]+" "+subCtx);
                        resourceList.add(resource);
                    }
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            log.info("===================================================== ");
            //todo log.info summery of the api context info resulting from the scan
        }
        return resourceList;
    }


    private List<URL> convertToFileUrl(URL[] libPath, URL classPath, ServletContext context) {

        if ((libPath != null || libPath.length == 0) && classPath == null) {
            return null;
        }

        List<URL> list = new ArrayList<URL>();
        if (classPath != null) {
            list.add(classPath);
        }

        if (libPath.length != 0) {
            final String libBasePath = context.getRealPath("/WEB-INF/lib");
            for (URL lib : libPath) {
                String path = lib.getPath();
                if (path != null) {
                    String fileName = path.substring(path.lastIndexOf(File.separator));
                    try {
                        list.add(new URL("jar:file://" + libBasePath + File.separator + fileName + "!/"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return list;
    }

}
