package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.hotword.SpeechRecognizer;
import com.vaporwarecorp.mirror.event.HotWordEvent;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;

import static com.vaporwarecorp.mirror.component.hotword.SpeechRecognizerSetup.defaultSetup;


@Plugin
public class HotWordManagerImpl extends AbstractManager implements HotWordManager, RecognitionListener {
// ------------------------------ FIELDS ------------------------------

    private static final String KEYPHRASE = "HotWordKeyphrase";
    private static final String KWS_SEARCH = "COMMAND";

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;

    private String mKeyphrase;
    private SpeechRecognizer mRecognizer;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(HotWordManager.class)
    public HotWordManagerImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HotWordManager ---------------------

    @Override
    public void destroy() {
        mRecognizer.shutdown();
        mRecognizer = null;
    }

    @Override
    public void startListening() {
        mRecognizer.stop();
        mRecognizer.startListening(KWS_SEARCH);
    }

    @Override
    public void stopListening() {
        mRecognizer.stop();
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        mKeyphrase = mAppManager.getApplicationProperties().getProperty(KEYPHRASE);
        initializeRecognizer();
    }

// --------------------- Interface RecognitionListener ---------------------

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        String text = hypothesis.getHypstr().trim().toLowerCase();
        Timber.d("onPartialResult %s", text);
        if (text.equals(mKeyphrase)) {
            mRecognizer.stop();
            mEventManager.post(new HotWordEvent());
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        String text = hypothesis.getHypstr().trim().toLowerCase();
        Timber.d("onResult %s", text);
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onTimeout() {
    }

    private void initializeRecognizer() {
        try {
            File assetsDir = new Assets(mAppManager.getAppContext()).syncAssets();
            mRecognizer = defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                    .setKeywordThreshold(1e-10f)
                    .setBoolean("-allphone_ci", true)
                    .getRecognizer();
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
            mAppManager.exitApplication();
        }

        mRecognizer.addListener(this);
        mRecognizer.addKeyphraseSearch(KWS_SEARCH, mKeyphrase);
    }
}
