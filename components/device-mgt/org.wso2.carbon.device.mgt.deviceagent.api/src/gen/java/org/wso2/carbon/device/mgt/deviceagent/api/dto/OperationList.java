package org.wso2.carbon.device.mgt.deviceagent.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.Operation;
import java.util.Objects;

/**
 * Representation of a operation list
 */
@ApiModel(description = "Representation of a operation list")
public class OperationList   {
  @JsonProperty("opeartions")
  private List<Operation> opeartions = new ArrayList<Operation>();

  public OperationList opeartions(List<Operation> opeartions) {
    this.opeartions = opeartions;
    return this;
  }

  public OperationList addOpeartionsItem(Operation opeartionsItem) {
    this.opeartions.add(opeartionsItem);
    return this;
  }

   /**
   * Operations list.
   * @return opeartions
  **/
  @ApiModelProperty(value = "Operations list.")
  public List<Operation> getOpeartions() {
    return opeartions;
  }

  public void setOpeartions(List<Operation> opeartions) {
    this.opeartions = opeartions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationList operationList = (OperationList) o;
    return Objects.equals(this.opeartions, operationList.opeartions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(opeartions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationList {\n");
    
    sb.append("    opeartions: ").append(toIndentedString(opeartions)).append("\n");
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

