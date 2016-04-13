# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\johann.reyes\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

# leave stack traces and reflection intact
-dontobfuscate

# Keep all hound stuff - this keeps reflection based code working
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
-keep class com.fasterxml.jackson.databind.SerializationFeature { *; }
-keep class com.fasterxml.jackson.databind.ObjectMapper { *; }

# OkHttp
-dontwarn okio.**

# Otto
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# Retrofit
-keep class retrofit.** { *; }
-keep class package.with.model.classes.** { *; }
-keepclassmembernames interface * {
    @retrofit.http.* <methods>;
}

-dontwarn com.google.appengine.api.urlfetch.**
-dontwarn rx.**