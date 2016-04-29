package com.vaporwarecorp.mirror.util;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import java.util.concurrent.TimeUnit;

public class RxUtil {
// -------------------------- STATIC METHODS --------------------------

    public static void delay(Action1<? super Long> action, long delay) {
        Observable.timer(delay, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action);
    }
}
