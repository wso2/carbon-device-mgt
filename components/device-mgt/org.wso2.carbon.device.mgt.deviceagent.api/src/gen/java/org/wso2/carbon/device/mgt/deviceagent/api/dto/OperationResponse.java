package org.wso2.carbon.device.mgt.deviceagent.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * This class carries all information related to operation responses
 */
@ApiModel(description = "This class carries all information related to operation responses")
public class OperationResponse   {
  @JsonProperty("response")
  private String response = null;

  @JsonProperty("receivedTimeStamp")
  private String receivedTimeStamp = null;

  public OperationResponse response(String response) {
    this.response = response;
    return this;
  }

   /**
   * Operation response returned from the device
   * @return response
  **/
  @ApiModelProperty(example = "SUCCESSFUL", value = "Operation response returned from the device")
  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public OperationResponse receivedTimeStamp(String receivedTimeStamp) {
    this.receivedTimeStamp = receivedTimeStamp;
    return this;
  }

   /**
   * Time that the operation response received
   * @return receivedTimeStamp
  **/
  @ApiModelProperty(example = "Thu Oct 06 11:18:47 IST 2017", value = "Time that the operation response received")
  public String getReceivedTimeStamp() {
    return receivedTimeStamp;
  }

  public void setReceivedTimeStamp(String receivedTimeStamp) {
    this.receivedTimeStamp = receivedTimeStamp;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationResponse operationResponse = (OperationResponse) o;
    return Objects.equals(this.response, operationResponse.response) &&
        Objects.equals(this.receivedTimeStamp, operationResponse.receivedTimeStamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(response, receivedTimeStamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationResponse {\n");
    
    sb.append("    response: ").append(toIndentedString(response)).append("\n");
    sb.append("    receivedTimeStamp: ").append(toIndentedString(receivedTimeStamp)).append("\n");
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

