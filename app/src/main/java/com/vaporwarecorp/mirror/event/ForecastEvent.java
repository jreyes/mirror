package com.vaporwarecorp.mirror.event;

import com.vaporwarecorp.mirror.vendor.forecast.model.Forecast;

public class ForecastEvent {
// ------------------------------ FIELDS ------------------------------

    private Forecast forecast;

// --------------------------- CONSTRUCTORS ---------------------------

    public ForecastEvent(Forecast forecast) {
        this.forecast = forecast;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Forecast getForecast() {
        return forecast;
    }
}
