package com.vaporwarecorp.mirror.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.hound.android.fd.DefaultRequestInfoFactory;
import com.hound.android.fd.HoundSearchResult;
import com.hound.android.fd.Houndify;
import com.hound.core.model.sdk.ClientMatch;
import com.hound.core.model.sdk.CommandResult;
import com.hound.core.model.sdk.HoundRequestInfo;
import com.hound.core.model.sdk.HoundResponse;
import com.vaporwarecorp.mirror.command.HoundifyCommand;
import com.vaporwarecorp.mirror.vendor.houndify.HoundifyVoiceSearchActivity;
import com.vaporwarecorp.mirror.vendor.houndify.SimpleAudioByteStreamSource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import timber.log.Timber;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class HoundifyManager extends DefaultRequestInfoFactory {
// ------------------------------ FIELDS ------------------------------

    private static final String CLIENT_ID = "HoundClientId";
    private static final String CLIENT_KEY = "HoundClientKey";

    private List<ClientMatch> mClientMatches;
    private List<HoundifyCommand> mCommands;
    private JsonNode mConversationState;
    private Houndify mHoundify;
    private JsonNodeFactory mNodeFactory;

// --------------------------- CONSTRUCTORS ---------------------------

    public HoundifyManager(Context context, Properties properties) {
        super(context);

        mHoundify = Houndify.get(context);
        mHoundify.setClientId(properties.getProperty(CLIENT_ID));
        mHoundify.setClientKey(properties.getProperty(CLIENT_KEY));
        mHoundify.setAudioInputStreamFactory(SimpleAudioByteStreamSource::new);
        mHoundify.setRequestInfoFactory(this);

        mClientMatches = new LinkedList<>();
        mCommands = new LinkedList<>();
        mNodeFactory = JsonNodeFactory.instance;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface RequestInfoFactory ---------------------

    @NonNull
    @Override
    public HoundRequestInfo create() {
        final HoundRequestInfo requestInfo = super.create();
        requestInfo.setConversationState(mConversationState);
        requestInfo.setClientMatches(mClientMatches);
        return requestInfo;
    }

// -------------------------- OTHER METHODS --------------------------

    public HoundifyCommand findCommand(Class<?> clazz) {
        for (HoundifyCommand command : mCommands) {
            if (clazz.isAssignableFrom(command.getClass())) {
                return command;
            }
        }
        return null;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void processCommand(int resultCode, Intent data, HoundifyManagerListener listener) {
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

            for (HoundifyCommand command : mCommands) {
                if (command.matches(commandResult)) {
                    listener.onSuccess(commandResult, command);
                    return;
                }
            }
        } else if (result.getErrorType() != null && result.getException() != null) {
            Timber.e(result.getException(), "Exception on process command");
            String error = result.getErrorType() + "\n\n" + result.getException().getMessage();
            listener.onError(error);
        } else {
            listener.onError("Aborted search");
        }
    }

    public void registerClientMatch(String intent, String expression, String response) {
        ClientMatch clientMatch = new ClientMatch();
        clientMatch.setExpression(expression);
        clientMatch.setAllResponses(response);
        clientMatch.setResult(mNodeFactory.objectNode().put("Intent", intent));
        mClientMatches.add(clientMatch);
    }

    public void registerCommand(HoundifyCommand command) {
        mCommands.add(command);
    }

    public void voiceSearch(Activity activity) {
        HoundifyVoiceSearchActivity.newInstance(activity);
    }

// -------------------------- INNER CLASSES --------------------------

    public interface HoundifyManagerListener {
        void onSuccess(CommandResult result, HoundifyCommand command);

        void onError(String errorMessage);
    }
}
