/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.net.URI;

/**
 * This class is used to register the device APIs to the CoAP Resource Directory Server.
 */
public class CoAPResourceDirectoryClient extends CoapClient {

	private static final Log log = LogFactory.getLog(CoAPResourceDirectoryClient.class);
	//TODO - localhost should be ommited and the host must be defined in a config file [next phase - implementing CoAP RD outside the IoT server]
	private final static String LOCAL_HOST = "localhost";
    private final static String RESOURCE_DIRECTORY = "rd";

	public CoAPResourceDirectoryClient() {
		super(CoAP.COAP_URI_SCHEME, LOCAL_HOST, CoAP.DEFAULT_COAP_PORT, RESOURCE_DIRECTORY);
    }

    //register publishing api to the resource directory
    public void registerAPI(API api, String domain) {
        if (this.isServerConnected()) {

            String endpoint = api.getContext().split("/")[1];
            URI apiURI = URI.create(api.getUrl());
            String payload = "";

            //add uri templates
            if(!api.getUriTemplates().isEmpty()) {
                //payload+="ut=";
                for (URITemplate uriTemplate : api.getUriTemplates()) {

					payload += ",<" + uriTemplate.getUriTemplate() + ">;rt=\"device\";if=\"" + uriTemplate.getHTTPVerb()
							+ "\"";
				}

            }

            Request request = new Request(CoAP.Code.POST, CoAP.Type.CON); //coap request to send the api to server

			request.setURI(this.getURI() + "?ep=" + endpoint + "&d=" + domain+"&con=" +apiURI.getScheme()+"://"+apiURI.getHost()+":"+apiURI.getPort()); //endpoint name, domain and context
			//request.setURI(this.getURI() + "?ep=" + endpoint + "&d=" + domain); //with coap context
			request.setPayload(payload);
			//add api to the rd using the client
			this.advanced(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    if (log.isDebugEnabled()) {
                        log.debug(coapResponse.getResponseText());
                    }
                }

                @Override
                public void onError() {
                    if (log.isDebugEnabled()) {
                        log.debug("Error adding API to the resource directory");
                    }
                }
            }, request);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("CoAP server not connected");
            }
        }
    }



    public boolean isServerConnected() {
        return true;
        //return this.ping();
    }
}
