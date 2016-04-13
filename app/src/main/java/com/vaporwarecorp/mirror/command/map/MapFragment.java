package com.vaporwarecorp.mirror.command.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.directions.route.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.R;

import java.util.ArrayList;

public class MapFragment extends com.google.android.gms.maps.MapFragment implements OnMapReadyCallback, RoutingListener {
// ------------------------------ FIELDS ------------------------------

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent};
    private static final String MAP_FROM_LATITUDE = "MAP_FROM_LATITUDE";
    private static final String MAP_FROM_LONGITUDE = "MAP_FROM_LONGITUDE";
    private static final String MAP_FROM_TITLE = "MAP_FROM_TITLE";
    private static final String MAP_TO_LATITUDE = "MAP_TO_LATITUDE";
    private static final String MAP_TO_LONGITUDE = "MAP_TO_LONGITUDE";
    private static final String MAP_TO_TITLE = "MAP_TO_TITLE";

    private GoogleMap mMap;

// -------------------------- STATIC METHODS --------------------------

    public static MapFragment newInstance(String title, double latitude, double longitude) {
        Bundle args = new Bundle();
        args.putString(MAP_FROM_TITLE, title);
        args.putDouble(MAP_FROM_LATITUDE, latitude);
        args.putDouble(MAP_FROM_LONGITUDE, longitude);

        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static MapFragment newInstance(String fromTitle, Double fromLatitude, Double fromLongitude,
                                          String toTitle, Double toLatitude, Double toLongitude) {
        MapFragment fragment = newInstance(fromTitle, fromLatitude, fromLongitude);
        fragment.getArguments().putString(MAP_TO_TITLE, toTitle);
        fragment.getArguments().putDouble(MAP_TO_LONGITUDE, toLongitude);
        fragment.getArguments().putDouble(MAP_TO_LATITUDE, toLatitude);
        return fragment;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnMapReadyCallback ---------------------

    @SuppressWarnings("ResourceType")
    @Override
    public void onMapReady(GoogleMap map) {
        // set the map
        mMap = map;

        // now display the map
        MarkerOptions fromMarkerOptions = getFromMarkerOptions();
        MarkerOptions toMarkerOptions = getToMarkerOptions();

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fromMarkerOptions.getPosition(), 14));
        mMap.addMarker(fromMarkerOptions);
        if (toMarkerOptions != null) {
            mMap.addMarker(toMarkerOptions);

            LatLngBounds bounds = LatLngBounds.builder()
                    .include(fromMarkerOptions.getPosition())
                    .include(toMarkerOptions.getPosition())
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

            new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(fromMarkerOptions.getPosition(), toMarkerOptions.getPosition())
                    .build()
                    .execute();
        }
    }

// --------------------- Interface RoutingListener ---------------------

    @Override
    public void onRoutingFailure(RouteException e) {
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        for (int i = 0; i < routes.size(); i++) {
            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;
            PolylineOptions polyOptions = new PolylineOptions()
                    .color(getResources().getColor(COLORS[colorIndex]))
                    .width(5 + i)
                    .addAll(routes.get(i).getPoints());
            mMap.addPolyline(polyOptions);
        }
    }

    @Override
    public void onRoutingCancelled() {
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getMapAsync(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MirrorApp.refWatcher(getActivity()).watch(this);
    }

    private MarkerOptions getFromMarkerOptions() {
        String title = getArguments().getString(MAP_FROM_TITLE);
        LatLng latLng = new LatLng(
                getArguments().getDouble(MAP_FROM_LATITUDE),
                getArguments().getDouble(MAP_FROM_LONGITUDE)
        );
        return new MarkerOptions().position(latLng).title(title);
    }

    private MarkerOptions getToMarkerOptions() {
        String title = getArguments().getString(MAP_TO_TITLE);
        if (title == null) {
            return null;
        }

        LatLng latLng = new LatLng(
                getArguments().getDouble(MAP_TO_LATITUDE),
                getArguments().getDouble(MAP_TO_LONGITUDE)
        );
        return new MarkerOptions().position(latLng).title(title);
    }
}
