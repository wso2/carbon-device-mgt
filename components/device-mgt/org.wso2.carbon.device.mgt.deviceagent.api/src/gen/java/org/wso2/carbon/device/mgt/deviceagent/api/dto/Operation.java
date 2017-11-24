package org.wso2.carbon.device.mgt.deviceagent.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.OperationResponse;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.Property;
import java.util.Objects;

/**
 * Representation of a operation list
 */
@ApiModel(description = "Representation of a operation list")
public class Operation   {
  @JsonProperty("id")
  private Integer id = null;

  @JsonProperty("code")
  private String code = null;

  @JsonProperty("properties")
  private List<Property> properties = new ArrayList<Property>();

  /**
   * The operation type that was carried out on the device.
   */
  public enum TypeEnum {
    COMMAND("COMMAND"),
    
    CONFIG("CONFIG"),
    
    MESSAGE("MESSAGE"),
    
    INFO("INFO"),
    
    PROFILE("PROFILE"),
    
    POLICY("POLICY");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("type")
  private TypeEnum type = null;

  /**
   * The status of the operation that has been carried out on a device. The operation status can be any one of the following; IN-PROGRESS - The operation is processing on the EMM server side and has not yet been delivered to the device. PENDING - The operation is delivered to the device but the response from the device is pending. COMPLETED - The operation is delivered to the device and the server has received a response back from the device. ERROR - An error has occurred while carrying out the operation.
   */
  public enum StatusEnum {
    IN_PROGRESS("IN_PROGRESS"),
    
    PENDING("PENDING"),
    
    COMPLETED("COMPLETED"),
    
    ERROR("ERROR"),
    
    REPEATED("REPEATED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
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

  /**
   * How the operation should be executed.
   */
  public enum ControlEnum {
    REPEAT("REPEAT"),
    
    NO_REPEAT("NO_REPEAT"),
    
    PAUSE_SEQUENCE("PAUSE_SEQUENCE"),
    
    STOP_SEQUENCE("STOP_SEQUENCE");

    private String value;

    ControlEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ControlEnum fromValue(String text) {
      for (ControlEnum b : ControlEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("control")
  private ControlEnum control = null;

  @JsonProperty("receivedTimeStamp")
  private String receivedTimeStamp = null;

  @JsonProperty("createdTimeStamp")
  private String createdTimeStamp = null;

  @JsonProperty("isEnabled")
  private Boolean isEnabled = null;

  @JsonProperty("payLoad")
  private Object payLoad = null;

  @JsonProperty("operationResponse")
  private String operationResponse = null;

  @JsonProperty("activityId")
  private String activityId = null;

  @JsonProperty("responses")
  private List<OperationResponse> responses = new ArrayList<OperationResponse>();

  public Operation id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * The operations carried out on a device is recorded in a database table.
   * @return id
  **/
  @ApiModelProperty(value = "The operations carried out on a device is recorded in a database table.")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Operation code(String code) {
    this.code = code;
    return this;
  }

   /**
   * operation code
   * @return code
  **/
  @ApiModelProperty(example = "DEVICE_INFO", value = "operation code")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Operation properties(List<Property> properties) {
    this.properties = properties;
    return this;
  }

  public Operation addPropertiesItem(Property propertiesItem) {
    this.properties.add(propertiesItem);
    return this;
  }

   /**
   * Get properties
   * @return properties
  **/
  @ApiModelProperty(value = "")
  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  public Operation type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * The operation type that was carried out on the device.
   * @return type
  **/
  @ApiModelProperty(value = "The operation type that was carried out on the device.")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public Operation status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * The status of the operation that has been carried out on a device. The operation status can be any one of the following; IN-PROGRESS - The operation is processing on the EMM server side and has not yet been delivered to the device. PENDING - The operation is delivered to the device but the response from the device is pending. COMPLETED - The operation is delivered to the device and the server has received a response back from the device. ERROR - An error has occurred while carrying out the operation.
   * @return status
  **/
  @ApiModelProperty(value = "The status of the operation that has been carried out on a device. The operation status can be any one of the following; IN-PROGRESS - The operation is processing on the EMM server side and has not yet been delivered to the device. PENDING - The operation is delivered to the device but the response from the device is pending. COMPLETED - The operation is delivered to the device and the server has received a response back from the device. ERROR - An error has occurred while carrying out the operation.")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public Operation control(ControlEnum control) {
    this.control = control;
    return this;
  }

   /**
   * How the operation should be executed.
   * @return control
  **/
  @ApiModelProperty(value = "How the operation should be executed.")
  public ControlEnum getControl() {
    return control;
  }

  public void setControl(ControlEnum control) {
    this.control = control;
  }

  public Operation receivedTimeStamp(String receivedTimeStamp) {
    this.receivedTimeStamp = receivedTimeStamp;
    return this;
  }

   /**
   * The time WSO2 EMM received the response from the device
   * @return receivedTimeStamp
  **/
  @ApiModelProperty(example = "Thu Oct 06 11:18:47 IST 2017", value = "The time WSO2 EMM received the response from the device")
  public String getReceivedTimeStamp() {
    return receivedTimeStamp;
  }

  public void setReceivedTimeStamp(String receivedTimeStamp) {
    this.receivedTimeStamp = receivedTimeStamp;
  }

  public Operation createdTimeStamp(String createdTimeStamp) {
    this.createdTimeStamp = createdTimeStamp;
    return this;
  }

   /**
   * The time when the operation was requested to be carried out
   * @return createdTimeStamp
  **/
  @ApiModelProperty(example = "Thu Oct 06 11:18:47 IST 2017", value = "The time when the operation was requested to be carried out")
  public String getCreatedTimeStamp() {
    return createdTimeStamp;
  }

  public void setCreatedTimeStamp(String createdTimeStamp) {
    this.createdTimeStamp = createdTimeStamp;
  }

  public Operation isEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
    return this;
  }

   /**
   * If the assigned value is true it indicates that a policy is enforced on the device. If the assigned value is false it indicates that a policy is not enforced on a device.
   * @return isEnabled
  **/
  @ApiModelProperty(value = "If the assigned value is true it indicates that a policy is enforced on the device. If the assigned value is false it indicates that a policy is not enforced on a device.")
  public Boolean getIsEnabled() {
    return isEnabled;
  }

  public void setIsEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public Operation payLoad(Object payLoad) {
    this.payLoad = payLoad;
    return this;
  }

   /**
   * Payload of the operation to be sent to the device
   * @return payLoad
  **/
  @ApiModelProperty(value = "Payload of the operation to be sent to the device")
  public Object getPayLoad() {
    return payLoad;
  }

  public void setPayLoad(Object payLoad) {
    this.payLoad = payLoad;
  }

  public Operation operationResponse(String operationResponse) {
    this.operationResponse = operationResponse;
    return this;
  }

   /**
   * Response received from the device
   * @return operationResponse
  **/
  @ApiModelProperty(value = "Response received from the device")
  public String getOperationResponse() {
    return operationResponse;
  }

  public void setOperationResponse(String operationResponse) {
    this.operationResponse = operationResponse;
  }

  public Operation activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

   /**
   * The identifier used to identify the operation uniquely.
   * @return activityId
  **/
  @ApiModelProperty(value = "The identifier used to identify the operation uniquely.")
  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public Operation responses(List<OperationResponse> responses) {
    this.responses = responses;
    return this;
  }

  public Operation addResponsesItem(OperationResponse responsesItem) {
    this.responses.add(responsesItem);
    return this;
  }

   /**
   * List of operation responses.
   * @return responses
  **/
  @ApiModelProperty(value = "List of operation responses.")
  public List<OperationResponse> getResponses() {
    return responses;
  }

  public void setResponses(List<OperationResponse> responses) {
    this.responses = responses;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Operation operation = (Operation) o;
    return Objects.equals(this.id, operation.id) &&
        Objects.equals(this.code, operation.code) &&
        Objects.equals(this.properties, operation.properties) &&
        Objects.equals(this.type, operation.type) &&
        Objects.equals(this.status, operation.status) &&
        Objects.equals(this.control, operation.control) &&
        Objects.equals(this.receivedTimeStamp, operation.receivedTimeStamp) &&
        Objects.equals(this.createdTimeStamp, operation.createdTimeStamp) &&
        Objects.equals(this.isEnabled, operation.isEnabled) &&
        Objects.equals(this.payLoad, operation.payLoad) &&
        Objects.equals(this.operationResponse, operation.operationResponse) &&
        Objects.equals(this.activityId, operation.activityId) &&
        Objects.equals(this.responses, operation.responses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code, properties, type, status, control, receivedTimeStamp, createdTimeStamp, isEnabled, payLoad, operationResponse, activityId, responses);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Operation {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    control: ").append(toIndentedString(control)).append("\n");
    sb.append("    receivedTimeStamp: ").append(toIndentedString(receivedTimeStamp)).append("\n");
    sb.append("    createdTimeStamp: ").append(toIndentedString(createdTimeStamp)).append("\n");
    sb.append("    isEnabled: ").append(toIndentedString(isEnabled)).append("\n");
    sb.append("    payLoad: ").append(toIndentedString(payLoad)).append("\n");
    sb.append("    operationResponse: ").append(toIndentedString(operationResponse)).append("\n");
    sb.append("    activityId: ").append(toIndentedString(activityId)).append("\n");
    sb.append("    responses: ").append(toIndentedString(responses)).append("\n");
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

