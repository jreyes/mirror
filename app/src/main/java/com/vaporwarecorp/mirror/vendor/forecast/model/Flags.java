
package com.vaporwarecorp.mirror.vendor.forecast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Flags {
// ------------------------------ FIELDS ------------------------------

    @SerializedName("isd-stations")
    @Expose
    public List<String> isdStations = new ArrayList<>();

    @SerializedName("sources")
    @Expose
    public List<String> sources = new ArrayList<>();
    @SerializedName("units")
    @Expose
    public String units;
}
