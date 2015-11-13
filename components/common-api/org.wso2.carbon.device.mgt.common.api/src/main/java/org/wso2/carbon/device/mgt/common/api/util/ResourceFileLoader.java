/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.common.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ResourceFileLoader {

	private static Log log = LogFactory.getLog(ResourceFileLoader.class);
	private String filePath;

	public ResourceFileLoader(String fileName) {
		String path = ResourceFileLoader.class.getClassLoader().getResource("").getPath();

		String fullPath = path;
		try {
			fullPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {

		}
		//log.info(fullPath);
		String pathArr[] = fullPath.split("/WEB-INF/classes/");
		filePath = pathArr[0] + fileName;

	}

	public String getPath() {
		return filePath;
	}

	public File getFile() {
		File file = new File(filePath);
		return file;
	}

}
