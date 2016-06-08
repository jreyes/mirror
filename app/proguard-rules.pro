# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\servers\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn android.support.**
-dontwarn com.google.android.**
-dontwarn okhttp3.**
-dontwarn sun.net.spi.**
-dontwarn sun.misc.**
-dontwarn sun.nio.**
-dontwarn java.nio.file.**

-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes SourceFile
-keepattributes LineNumberTable
-keepattributes Signature
-keepattributes InnetClasses

# Keep all hound stuff - this keeps reflection based code working
-dontwarn com.hound.android.**
-keep class com.hound.android.** { *; }

# keep JNI stuff untouched
-keep class com.soundhound.android.libvad.** { *; }
-keep class com.soundhound.android.libspeex.** { *; }
-keep class com.hound.android.libphs.** { *; }

# Google Play Services
# http://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Jackson
-keep class com.fasterxml.jackson.databind.ObjectMapper { *; }
-keep class com.fasterxml.jackson.databind.ObjectReader { *; }
-keep class com.fasterxml.jackson.databind.ObjectWriter { *; }
-keep class com.fasterxml.jackson.databind.SerializationFeature { *; }

# OkHttp
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

# Otto
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# Retrofit
-dontwarn retrofit.**
-dontwarn rx.**
-keep class retrofit.** { *; }
-keep class com.varporwarecorp.mirror.component.forecast.model.** { *; }
-keepclassmembernames interface * {
    @retrofit.http.* <methods>;
}

# PocketSphinx
-dontwarn edu.cmu.pocketsphinx.**
-keep class edu.cmu.pocketsphinx.** { *; }

# PLDroid
-dontwarn tv.danmaku.ijk.**
-keep class com.pili.pldroid.player.** { *; }
-keep class tv.danmaku.ijk.** { *; }

# RxJava
-dontwarn rx.**