package com.vaporwarecorp.mirror.feature.houndify;

import android.content.Intent;
import android.os.Handler;
import com.fasterxml.jackson.databind.JsonNode;
import com.hound.android.fd.DefaultRequestInfoFactory;
import com.hound.android.fd.HoundSearchResult;
import com.hound.android.fd.Houndify;
import com.hound.android.sdk.VoiceSearch;
import com.hound.android.sdk.VoiceSearchInfo;
import com.hound.android.sdk.VoiceSearchListener;
import com.hound.core.model.sdk.*;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.D;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.event.CommandEvent;
import com.vaporwarecorp.mirror.event.SpeechEvent;
import com.vaporwarecorp.mirror.feature.Command;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import solid.functions.Action1;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static solid.collectors.ToList.toList;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(HoundifyCommandManager.class)
public class HoundifyCommandManagerImpl
        extends AbstractManager
        implements HoundifyCommandManager, VoiceSearchListener {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = HoundifyCommandManager.class.getName();
    private static final String PREF_CLIENT_ID = PREF + ".PREF_CLIENT_ID";
    private static final String PREF_CLIENT_KEY = PREF + ".PREF_CLIENT_KEY";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;
    @Plug
    PluginFeatureManager mFeatureManager;

    private String mClientId;
    private String mClientKey;
    private List<ClientMatch> mClientMatches;
    private Collection<HoundifyCommand> mCommands;
    private JsonNode mConversationState;
    private Handler mHandler;
    private VoiceSearch mVoiceSearch;

    private final Runnable pollVolumeRunnable = new Runnable() {
        @Override
        public void run() {
            mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_VOLUME,
                    String.valueOf(mVoiceSearch.getCurrentVolume())));
            mHandler.postDelayed(pollVolumeRunnable, 5);
        }
    };

// --------------------------- CONSTRUCTORS ---------------------------

    public HoundifyCommandManagerImpl() {
        mClientMatches = new ArrayList<>();
        mCommands = new ArrayList<>();
        mHandler = new Handler();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CommandManager ---------------------

    @Override
    public boolean isEnabled() {
        return isNotEmpty(mClientId) && isNotEmpty(mClientKey);
    }

    @Override
    public void start() {
        if (mVoiceSearch != null || mClientId == null || mClientKey == null) {
            return;
        }

        initializeCommands();
    }

    @Override
    public void stop() {
        stream(mCommands).forEach((Action1<HoundifyCommand>) PluginBus::unplug);

        mClientMatches.clear();
        mCommands.clear();

        if (mVoiceSearch != null) {
            mVoiceSearch.abort();
            mVoiceSearch = null;
        }
    }

    @Override
    public void voiceSearch() {
        if (mVoiceSearch == null) {
            mVoiceSearch = new VoiceSearch.Builder()
                    .setRequestInfo(new RequestInfoFactory().create())
                    .setAudioSource(Houndify.get(mAppManager.getAppContext()).getAudioInputStream())
                    .setClientId(mClientId)
                    .setClientKey(mClientKey)
                    .setListener(this)
                    .build();
            mVoiceSearch.start();
            mHandler.post(pollVolumeRunnable);
        }
    }

// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/houndify.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("clientId", mClientId).put("clientKey", mClientKey).toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateString(PREF_CLIENT_ID, jsonNode, "clientId");
        mConfigurationManager.updateString(PREF_CLIENT_KEY, jsonNode, "clientKey");
        loadConfiguration();
    }

// --------------------- Interface HoundifyCommandManager ---------------------

    @Override
    public void processCommand(int resultCode, Intent data) {
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

// --------------------- Interface VoiceSearchListener ---------------------

    @Override
    public void onTranscriptionUpdate(final PartialTranscript transcript) {
    }

    @Override
    public void onResponse(final HoundResponse response, final VoiceSearchInfo info) {
        updateUiState();
        deliverResult(HoundSearchResult.createSuccess(response, info));
    }

    @Override
    public void onError(final Exception ex, final VoiceSearchInfo info) {
        deliverResult(HoundSearchResult.createError(ex, info.getErrorType(), info));
    }

    @Override
    public void onAbort(final VoiceSearchInfo info) {
        deliverResult(HoundSearchResult.createAborted());
    }

    @Override
    public void onRecordingStopped() {
        updateUiState();
    }

    private void deliverResult(HoundSearchResult result) {
        mHandler.removeCallbacks(pollVolumeRunnable);

        if (result.hasResult()) {
            HoundResponse response = result.getResponse();
            if (response.getResults().isEmpty()) {
                return;
            }

            CommandResult commandResult = response.getResults().get(0);
            Timber.i(ToStringBuilder.reflectionToString(commandResult, ToStringStyle.MULTI_LINE_STYLE));

            mConversationState = commandResult.getConversationState();

            for (HoundifyCommand command : mCommands) {
                if (command.matches(commandResult)) {
                    command.executeCommand(commandResult);
                    return;
                }
            }

            mEventManager.post(new SpeechEvent(commandResult.getSpokenResponseLong()));
        } else if (result.getErrorType() != null && result.getException() != null) {
            onError(result.getErrorType() + "\n\n" + result.getException().getMessage());
        } else {
            onError("Aborted search");
        }
    }

    private void initializeCommands() {
        mClientMatches.clear();
        mCommands = stream(D.getAll(Command.class))
                .filter(c -> c instanceof HoundifyCommand)
                .map(c -> {
                    HoundifyCommand command = (HoundifyCommand) c;
                    if (!PluginBus.isPlugged(c)) {
                        PluginBus.plug(c);
                        if (command.getClientMatch() != null) {
                            mClientMatches.add(command.getClientMatch());
                        }
                    }
                    Timber.i("loaded %s houndify command", c.getClass().getCanonicalName());
                    return command;
                })
                .collect(toList());
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mClientId = mConfigurationManager.getString(PREF_CLIENT_ID, "");
        mClientKey = mConfigurationManager.getString(PREF_CLIENT_KEY, "");
        Timber.i("loaded HoundifyCommandManager configuration");
    }

    private void onError(String message) {
        mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_ERROR, message));
    }

    private void updateUiState() {
        switch (mVoiceSearch.getState()) {
            case STATE_SEARCHING:
                mHandler.removeCallbacks(pollVolumeRunnable);
                mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_SEARCHING, KEYWORD_HOUNDIFY));
                break;
            case STATE_ERROR:
            case STATE_ABORTED:
            case STATE_FINISHED:
                mHandler.removeCallbacks(pollVolumeRunnable);
                if (mVoiceSearch != null) {
                    mVoiceSearch.abort();
                    mVoiceSearch = null;
                }
                mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_STOP, KEYWORD_HOUNDIFY));
                break;
            default:
                throw new IllegalStateException("This should never happen!");
        }
    }

    private class RequestInfoFactory extends DefaultRequestInfoFactory {
        RequestInfoFactory() {
            super(mAppManager.getAppContext());
        }

        @Override
        public HoundRequestInfo create() {
            final HoundRequestInfo requestInfo = super.create();
            requestInfo.setConversationState(mConversationState);
            requestInfo.setClientMatches(mClientMatches);
            return requestInfo;
        }
    }
}
