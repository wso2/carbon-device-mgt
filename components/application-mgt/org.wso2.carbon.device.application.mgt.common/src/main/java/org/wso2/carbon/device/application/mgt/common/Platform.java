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
package org.wso2.carbon.device.application.mgt.common;

import org.wso2.carbon.device.application.mgt.common.jaxrs.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Platform implements Cloneable {

    @Exclude
    private int id;

    private String name;

    private String description;

    private String code;

    private String iconName;

    private boolean fileBased;

    private boolean shared;

    private List<String> tags;

    private List<Property> properties;

    public Platform(Platform platform) {
        this.id = platform.getId();
        this.name = platform.getName();
        this.description = platform.getDescription();
        this.code = platform.getCode();
        this.iconName = platform.getIconName();
        this.fileBased = platform.isFileBased();
        this.shared = platform.isShared();
        if (platform.getProperties() != null) {
            this.properties = new ArrayList<>();
            for (Property property : platform.getProperties()) {
                this.properties.add(new Property(property));
            }
        }
        if (platform.getTags() != null) {
            this.tags = new ArrayList<>();
            for (String tag : platform.getTags()) {
                this.tags.add(tag);
            }
        }
    }

    public Platform() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code != null) {
            code = code.trim().toLowerCase();
        }
        this.code = code;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isFileBased() {
        return fileBased;
    }

    public void setFileBased(boolean fileBased) {
        this.fileBased = fileBased;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }


    public static class Property implements Cloneable {

        private String name;
        private boolean optional;
        private String defaultValue;

        public Property(Property property) {
            this.name = property.getName();
            this.optional = property.isOptional();
            this.defaultValue = property.getDefaultValue();
        }

        public Property() {

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
