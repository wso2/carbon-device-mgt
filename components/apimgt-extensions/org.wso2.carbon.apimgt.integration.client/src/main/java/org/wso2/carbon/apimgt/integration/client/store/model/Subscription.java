/**
 * WSO2 API Manager - Store
 * This document specifies a **RESTful API** for WSO2 **API Manager** - Store.  It is written with [swagger 2](http://swagger.io/). 
 *
 * OpenAPI spec version: 0.10.0
 * Contact: architecture@wso2.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
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


package org.wso2.carbon.apimgt.integration.client.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * Subscription
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:54.991+05:30")
public class Subscription   {
  @JsonProperty("subscriptionId")
  private String subscriptionId = null;

  @JsonProperty("applicationId")
  private String applicationId = null;

  @JsonProperty("apiIdentifier")
  private String apiIdentifier = null;

  @JsonProperty("tier")
  private String tier = null;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    BLOCKED("BLOCKED"),
    
    PROD_ONLY_BLOCKED("PROD_ONLY_BLOCKED"),
    
    UNBLOCKED("UNBLOCKED"),
    
    ON_HOLD("ON_HOLD"),
    
    REJECTED("REJECTED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
          if (String.valueOf(b.value).equals(text)) {
              return b;
          }
      }
      return null;
    }
  }

  @JsonProperty("status")
  private StatusEnum status = null;

  public Subscription subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

   /**
   * Get subscriptionId
   * @return subscriptionId
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public Subscription applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

   /**
   * Get applicationId
   * @return applicationId
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", required = true, value = "")
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public Subscription apiIdentifier(String apiIdentifier) {
    this.apiIdentifier = apiIdentifier;
    return this;
  }

   /**
   * Get apiIdentifier
   * @return apiIdentifier
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", required = true, value = "")
  public String getApiIdentifier() {
    return apiIdentifier;
  }

  public void setApiIdentifier(String apiIdentifier) {
    this.apiIdentifier = apiIdentifier;
  }

  public Subscription tier(String tier) {
    this.tier = tier;
    return this;
  }

   /**
   * Get tier
   * @return tier
  **/
  @ApiModelProperty(example = "Unlimited", required = true, value = "")
  public String getTier() {
    return tier;
  }

  public void setTier(String tier) {
    this.tier = tier;
  }

  public Subscription status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * Get status
   * @return status
  **/
  @ApiModelProperty(example = "UNBLOCKED", value = "")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Subscription subscription = (Subscription) o;
    return Objects.equals(this.subscriptionId, subscription.subscriptionId) &&
        Objects.equals(this.applicationId, subscription.applicationId) &&
        Objects.equals(this.apiIdentifier, subscription.apiIdentifier) &&
        Objects.equals(this.tier, subscription.tier) &&
        Objects.equals(this.status, subscription.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionId, applicationId, apiIdentifier, tier, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Subscription {\n");

    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    apiIdentifier: ").append(toIndentedString(apiIdentifier)).append("\n");
    sb.append("    tier: ").append(toIndentedString(tier)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

