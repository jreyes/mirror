package com.vaporwarecorp.mirror.command.music;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

public class MusicPresenter extends AbstractDetailsDescriptionPresenter {
    @Override
    protected void onBindDescription(ViewHolder vh, Object item) {
        if (item == null) {
            return;
        }
        vh.getTitle().setText("Some title");
        vh.getSubtitle().setText("some subtitle");
        vh.getBody().setText("Some body text");
    }
}
