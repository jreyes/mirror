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
package com.vaporwarecorp.mirror.component.command;

import android.app.Activity;
import android.content.Intent;
import com.hound.android.fd.Houndify;
import com.vaporwarecorp.mirror.util.FullScreenUtil;

public class HoundifyVoiceSearchActivity
        extends com.hound.android.fd.HoundifyVoiceSearchActivity {
// ------------------------------ FIELDS ------------------------------

    private static final String SAFE_TOKEN = "you_must_start_with_me";

// -------------------------- STATIC METHODS --------------------------

    public static void newInstance(Activity context) {
        final Intent intent = new Intent(context, HoundifyVoiceSearchActivity.class);
        intent.putExtra(SAFE_TOKEN, true);
        context.startActivityForResult(intent, Houndify.REQUEST_CODE);
        context.overridePendingTransition(com.hound.android.voicesdk.R.anim.houndify_search_enter, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FullScreenUtil.onResume(this);
    }
}
