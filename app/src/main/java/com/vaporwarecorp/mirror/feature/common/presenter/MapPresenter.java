package com.vaporwarecorp.mirror.feature.common.presenter;

import com.robopupu.api.feature.FeaturePresenter;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface MapPresenter extends FeaturePresenter {
// ------------------------------ FIELDS ------------------------------

    String MAP_FROM_LATITUDE = "MAP_FROM_LATITUDE";
    String MAP_FROM_LONGITUDE = "MAP_FROM_LONGITUDE";
    String MAP_FROM_TITLE = "MAP_FROM_TITLE";
    String MAP_TO_LATITUDE = "MAP_TO_LATITUDE";
    String MAP_TO_LONGITUDE = "MAP_TO_LONGITUDE";
    String MAP_TO_TITLE = "MAP_TO_TITLE";
}
