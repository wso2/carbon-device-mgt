/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.TenancyAppPublisherAdminService;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/admin/tenancy")
public class TenancyAppPublisherAdminServiceImpl implements TenancyAppPublisherAdminService{

    public static final String TENANT_ADMIN_USER = "tenantAdmin";
    public static final String CAR_FILE_LOCATION = CarbonUtils.getCarbonRepository()+File.separator+"resources"+File.separator+"analytics";


    @Override
    @POST
    @Path("/app/publisher")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPublish(String tenancyAppContext) throws JSONException, MalformedURLException, RemoteException {

        JSONObject jsonTenancyApp = new JSONObject(tenancyAppContext);
        String tentAdminUser = jsonTenancyApp.getString(TENANT_ADMIN_USER);

        String strHeader = "<m:UserName soapenv:mustUnderstand=\"0\" xmlns:m=\"http://mutualssl.carbon.wso2.org\">'"+tentAdminUser+"'</m:UserName>";
        InputStream is = new ByteArrayInputStream(strHeader.getBytes());
        OMElement header = OMXMLBuilderFactory.createOMBuilder(is).getDocumentElement();

        CarbonAppUploaderStub carbonAppUploaderStub = new CarbonAppUploaderStub("serverUrl" + "/services/CarbonAppUploader");
        carbonAppUploaderStub._getServiceClient().addHeader(header);
        carbonAppUploaderStub.uploadApp(loadCappFromFileSystem());

        return Response.status(201).entity("\"OK. \\n Successfully updated the credentials of the user.\"").build();
    }

    private UploadedFileItem[] loadCappFromFileSystem() throws MalformedURLException {

        Collection<File> carFiles = FileUtils.listFiles(new File(CAR_FILE_LOCATION),null, false);
        List<UploadedFileItem> uploadedFileItemLis = new ArrayList<>();

        for (File carFile : carFiles){
            UploadedFileItem uploadedFileItem = new UploadedFileItem();
            DataHandler param = new DataHandler(carFile.toURI().toURL());
            uploadedFileItem.setDataHandler(param);
            uploadedFileItem.setFileName(carFile.getName());
            uploadedFileItem.setFileType("jar");
            uploadedFileItemLis.add(uploadedFileItem);
        }

        UploadedFileItem[] fileItems = new UploadedFileItem[uploadedFileItemLis.size()];
        fileItems = uploadedFileItemLis.toArray(fileItems);
        return  fileItems;
    }

}
