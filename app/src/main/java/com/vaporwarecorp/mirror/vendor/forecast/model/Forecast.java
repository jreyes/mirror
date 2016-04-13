
package com.vaporwarecorp.mirror.vendor.forecast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Forecast {
// ------------------------------ FIELDS ------------------------------

    @SerializedName("currently")
    @Expose
    public Currently currently;
    @SerializedName("daily")
    @Expose
    public Daily daily;
    @SerializedName("flags")
    @Expose
    public Flags flags;
    @SerializedName("hourly")
    @Expose
    public Hourly hourly;
    @SerializedName("latitude")
    @Expose
    public Double latitude;
    @SerializedName("longitude")
    @Expose
    public Double longitude;
    @SerializedName("offset")
    @Expose
    public Integer offset;
    @SerializedName("timezone")
    @Expose
    public String timezone;
}
