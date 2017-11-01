/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * Device
 */
public class Device   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("user")
  private String user = null;

  @JsonProperty("userPattern")
  private String userPattern = null;

  @JsonProperty("role")
  private String role = null;

  /**
   * Provide the ownership status of the device. The following values can be assigned: BYOD: Bring Your Own Device COPE: Corporate-Owned, Personally-Enabled 
   */
  public enum OwnershipEnum {
    BYOD("BYOD"),
    
    COPE("COPE");

    private String value;

    OwnershipEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static OwnershipEnum fromValue(String text) {
      for (OwnershipEnum b : OwnershipEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("ownership")
  private OwnershipEnum ownership = null;

  public Device name(String name) {
    this.name = name;
    return this;
  }

   /**
   * FSDFSDFDSFS
   * @return name
  **/
  @ApiModelProperty(value = "FSDFSDFDSFS")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Device type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Device user(String user) {
    this.user = user;
    return this;
  }

   /**
   * Get user
   * @return user
  **/
  @ApiModelProperty(value = "")
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Device userPattern(String userPattern) {
    this.userPattern = userPattern;
    return this;
  }

   /**
   * Get userPattern
   * @return userPattern
  **/
  @ApiModelProperty(value = "")
  public String getUserPattern() {
    return userPattern;
  }

  public void setUserPattern(String userPattern) {
    this.userPattern = userPattern;
  }

  public Device role(String role) {
    this.role = role;
    return this;
  }

   /**
   * Get role
   * @return role
  **/
  @ApiModelProperty(value = "")
  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public Device ownership(OwnershipEnum ownership) {
    this.ownership = ownership;
    return this;
  }

   /**
   * Provide the ownership status of the device. The following values can be assigned: BYOD: Bring Your Own Device COPE: Corporate-Owned, Personally-Enabled 
   * @return ownership
  **/
  @ApiModelProperty(value = "Provide the ownership status of the device. The following values can be assigned: BYOD: Bring Your Own Device COPE: Corporate-Owned, Personally-Enabled ")
  public OwnershipEnum getOwnership() {
    return ownership;
  }

  public void setOwnership(OwnershipEnum ownership) {
    this.ownership = ownership;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Device device = (Device) o;
    return Objects.equals(this.name, device.name) &&
        Objects.equals(this.type, device.type) &&
        Objects.equals(this.user, device.user) &&
        Objects.equals(this.userPattern, device.userPattern) &&
        Objects.equals(this.role, device.role) &&
        Objects.equals(this.ownership, device.ownership);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, user, userPattern, role, ownership);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Device {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    userPattern: ").append(toIndentedString(userPattern)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    ownership: ").append(toIndentedString(ownership)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

