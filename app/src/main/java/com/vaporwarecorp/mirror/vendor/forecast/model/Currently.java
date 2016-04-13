
package com.vaporwarecorp.mirror.vendor.forecast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Currently {
// ------------------------------ FIELDS ------------------------------

    @SerializedName("apparentTemperature")
    @Expose
    public Double apparentTemperature;
    @SerializedName("cloudCover")
    @Expose
    public Double cloudCover;
    @SerializedName("dewPoint")
    @Expose
    public Double dewPoint;
    @SerializedName("humidity")
    @Expose
    public Double humidity;
    @SerializedName("icon")
    @Expose
    public String icon;
    @SerializedName("ozone")
    @Expose
    public Double ozone;
    @SerializedName("precipIntensity")
    @Expose
    public Double precipIntensity;
    @SerializedName("precipProbability")
    @Expose
    public Double precipProbability;
    @SerializedName("precipType")
    @Expose
    public String precipType;
    @SerializedName("pressure")
    @Expose
    public Double pressure;
    @SerializedName("summary")
    @Expose
    public String summary;
    @SerializedName("temperature")
    @Expose
    public Double temperature;
    @SerializedName("time")
    @Expose
    public Integer time;
    @SerializedName("windBearing")
    @Expose
    public Integer windBearing;
    @SerializedName("windSpeed")
    @Expose
    public Double windSpeed;
}
