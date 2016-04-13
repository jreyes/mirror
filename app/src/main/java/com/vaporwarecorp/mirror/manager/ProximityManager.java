package com.vaporwarecorp.mirror.manager;

import android.content.Context;
import com.radiusnetworks.proximity.*;
import com.vaporwarecorp.mirror.event.UserInRangeEvent;
import com.vaporwarecorp.mirror.event.UserOutOfRangeEvent;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

import java.util.Collection;
import java.util.Properties;

public class ProximityManager implements ProximityKitRangeNotifier {
// ------------------------------ FIELDS ------------------------------

    private static final int BEACON_MAX_DISTANCE = 2;
    private static final int BEACON_MAX_RETRIES = 5;

    private int mBeaconRetries;
    private Context mContext;
    private boolean mInRange;

// --------------------------- CONSTRUCTORS ---------------------------

    public ProximityManager(Context context, Properties properties) {
        this.mContext = context;
        this.mBeaconRetries = 0;
        init(new KitConfig(properties));
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ProximityKitRangeNotifier ---------------------

    @Override
    public void didRangeBeaconsInRegion(Collection<ProximityKitBeacon> beacons, ProximityKitBeaconRegion region) {
        if (beacons.size() == 0) {
            mBeaconRetries++;
            if (mInRange && mBeaconRetries > BEACON_MAX_RETRIES) {
                mInRange = false;
                Timber.d("beacons.size() == 0");
                EventBus.getDefault().post(new UserOutOfRangeEvent());
            }
            return;
        }

        mBeaconRetries = 0;

        for (ProximityKitBeacon beacon : beacons) {
            if (!mInRange && beacon.getDistance() < BEACON_MAX_DISTANCE) {
                Timber.d("UserInRangeEvent - beacons.getDistance() = %s", beacon.getDistance());
                mInRange = true;
                EventBus.getDefault().post(new UserInRangeEvent());
            }
            if (mInRange && beacon.getDistance() >= BEACON_MAX_DISTANCE) {
                Timber.d("UserOutOfRangeEvent - beacons.getDistance() = %s", beacon.getDistance());
                mInRange = false;
                EventBus.getDefault().post(new UserOutOfRangeEvent());
            }
        }
    }

// -------------------------- OTHER METHODS --------------------------

    public void update() {
        mInRange = false;
    }

    private void init(KitConfig kitConfig) {
        // enable the ProximityKit
        ProximityKitManager proximityKitManager = ProximityKitManager.getInstance(mContext, kitConfig);
        proximityKitManager.setProximityKitRangeNotifier(this);
        proximityKitManager.start();
        proximityKitManager.sync();
    }
}
