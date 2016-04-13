package com.vaporwarecorp.mirror.ui.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.github.florent37.viewanimator.ViewAnimator;
import com.hound.android.fd.Houndify;
import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.command.HoundifyCommand;
import com.vaporwarecorp.mirror.event.*;
import com.vaporwarecorp.mirror.manager.*;
import com.vaporwarecorp.mirror.manager.HoundifyManager.HoundifyManagerListener;
import com.vaporwarecorp.mirror.ui.fragment.PictureFragment;
import com.vaporwarecorp.mirror.util.FullScreenActivityUtil;
import com.vaporwarecorp.mirror.util.PermissionUtil;
import com.vaporwarecorp.mirror.vendor.forecast.ForecastView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.JavaCameraView;
import timber.log.Timber;

public class MirrorActivity extends Activity {
// ------------------------------ FIELDS ------------------------------

    private View mBackgroundContainer;
    private JavaCameraView mCameraView;
    private TextView mCaptionText;
    private Fragment mCurrentFragment;
    private ForecastView mForecastView;
    private HandWaveManager mHandWaveManager;
    private HotWordManager mHotWordManager;
    private HoundifyManager mHoundifyManager;
    private HoundifyManagerListener mHoundifyManagerListener = new HoundifyManagerListener() {
        @Override
        public void onSuccess(CommandResult result, HoundifyCommand command) {
            removeCurrentFragment();
            if (command != null) {
                command.executeCommand(result, MirrorActivity.this);
            }
        }

        @Override
        public void onError(String errorMessage) {
            MirrorApp.sound(MirrorActivity.this).error();
            setCaptionText(errorMessage);
            startListening();
        }
    };
    private View mOverlayContainer;
    private SpotifyManager mSpotifyManager;
    private TextToSpeechManager mTextToSpeechManager;

// -------------------------- OTHER METHODS --------------------------

    public void displayFragment(Fragment fragment) {
        mCurrentFragment = fragment;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mCurrentFragment)
                .commitAllowingStateLoss();
    }

    public void hideScreen() {
        mBackgroundContainer.setVisibility(View.INVISIBLE);
        mOverlayContainer.setVisibility(View.INVISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(UserInRangeEvent event) {
        showScreen();
        startListening();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(ForecastEvent event) {
        Timber.i("Got ForecastEvent");
        mForecastView.setForecast(event.getForecast());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(UserOutOfRangeEvent event) {
        mHotWordManager.stopListening();
        stopHandGestures();
        hideScreen();
        removeCurrentFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(VideoCompletedEvent event) {
        removeCurrentFragment();
        stopHandGestures();
        showScreen();
        startListening();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(HotWordEvent event) {
        MirrorApp.sound(this).acknowledge();
        mHoundifyManager.voiceSearch(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void showScreen() {
        mBackgroundContainer.setVisibility(View.VISIBLE);
        mOverlayContainer.setVisibility(View.VISIBLE);
    }

    public void speak(String textToSpeak) {
        mTextToSpeechManager.speak(textToSpeak);
    }

    public void startHandGestures() {
        mHandWaveManager.startDetecting(mCameraView);
    }

    public void startListening() {
        mHotWordManager.startListening();
    }

    public void stopHandGestures() {
        mHandWaveManager.stopDetecting();
        mCameraView.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Houndify.REQUEST_CODE) {
            mHoundifyManager.processCommand(resultCode, data, mHoundifyManagerListener);
        } else if (requestCode == SpotifyManager.REQUEST_CODE) {
            mSpotifyManager.processAuthentication(resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mirror);

        checkPermissions();
        onCreateManagers();
        onCreateDisplay();
        onAuthenticateManagers();
    }

    @Override
    protected void onDestroy() {
        if (mSpotifyManager != null) {
            mSpotifyManager.onDestroy();
        }

        super.onDestroy();
        MirrorApp.refWatcher(this).watch(this);
        Process.killProcess(Process.myPid());
    }

    @Override
    protected void onPause() {
        mHandWaveManager.stopDetecting();
        mHotWordManager.stopListening();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        onResumeFullScreen();
    }

    private void checkPermissions() {
        PermissionUtil.checkPermissions(this);
    }

    private void displayBsod() {
        displayFragment(PictureFragment.newInstance("http://wallpapercave.com/wp/KZTDh1d.gif"));
    }

    private void onAuthenticateManagers() {
        mSpotifyManager.authenticate(this);
    }

    private void onCreateDisplay() {
        mBackgroundContainer = findViewById(R.id.background_container);
        mOverlayContainer = findViewById(R.id.overlay_container);
        mCaptionText = (TextView) findViewById(R.id.result_text);
        mCameraView = (JavaCameraView) findViewById(R.id.camera_view);
        mCameraView.setZOrderOnTop(true);
        mCameraView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mForecastView = (ForecastView) findViewById(R.id.forecast_view);

        hideScreen();

        MirrorApp.forecast(this).update();
        MirrorApp.proximity(this).update();
    }

    private void onCreateManagers() {
        mHotWordManager = MirrorApp.hotWord(this);
        mHoundifyManager = MirrorApp.houndify(this);
        mHandWaveManager = MirrorApp.handWave(this);
        mSpotifyManager = MirrorApp.spotify(this);
        mTextToSpeechManager = MirrorApp.textToSpeech(this);
    }

    private void onResumeFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FullScreenActivityUtil.onResume(this);
    }

    private void removeCurrentFragment() {
        if (mCurrentFragment == null) {
            return;
        }

        getFragmentManager().beginTransaction().remove(mCurrentFragment).commitAllowingStateLoss();
        mCurrentFragment = null;
    }

    private void setCaptionText(String text) {
        mCaptionText.setText(text);
        mCaptionText.setVisibility(View.VISIBLE);
        ViewAnimator.animate(mCaptionText).fadeOut().startDelay(600);
    }
}
