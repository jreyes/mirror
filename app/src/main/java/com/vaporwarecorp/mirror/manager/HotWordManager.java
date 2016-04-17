package com.vaporwarecorp.mirror.manager;

import android.content.Context;
import com.vaporwarecorp.mirror.event.HotWordEvent;
import com.vaporwarecorp.mirror.vendor.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.vaporwarecorp.mirror.vendor.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class HotWordManager implements RecognitionListener {
// ------------------------------ FIELDS ------------------------------

    private static final String KEYPHRASE = "HotWordKeyphrase";
    private static final String KWS_SEARCH = "COMMAND";

    private Context mContext;
    private String mKeyphrase;
    private SpeechRecognizer mRecognizer;

// --------------------------- CONSTRUCTORS ---------------------------

    public HotWordManager(Context context, Properties properties) throws IOException {
        mContext = context;
        mKeyphrase = properties.getProperty(KEYPHRASE);
        setupRecognizer();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface RecognitionListener ---------------------

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
            EventBus.getDefault().post(new HotWordEvent());
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        String text = hypothesis.getHypstr().trim().toLowerCase();
        Timber.d("onPartialResult %s", text);
    }

    @Override
    public void onError(Exception error) {
    }

    @Override
    public void onTimeout() {
    }

// -------------------------- OTHER METHODS --------------------------

    public void onDestroy() {
        mRecognizer.removeListener(this);
        mRecognizer.shutdown();
    }

    public void startListening() {
        mRecognizer.stop();
        mRecognizer.startListening(KWS_SEARCH);
    }

    public void stopListening() {
        mRecognizer.stop();
    }

    private void setupRecognizer() throws IOException {
        File assetsDir = new Assets(mContext).syncAssets();
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        mRecognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setKeywordThreshold(1e-20f)
                .setBoolean("-allphone_ci", true)
                .getRecognizer();
        mRecognizer.addListener(this);
        mRecognizer.addKeyphraseSearch(KWS_SEARCH, mKeyphrase);
    }
}
