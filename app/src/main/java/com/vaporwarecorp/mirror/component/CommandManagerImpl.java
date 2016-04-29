package com.vaporwarecorp.mirror.component;

import android.content.Intent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.hound.android.fd.DefaultRequestInfoFactory;
import com.hound.android.fd.HoundSearchResult;
import com.hound.android.fd.Houndify;
import com.hound.core.model.sdk.CommandResult;
import com.hound.core.model.sdk.HoundRequestInfo;
import com.hound.core.model.sdk.HoundResponse;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.command.HoundifyVoiceSearchActivity;
import com.vaporwarecorp.mirror.event.SpeechEvent;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import timber.log.Timber;

@Plugin
public class CommandManagerImpl extends AbstractManager implements CommandManager {
// ------------------------------ FIELDS ------------------------------

    private static final String CLIENT_ID = "HoundClientId";
    private static final String CLIENT_KEY = "HoundClientKey";

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;
    @Plug
    PluginFeatureManager mFeatureManager;

    private JsonNode mConversationState;
    private Houndify mHoundify;
    private JsonNodeFactory mNodeFactory;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(CommandManager.class)
    public CommandManagerImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CommandManager ---------------------

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
            if (commandResult.getHtmlData() != null) {
                Timber.i(ToStringBuilder.reflectionToString(commandResult.getHtmlData(), ToStringStyle.MULTI_LINE_STYLE));
            }

            mConversationState = commandResult.getConversationState();
            mEventManager.post(new SpeechEvent(commandResult.getSpokenResponseLong()));

            /*
            for (HoundifyCommand command : mCommands) {
                if (command.matches(commandResult)) {
                    listener.onSuccess(commandResult, command);
                    return;
                }
            }
            */
        } else if (result.getErrorType() != null && result.getException() != null) {
            Timber.e(result.getException(), "Exception on process command");
            String error = result.getErrorType() + "\n\n" + result.getException().getMessage();
            //listener.onError(error);
        } else {
            //listener.onError("Aborted search");
        }
    }

    @Override
    public void voiceSearch() {
        HoundifyVoiceSearchActivity.newInstance(mFeatureManager.getForegroundActivity());
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        initializeHoundify();
    }

    private void initializeHoundify() {
        mHoundify = Houndify.get(mAppManager.getAppContext());
        mHoundify.setClientId(mAppManager.getApplicationProperties().getProperty(CLIENT_ID));
        mHoundify.setClientKey(mAppManager.getApplicationProperties().getProperty(CLIENT_KEY));
        mHoundify.setRequestInfoFactory(new RequestInfoFactory());
    }

    private class RequestInfoFactory extends DefaultRequestInfoFactory {
        RequestInfoFactory() {
            super(mAppManager.getAppContext());
        }

        @Override
        public HoundRequestInfo create() {
            final HoundRequestInfo requestInfo = super.create();
            requestInfo.setConversationState(mConversationState);
            //requestInfo.setClientMatches(mClientMatches);
            return requestInfo;
        }
    }
}
