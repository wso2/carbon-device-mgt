--
-- Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
--
-- WSO2 Inc. licenses this file to you under the Apache License,
-- Version 2.0 (the "License"); you may not use this file except
-- in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.

CREATE TABLE IF NOT EXISTS DM_DEVICE_CERTIFICATE (
      ID                    INTEGER auto_increment NOT NULL,
      SERIAL_NUMBER            VARCHAR(500) DEFAULT NULL,
    	CERTIFICATE                  BLOB DEFAULT NULL,
    	TENANT_ID INTEGER DEFAULT 0,
    	USERNAME  VARCHAR(500) DEFAULT NULL,
    	PRIMARY KEY (ID)
);