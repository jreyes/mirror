/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vaporwarecorp.mirror.feature.twilio;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.FeatureFragment;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.twilio.conversations.*;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.util.FullScreenUtil;
import timber.log.Timber;

import static com.twilio.conversations.CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA;

@Plugin
@Provides(TwilioView.class)
public class TwilioFragment extends FeatureFragment<TwilioPresenter> implements TwilioView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    TwilioPresenter mPresenter;

    private CameraCapturer mCameraCapturer;
    private Conversation mConversation;
    private SweetAlertDialog mIncomingInviteDialog;
    private ViewGroup mLocalContainer;
    private VideoViewRenderer mLocalVideoRenderer;
    private OutgoingInvite mOutgoingInvite;
    private ViewGroup mParticipantContainer;
    private VideoViewRenderer mParticipantVideoRenderer;
    private boolean mPauseVideo;
    private FrameLayout mPreviewFrameLayout;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void onCenterDisplay() {
        if (mPauseVideo) {
            mPauseVideo = false;
            pauseVideo(false);
        }
    }

    @Override
    public void onSideDisplay() {
        if (!mPauseVideo) {
            mPauseVideo = true;
            pauseVideo(true);
        }
    }

    @Override
    public Class presenterClass() {
        return TwilioPresenter.class;
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public TwilioPresenter getPresenter() {
        return mPresenter;
    }

// --------------------- Interface TwilioView ---------------------

    @Override
    public void displayInvite(final IncomingInvite incomingInvite) {
        mIncomingInviteDialog = new SweetAlertDialog(getActivity())
                .setTitleText(getString(R.string.conversation_title))
                .setContentText(getString(R.string.conversation_message, incomingInvite.getInviter()))
                .setConfirmText(getString(R.string.conversation_accept))
                .setConfirmClickListener(acceptCallClickListener(incomingInvite))
                .setCancelText(getString(R.string.conversation_reject))
                .setCancelClickListener(rejectCallClickListener(incomingInvite));
        FullScreenUtil.onResume(mIncomingInviteDialog);
        mIncomingInviteDialog.show();
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle inState) {
        return inflater.inflate(R.layout.fragment_twilio, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        FullScreenUtil.onResume(getActivity());
    }

    @Override
    public void onStop() {
        hangUp();
        super.onStop();
    }

    @Override
    protected void onCreateBindings() {
        super.onCreateBindings();

        mPreviewFrameLayout = getView(R.id.preview_frame_layout);
        mLocalContainer = getView(R.id.local_container);
        mParticipantContainer = getView(R.id.participant_container);

        FloatingActionButton actionFab = getView(R.id.call_action_fab);
        actionFab.setOnClickListener(hangUpClickListener());
    }

    private OnSweetClickListener acceptCallClickListener(final IncomingInvite invite) {
        return (dialog) -> {
            dialog.dismissWithAnimation();

            displayRootView();

            invite.accept(setupLocalMedia(), (conversation, e) -> {
                if (e == null) {
                    Timber.i("Accepted conversation invite");
                    mConversation = conversation;
                    conversation.setConversationListener(conversationListener());
                } else {
                    Timber.e(e, "Error accepting the conversation invite");
                    getPresenter().rejectCall(invite);
                }
            });
        };
    }

    /**
     * CameraCapture error listener
     */
    private CapturerErrorListener capturerErrorListener() {
        return e -> Timber.e("Camera capturer error: " + e.getMessage());
    }

    /**
     * Conversation Listener
     */
    private Conversation.Listener conversationListener() {
        return new Conversation.Listener() {
            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {
                Timber.d("onParticipantDisconnected %s", participant.getIdentity());
                participant.setParticipantListener(participantListener());
            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation,
                                                     Participant participant,
                                                     TwilioConversationsException e) {
                Timber.e(e, "onFailedToConnectParticipant %s", participant.getIdentity());
            }

            @Override
            public void onParticipantDisconnected(Conversation conversation,
                                                  Participant participant) {
                Timber.d("onParticipantDisconnected %s", participant.getIdentity());
            }

            @Override
            public void onConversationEnded(Conversation conversation,
                                            TwilioConversationsException e) {
                Timber.e(e, "onConversationEnded");
                // now let's remove this view from the framework
                getPresenter().endCall();
            }
        };
    }

    private void displayRootView() {
        if (getView() != null && getView().getVisibility() != View.VISIBLE) {
            getView().setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hang up the current call
     */
    private void hangUp() {
        // let's dismiss the dialog if still open
        if (mIncomingInviteDialog != null) {
            mIncomingInviteDialog.cancel();
            mIncomingInviteDialog = null;
        }

        // let's disconnect the conversation or cancel the outgoing invite
        if (mConversation != null) {
            mConversation.setConversationListener(null);
            mConversation.disconnect();
        } else if (mOutgoingInvite != null) {
            mOutgoingInvite.cancel();
        }

        // Teardown preview
        if (mCameraCapturer != null) {
            if (mCameraCapturer.isPreviewing()) {
                mCameraCapturer.stopPreview();
            }
            mCameraCapturer = null;
        }

        // release the Conversation and OutgoingInvite object
        mConversation = null;
        mOutgoingInvite = null;
    }

    private OnClickListener hangUpClickListener() {
        return v -> mPresenter.endCall();
    }

    /**
     * LocalMedia listener
     */
    private LocalMedia.Listener localMediaListener() {
        return new LocalMedia.Listener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia,
                                               LocalVideoTrack localVideoTrack) {
                Timber.d("onLocalVideoTrackAdded");
                mLocalVideoRenderer = new VideoViewRenderer(getActivity(), mLocalContainer);
                mLocalVideoRenderer.applyZOrder(true);
                localVideoTrack.addRenderer(mLocalVideoRenderer);
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia,
                                                 LocalVideoTrack localVideoTrack) {
                Timber.d("onLocalVideoTrackRemoved");
                mLocalContainer.removeAllViews();
                mLocalVideoRenderer.release();
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia,
                                               LocalVideoTrack localVideoTrack,
                                               TwilioConversationsException e) {
                Timber.e(e, "onLocalVideoTrackError");
            }
        };
    }

    /**
     * Participant listener
     */
    private Participant.Listener participantListener() {
        return new Participant.Listener() {
            @Override
            public void onVideoTrackAdded(Conversation conversation,
                                          Participant participant,
                                          VideoTrack videoTrack) {
                Timber.d("onVideoTrackAdded %s", participant.getIdentity());

                // Remote participant
                mParticipantVideoRenderer = new VideoViewRenderer(getActivity(), mParticipantContainer);

                // Scale the remote video to fill the view group
                mParticipantVideoRenderer.setVideoScaleType(VideoScaleType.ASPECT_FILL);
                videoTrack.addRenderer(mParticipantVideoRenderer);
            }

            @Override
            public void onVideoTrackRemoved(Conversation conversation,
                                            Participant participant,
                                            VideoTrack videoTrack) {
                Timber.d("onVideoTrackRemoved %s", participant.getIdentity());
                mParticipantContainer.removeAllViews();
                mParticipantVideoRenderer.release();
            }

            @Override
            public void onAudioTrackAdded(Conversation conversation,
                                          Participant participant,
                                          AudioTrack audioTrack) {
                Timber.i("onAudioTrackAdded %s", participant.getIdentity());
            }

            @Override
            public void onAudioTrackRemoved(Conversation conversation,
                                            Participant participant,
                                            AudioTrack audioTrack) {
                Timber.i("onAudioTrackRemoved %s", participant.getIdentity());
            }

            @Override
            public void onTrackEnabled(Conversation conversation,
                                       Participant participant,
                                       MediaTrack mediaTrack) {
                Timber.i("onTrackEnabled %s", participant.getIdentity());
            }

            @Override
            public void onTrackDisabled(Conversation conversation,
                                        Participant participant,
                                        MediaTrack mediaTrack) {
                Timber.i("onTrackDisabled %s", participant.getIdentity());
            }
        };
    }

    private boolean pauseVideo(boolean pauseVideo) {
        /*
         * Enable/disable local video track
         */
        if (mConversation != null) {
            LocalVideoTrack videoTrack = mConversation.getLocalMedia().getLocalVideoTracks().get(0);
            if (videoTrack != null) {
                return videoTrack.enable(!pauseVideo);
            }
            mConversation.getLocalMedia().mute(pauseVideo);
        }
        return false;
    }

    private OnSweetClickListener rejectCallClickListener(final IncomingInvite incomingInvite) {
        return (dialog) -> {
            dialog.cancel();
            incomingInvite.reject();
        };
    }

    private LocalMedia setupLocalMedia() {
        mCameraCapturer = CameraCapturer.create(getActivity(), CAMERA_SOURCE_FRONT_CAMERA, capturerErrorListener());
        mCameraCapturer.startPreview(mPreviewFrameLayout);

        LocalMedia localMedia = new LocalMedia(localMediaListener());
        LocalVideoTrack localVideoTrack = new LocalVideoTrack(mCameraCapturer);
        localMedia.addLocalVideoTrack(localVideoTrack);
        return localMedia;
    }
}
