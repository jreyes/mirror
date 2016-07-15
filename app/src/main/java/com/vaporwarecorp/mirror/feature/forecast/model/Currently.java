
/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vaporwarecorp.mirror.feature.forecast.model;

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
