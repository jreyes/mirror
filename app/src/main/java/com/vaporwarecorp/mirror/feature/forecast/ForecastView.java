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

package com.vaporwarecorp.mirror.feature.forecast;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.feature.forecast.model.Forecast;
import com.vaporwarecorp.mirror.util.ForecastUtil;

public class ForecastView extends RelativeLayout {
// ------------------------------ FIELDS ------------------------------

    private TextView mIcon;
    private TextView mPrecipitation;
    private TextView mPrecipitationIcon;
    private TextView mTemperature;
    private TextView mWeatherSummary;

// --------------------------- CONSTRUCTORS ---------------------------

    public ForecastView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_forecast, this);

        mTemperature = (TextView) findViewById(R.id.temperature);
        mIcon = (TextView) findViewById(R.id.icon);
        mWeatherSummary = (TextView) findViewById(R.id.weather_summary);
        mPrecipitationIcon = (TextView) findViewById(R.id.precipitation_icon);
        mPrecipitation = (TextView) findViewById(R.id.precipitation);

        if (!getRootView().isInEditMode()) {
            Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/weathericons-regular-webfont.ttf");
            mIcon.setTypeface(font);
            mPrecipitationIcon.setTypeface(font);
        }
    }

// -------------------------- OTHER METHODS --------------------------

    public void setForecast(Forecast forecast) {
        if (forecast == null || forecast.currently == null || forecast.hourly == null) {
            return;
        }

        setTemperature(forecast.currently.temperature);
        setWeatherSummary(forecast.hourly.summary);
        setPrecipitation(forecast.currently.precipProbability);
        setIcon(forecast.currently.icon);
    }

    private void setIcon(String icon) {
        mIcon.setText(ForecastUtil.getIconResource(icon, getContext()));
    }

    private void setPrecipitation(double dayPrecipitationProbability) {
        mPrecipitationIcon.setText(ForecastUtil.getIconResource("umbrella", getContext()));
        mPrecipitation.setText(String.format("%d%%", Math.round(100 * dayPrecipitationProbability)));
    }

    private void setTemperature(double temperature) {
        mTemperature.setText(String.format("%d°", Math.round(temperature)));
    }

    private void setWeatherSummary(String weatherSummary) {
        mWeatherSummary.setText(stripPeriod(weatherSummary));
    }

    private String stripPeriod(String sentence) {
        if (sentence == null) {
            return null;
        }
        if (sentence.endsWith(".")) {
            return sentence.substring(0, sentence.length() - 1);
        } else {
            return sentence;
        }
    }
}
