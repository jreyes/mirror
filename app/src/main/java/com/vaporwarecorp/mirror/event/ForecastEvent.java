package com.vaporwarecorp.mirror.event;

import com.vaporwarecorp.mirror.component.forecast.model.Forecast;

public class ForecastEvent implements Event {
// ------------------------------ FIELDS ------------------------------

    private final Forecast forecast;

// --------------------------- CONSTRUCTORS ---------------------------

    public ForecastEvent(Forecast forecast) {
        this.forecast = forecast;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Forecast getForecast() {
        return forecast;
    }
}
