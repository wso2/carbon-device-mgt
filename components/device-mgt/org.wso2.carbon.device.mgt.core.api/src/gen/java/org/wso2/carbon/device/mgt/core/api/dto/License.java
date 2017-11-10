package org.wso2.carbon.device.mgt.core.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.Objects;

/**
 * License.
 */
public class License {
    @JsonProperty("provider")
    private String provider = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("version")
    private String version = null;

    @JsonProperty("language")
    private String language = null;

    @JsonProperty("validFrom")
    private Date validFrom = null;

    @JsonProperty("validTo")
    private Date validTo = null;

    @JsonProperty("text")
    private String text = null;

    public License provider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Get provider.
     *
     * @return provider
     **/
    @ApiModelProperty(required = true, value = "")
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public License name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name.
     *
     * @return name
     **/
    @ApiModelProperty(required = true, value = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public License version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get version.
     *
     * @return version
     **/
    @ApiModelProperty(required = true, value = "")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public License language(String language) {
        this.language = language;
        return this;
    }

    /**
     * Get language.
     *
     * @return language
     **/
    @ApiModelProperty(required = true, value = "")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public License validFrom(Date validFrom) {
        this.validFrom = (Date) validFrom.clone();
        return this;
    }

    /**
     * Get validFrom.
     *
     * @return validFrom
     **/
    @ApiModelProperty(value = "")
    public Date getValidFrom() {
        return (Date) validFrom.clone();
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = (Date) validFrom.clone();
    }

    public License validTo(Date validTo) {
        this.validTo = (Date) validTo.clone();
        return this;
    }

    /**
     * Get validTo.
     *
     * @return validTo
     **/
    @ApiModelProperty(value = "")
    public Date getValidTo() {
        return (Date) validTo.clone();
    }

    public void setValidTo(Date validTo) {
        this.validTo = (Date) validTo.clone();
    }

    public License text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get text.
     *
     * @return text
     **/
    @ApiModelProperty(required = true, value = "")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        License license = (License) o;
        return Objects.equals(this.provider, license.provider) &&
                Objects.equals(this.name, license.name) &&
                Objects.equals(this.version, license.version) &&
                Objects.equals(this.language, license.language) &&
                Objects.equals(this.validFrom, license.validFrom) &&
                Objects.equals(this.validTo, license.validTo) &&
                Objects.equals(this.text, license.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, name, version, language, validFrom, validTo, text);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class License {\n");

        sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    validFrom: ").append(toIndentedString(validFrom)).append("\n");
        sb.append("    validTo: ").append(toIndentedString(validTo)).append("\n");
        sb.append("    text: ").append(toIndentedString(text)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces.
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

