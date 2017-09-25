package org.wso2.carbon.device.mgt.extensions.utils;

import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class Utils {

    /**
     * To get the device type configuration based on the configuration file
     * @param configurationFile Relevant configuration file of a device type
     * @return the DeviceTypeConfiguration object of the relevant Device Type
     * @throws DeviceTypeConfigurationException DeviceType Configuration Exception
     * @throws IOException IO Exception
     * @throws SAXException SAX Exception
     * @throws ParserConfigurationException Parser Configuration Exception
     * @throws JAXBException JAXB Exception
     */
    public static  DeviceTypeConfiguration getDeviceTypeConfiguration(File configurationFile)
            throws DeviceTypeConfigurationException, IOException, SAXException, ParserConfigurationException,
            JAXBException {

        Document doc = convertToDocument(configurationFile);

        /* Un-marshaling Webapp Authenticator configuration */
        JAXBContext ctx = JAXBContext.newInstance(DeviceTypeConfiguration.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        //unmarshaller.setSchema(getSchema());
        return (DeviceTypeConfiguration) unmarshaller.unmarshal(doc);

    }

    private static Document convertToDocument(File file)
            throws DeviceTypeConfigurationException, ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        return docBuilder.parse(file);
    }
}
