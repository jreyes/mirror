package com.vaporwarecorp.mirror.feature.houndify;

import android.content.Intent;
import com.fasterxml.jackson.databind.JsonNode;
import com.hound.android.fd.DefaultRequestInfoFactory;
import com.hound.android.fd.HoundSearchResult;
import com.hound.android.fd.Houndify;
import com.hound.core.model.sdk.ClientMatch;
import com.hound.core.model.sdk.CommandResult;
import com.hound.core.model.sdk.HoundRequestInfo;
import com.hound.core.model.sdk.HoundResponse;
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(HoundifyCommandManager.class)
public class HoundifyCommandManagerImpl extends AbstractManager implements HoundifyCommandManager {
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
    private boolean mEnabled;
    private Houndify mHoundify;

// --------------------------- CONSTRUCTORS ---------------------------

    public HoundifyCommandManagerImpl() {
        mClientMatches = new ArrayList<>();
        mCommands = new ArrayList<>();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CommandManager ---------------------

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    @Override
    public void start() {
        if (!isEnabled()) {
            return;
        }

        initializeHoundify();
        initializeCommands();
    }

    @Override
    public void stop() {
        stream(mCommands).forEach(PluginBus::unplug);

        mClientMatches.clear();
        mCommands.clear();

        if (mHoundify != null) {
            mHoundify.setRequestInfoFactory(null);
            mHoundify = null;
        }
    }

    @Override
    public void voiceSearch() {
        if (isEnabled()) {
            HoundifyVoiceSearchActivity.newInstance(mFeatureManager.getForegroundActivity());
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
        final HoundSearchResult result = mHoundify.fromActivityResult(resultCode, data);
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

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

    private void initializeCommands() {
        if (mCommands.isEmpty()) {
            for (HoundifyCommand command : D.getAll(HoundifyCommand.class)) {
                PluginBus.plug(command);
                mCommands.add(command);
                if (command.getClientMatch() != null) {
                    mClientMatches.add(command.getClientMatch());
                }
                Timber.i("loaded %s command", command.getClass().getCanonicalName());
            }
        }
    }

    private void initializeHoundify() {
        mHoundify = Houndify.get(mAppManager.getAppContext());
        mHoundify.setClientId(mClientId);
        mHoundify.setClientKey(mClientKey);
        mHoundify.setRequestInfoFactory(new RequestInfoFactory());
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mClientId = mConfigurationManager.getString(PREF_CLIENT_ID, "");
        mClientKey = mConfigurationManager.getString(PREF_CLIENT_KEY, "");
        mEnabled = !(isEmpty(mClientId) || isEmpty(mClientKey));
        Timber.i("loaded HoundifyCommandManager configuration");
    }

    private void onError(String message) {
        mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_ERROR, message));
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
