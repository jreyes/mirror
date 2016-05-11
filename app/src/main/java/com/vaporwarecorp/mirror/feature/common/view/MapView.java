package com.vaporwarecorp.mirror.feature.common.view;

import com.google.android.gms.maps.model.MarkerOptions;
import com.robopupu.api.mvp.View;

public interface MapView extends View {
// -------------------------- OTHER METHODS --------------------------

    void displayMap(MarkerOptions fromMarkerOptions);

    void displayMap(MarkerOptions fromMarkerOptions, MarkerOptions toMarkerOptions);
}
