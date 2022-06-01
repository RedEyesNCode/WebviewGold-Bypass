package webviewgold.myappname;

import android.app.Application;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.onesignal.OneSignal;

public class WebViewApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        if (Config.PUSH_ENABLED) {
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.NONE);
            OneSignal.startInit(this)
                    .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                    .setNotificationOpenedHandler(new MyNotificationOpenedHandler(this))
                    .unsubscribeWhenNotificationsAreDisabled(true)
                    .init();

        }


        if ((Config.SHOW_BANNER_AD) || (Config.SHOW_FULL_SCREEN_AD)) {
            if (Config.USE_FACEBOOK_ADS) {
                AudienceNetworkAds.initialize(this);
                AdSettings.addTestDevice("bf26e52d-43b9-4814-99ee-2b82136d7077");
            } else {
                MobileAds.initialize(this, String.valueOf(R.string.admob_app_id));
            }
        }
    }
}