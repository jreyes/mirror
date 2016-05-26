package com.vaporwarecorp.mirror.component;

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
import com.vaporwarecorp.mirror.component.command.HoundifyCommand;
import com.vaporwarecorp.mirror.component.command.HoundifyVoiceSearchActivity;
import com.vaporwarecorp.mirror.event.CommandEvent;
import com.vaporwarecorp.mirror.event.SpeechEvent;
import com.vaporwarecorp.mirror.feature.Command;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static solid.stream.Stream.stream;

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

    private List<ClientMatch> mClientMatches;
    private Collection<Command> mCommands;
    private JsonNode mConversationState;
    private Houndify mHoundify;

// --------------------------- CONSTRUCTORS ---------------------------

    //
    @Scope(MirrorAppScope.class)
    @Provides(CommandManager.class)
    public CommandManagerImpl() {
        mClientMatches = new ArrayList<>();
        mCommands = new ArrayList<>();
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

            mConversationState = commandResult.getConversationState();

            for (Command command : mCommands) {
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

    @Override
    public void start() {
        initializeHoundify();
        initializeCommands();
    }

    @Override
    public void stop() {
        stream(mCommands).forEach(PluginBus::unplug);

        mClientMatches.clear();
        mCommands.clear();
        mHoundify.setRequestInfoFactory(null);
        mHoundify = null;
    }

    @Override
    public void voiceSearch() {
        HoundifyVoiceSearchActivity.newInstance(mFeatureManager.getForegroundActivity());
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onUnplugged(PluginBus bus) {
        PluginBus.unplug(Command.class);
    }

    private void initializeCommands() {
        if (mCommands.isEmpty()) {
            for (Command command : D.getAll(Command.class)) {
                PluginBus.plug(command);
                mCommands.add(command);
                if (command instanceof HoundifyCommand) {
                    mClientMatches.add(((HoundifyCommand) command).getClientMatch());
                }
                Timber.i("loaded %s command", command.getClass().getCanonicalName());
            }
            /*
            stream(D.getAll(Command.class)).filter(c -> !mCommands.contains(c)).forEach((Command command) -> {
                PluginBus.plug(command);
                mCommands.add(command);
                if (command instanceof HoundifyCommand) {
                    mClientMatches.add(((HoundifyCommand) command).getClientMatch());
                }
                Timber.i("loaded %s command", command.getClass().getCanonicalName());
            });
            */
        }
    }

    private void initializeHoundify() {
        mHoundify = Houndify.get(mAppManager.getAppContext());
        mHoundify.setClientId(mAppManager.getApplicationProperties().getProperty(CLIENT_ID));
        mHoundify.setClientKey(mAppManager.getApplicationProperties().getProperty(CLIENT_KEY));
        mHoundify.setRequestInfoFactory(new RequestInfoFactory());
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
