/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.etc.util.cdmdevice.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.etc.util.ZipArchive;
import org.wso2.carbon.device.mgt.etc.util.cdmdevice.dto.IotDevice;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Provides utility methods required by the iot device management bundle.
 */
public class IotDeviceManagementUtil {

    private static final Log log = LogFactory.getLog(IotDeviceManagementUtil.class.getName());

    public static Document convertToDocument(File file) throws DeviceManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new DeviceManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    private static Device.Property getProperty(String property, String value) {
        if (property != null) {
            Device.Property prop = new Device.Property();
            prop.setName(property);
            prop.setValue(value);
            return prop;
        }
        return null;
    }

    public static IotDevice convertToIotDevice(Device device) {
        IotDevice iotDevice = null;
        if (device != null) {
            iotDevice = new IotDevice();
            iotDevice.setIotDeviceId(device.getDeviceIdentifier());
            iotDevice.setIotDeviceName(device.getName());
            Map<String, String> deviceProperties = new HashMap<String, String>();

            if (device.getProperties() != null) {

                for (Device.Property deviceProperty : device.getProperties()) {
                    deviceProperties.put(deviceProperty.getName(), deviceProperty.getValue());
                }

                iotDevice.setDeviceProperties(deviceProperties);
            } else {
                iotDevice.setDeviceProperties(deviceProperties);
            }
        }
        return iotDevice;
    }

    public static Device convertToDevice(IotDevice iotDevice) {
        Device device = null;
        if (iotDevice != null) {
            device = new Device();
            List<Device.Property> propertyList = new ArrayList<Device.Property>();

            if (iotDevice.getDeviceProperties() != null) {
                for (Map.Entry<String, String> deviceProperty : iotDevice.getDeviceProperties().entrySet()) {
                    propertyList.add(getProperty(deviceProperty.getKey(), deviceProperty.getValue()));
                }
            }

            device.setProperties(propertyList);
            device.setName(iotDevice.getIotDeviceName());
            device.setDeviceIdentifier(iotDevice.getIotDeviceId());
        }
        return device;
    }

    public static ZipArchive getSketchArchive(String archivesPath, String templateSketchPath, Map contextParams)
            throws DeviceManagementException, IOException {

        String sep = File.separator;
        String sketchPath = CarbonUtils.getCarbonHome() + sep + templateSketchPath;

        FileUtils.deleteDirectory(new File(archivesPath));//clear directory
        FileUtils.deleteDirectory(new File(archivesPath + ".zip"));//clear zip
        if (!new File(archivesPath).mkdirs()) { //new dir
            String message = "Could not create directory at path: " + archivesPath;
            log.error(message);
            throw new DeviceManagementException(message);
        }

        String zipFileName = "zipFile.zip";

        try {
            Map<String, List<String>> properties = getProperties(sketchPath + sep + "sketch" + ".properties");
            List<String> templateFiles = properties.get("templates");

//            zipFileName = properties.get("zipfilename").get(0);
            zipFileName = contextParams.get("DEVICE_NAME") + ".zip";

            for (String templateFile : templateFiles) {
                parseTemplate(templateSketchPath + sep + templateFile, archivesPath + sep + templateFile,
                        contextParams);
            }

	        templateFiles.add("sketch.properties");         // ommit copying the props file
	        copyFolder(new File(sketchPath), new File(archivesPath), templateFiles);

        } catch (IOException ex) {
            throw new DeviceManagementException(
                    "Error occurred when trying to read property " + "file sketch.properties", ex);
        }

        try {
            createZipArchive(archivesPath);
        } catch (IOException e) {
            String message = "Zip file for the specific device agent not found at path: " + archivesPath;
            log.error(message);
            log.error(e);
            throw new DeviceManagementException(message, e);
        }
        FileUtils.deleteDirectory(new File(archivesPath));//clear folder

		/* now get the zip file */
        File zip = new File(archivesPath + ".zip");
        return new ZipArchive(zipFileName, zip);
    }

    private static Map<String, List<String>> getProperties(String propertyFilePath) throws IOException {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(propertyFilePath);

            // load a properties file
            prop.load(input);
            Map<String, List<String>> properties = new HashMap<String, List<String>>();

            String templates = prop.getProperty("templates");
            List<String> list = new ArrayList<String>(Arrays.asList(templates.split(",")));
            properties.put("templates", list);

            final String filename = prop.getProperty("zipfilename");
            list = new ArrayList<String>() {{
                add(filename);
            }};
            properties.put("zipfilename", list);
            return properties;

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void parseTemplate(String srcFile, String dstFile, Map contextParams) throws IOException {
        //TODO add velocity 1.7, currently commented
        //TODO conflicting when calling in CXF environment with the opensaml orbit

        //		/*  create a context and add data */
        //		VelocityContext context = new VelocityContext(contextParams);
        //
        //		/*  first, get and initialize an engine  */
        //		VelocityEngine ve = new VelocityEngine();
        //		ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
        //						"org.apache.velocity.runtime.log.Log4JLogChute" );
        //		ve.setProperty("runtime.log.logsystem.log4j.logger", IotDeviceManagementUtil.class.getName());
        //		ve.init();
        //
        //		String sep = File.separator;
        //		Template t = ve.getTemplate(srcFile);
        //		FileWriter writer = null;
        //		try {
        //			writer = new FileWriter(dstFile);
        //			t.merge(context, writer);
        //		} finally {
        //			if (writer != null) {
        //				writer.flush();
        //				writer.close();
        //			}
        //		}

        //read from file
        FileInputStream inputStream = new FileInputStream(srcFile);
        String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString());
        Iterator iterator = contextParams.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            content = content.replaceAll("\\$\\{" + mapEntry.getKey() + "\\}", mapEntry.getValue().toString());
        }
        if (inputStream != null) {
            inputStream.close();
        }
        //write to file
        FileOutputStream outputStream = new FileOutputStream(dstFile);
        IOUtils.write(content, outputStream, StandardCharsets.UTF_8.toString());
        if (outputStream != null) {
            outputStream.close();
        }
    }

    private static void copyFolder(File src, File dest, List<String> excludeFileNames) throws IOException {

        if (src.isDirectory()) {

            //if directory not exists, create it
            if (!dest.exists() && !dest.mkdirs()) {
                String message = "Could not create directory at path: " + dest;
                log.error(message);
                throw new IOException(message);
            }

            //list all the directory contents
            String files[] = src.list();

            if (files == null) {
                log.warn("There are no files insides the directory " + src.getAbsolutePath());
                return;
            }

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile, excludeFileNames);
            }

        } else {
            for (String fileName : excludeFileNames) {
                if (src.getName().equals(fileName)) {
                    return;
                }
            }
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];

                int length;
                //copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } finally {
                silentClose(in);
                silentClose(out);
            }
        }
    }

    private static void silentClose(InputStream is) {
        if (is == null) {
            return;
        }

        try {
            is.close();
        } catch (IOException e) {
            // do nothing
        }

    }

    private static void silentClose(OutputStream os) {
        if (os == null) {
            return;
        }

        try {

            os.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    private static boolean createZipArchive(String srcFolder) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = null;

        try {
            final int BUFFER = 2048;

            FileOutputStream dest = new FileOutputStream(new File(srcFolder + ".zip"));

            out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            File subDir = new File(srcFolder);
            String subdirList[] = subDir.list();

            if (subdirList == null) {
                log.warn("The sub directory " + subDir.getAbsolutePath() + " is empty");
                return false;
            }

            for (String sd : subdirList) {
                // get a list of files from current directory
                File f = new File(srcFolder + "/" + sd);
                if (f.isDirectory()) {
                    String files[] = f.list();

                    if (files == null) {
                        log.warn("The current directory " + f.getAbsolutePath() + " is empty. Has no files");
                        return false;
                    }

                    for (int i = 0; i < files.length; i++) {
                        FileInputStream fi = new FileInputStream(srcFolder + "/" + sd + "/" + files[i]);
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(sd + "/" + files[i]);
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                            out.flush();
                        }

                    }
                } else //it is just a file
                {
                    FileInputStream fi = new FileInputStream(f);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(sd);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                        out.flush();
                    }

                }
            }

            out.flush();
        } finally {
            silentClose(origin);
            silentClose(out);
        }
        return true;
    }

}
