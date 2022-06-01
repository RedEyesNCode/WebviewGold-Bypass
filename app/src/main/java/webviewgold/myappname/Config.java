package webviewgold.myappname;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Config {
    // ***********************************************************
    // *** THANKS FOR BEING PART OF THE WEBVIEWGOLD COMMUNITY! ***
    // ***********************************************************
    // *** Your Purchase Code of CodeCanyon ***
    // 1. Buy a WebViewGold license (https://www.webviewgold.com/download/android) for each app you publish. If your app is going to be free, a "Regular license" is required. If your app will be sold to your users or if you use the In-App Purchases API, an "Extended license" is required. More info: https://codecanyon.net/licenses/standard?ref=onlineappcreator
    // 2. Grab your Purchase Code (this is how to find it quickly: https://help.market.envato.com/hc/en-us/articles/202822600-Where-Is-My-Purchase-Code-)
    // 3. Great! Just enter it here and restart your app:
    public static final String PURCHASECODE = "ALL-THIS-FOR-4-HEARTS";
    // 4. Enjoy your app! :)

    /**
     * Main Configuration Of Your WebViewGold App
     */
    // Domain host and subdomain without any https:// or http:// prefixes (e.g., "www.example.org")
    public static final String HOST = "www.github.com";

    // Your URL including https:// or http:// prefix and including www. or any required subdomain (e.g., "https://www.example.org")
    public static String HOME_URL = "https://github.com/RedEyesNCode";

    // Set to "false" to disable the progress spinner/loading spinner
    public static final boolean ACTIVATE_PROGRESS_BAR = true;

    // Set a customized UserAgent for WebView URL requests (or leave it empty to use the default Android UserAgent)
    public static final String USER_AGENT = "";

    // Set to "true" if you want to extend URL request by the system language like ?webview_language=LANGUAGE CODE (e.g., ?webview_language=EN for English users)
    public static final boolean APPEND_LANG_CODE = false;

    // Set to "true" if you want to use the "local-html" folder fallback if the user is offline
    public static final boolean FALLBACK_USE_LOCAL_HTML_FOLDER_IF_OFFLINE = false;

    // Set to "true" to open external links in another browser by default
    public static final boolean OPEN_EXTERNAL_URLS_IN_ANOTHER_BROWSER = false;

    // Set to "true" to open links with attributes (_blank, _self) in new a tab by default
    public static final boolean OPEN_SPECIAL_URLS_IN_NEW_TAB = true;

    // Set to "true" to clear the WebView cache on each app startup and do not use cached versions of your web app/website
    public static final boolean CLEAR_CACHE_ON_STARTUP = false;

    //Set to "true" to use local "assets/index.html" file instead of URL
    public static final boolean USE_LOCAL_HTML_FOLDER = false;

    //Set to "true" to enable deep linking for App Links & Push (take a look at the documentation for further information)
    public static final boolean IS_DEEP_LINKING_ENABLED = true;

    //Set to "true" to open the notification deep linking URLs in the system browser instead of your app
    public static final boolean OPEN_NOTIFICATION_URLS_IN_SYSTEM_BROWSER = false;

    // Set to "true" to activate the splash screen
    public static final boolean SPLASH_SCREEN_ACTIVATED = true;

    //Set the splash screen timeout time in milliseconds
    public static final int SPLASH_TIMEOUT = 2000;

    //Set the splash screen image size with respect to device smallest width/height; range in percentage [0-100]; Caution: value  = 0 will hide the image completely
    public static final double SCALE_SPLASH_IMAGE = 25;

    //Status Bar Dark/Light Mode; Set to "true" for dark status bar text; use it in combination with 'colorPrimaryDark' in style.xml
    static boolean darkStatusBarText = false;

    //Set to "true" to prevent the device from going into sleep while the app is active
    public static final boolean PREVENT_SLEEP = false;

    //Set to "true" to close the app by pressing the hardware back button (instead of going back to the last page)
    public static final boolean EXIT_APP_BY_BACK_BUTTON_ALWAYS = false;
    //Set to "true" to close the app by pressing the hardware back button if the user is on the home page (which does not allow going to a prior page)
    public static final boolean EXIT_APP_BY_BACK_BUTTON_HOMEPAGE = true;

    /**
     * Dialog Options
     */
    public static boolean SHOW_FIRSTRUN_DIALOG = true; //Set to false to disable the First Run Dialog
    public static boolean SHOW_FACEBOOK_DIALOG = true; //Set to false to disable the Follow On Facebook Dialog
    public static boolean SHOW_RATE_DIALOG = true; //Set to false to disable the Rate This App Dialog

    // Set the minimum number of days to be passed after the application is installed before the "Rate this app" dialog is displayed
    public static final int RATE_DAYS_UNTIL_PROMPT = 3;
    // Set the minimum number of application launches before the "Rate this app" dialog will be displayed
    public static final int RATE_LAUNCHES_UNTIL_PROMPT = 3;

    // Set the minimum number of days to be passed after the application is installed before the "Follow on Facebook" dialog is displayed
    public static final int FACEBOOK_DAYS_UNTIL_PROMPT = 2;
    // Set the minimum number of application launches before the "Rate this app" dialog will be displayed
    public static final int FACEBOOK_LAUNCHES_UNTIL_PROMPT = 4;
    // Set the URL of your Facebook page
    public static final String FACEBOOK_URL = "https://www.facebook.com/OnlineAppCreator/";


    /**
     * OneSignal Push Notification Options
     */
    //Set to "true" to activate OneSignal Push (set OneSignal IDs in the build.gradle file)
    public static final boolean PUSH_ENABLED = false;

    //Set to "true" if you want to extend URL request by ?onesignal_push_id=XYZ (set the OneSignal IDs in the build.gradle file)
    public static final boolean PUSH_ENHANCE_WEBVIEW_URL = false;

    //Set to "true" if WebView should be reloaded when the app gets a UserID from OneSignal (set the OneSignal IDs in the build.gradle file)
    public static final boolean PUSH_RELOAD_ON_USERID = false;

    /**
     * Firebase Push Notification Options
     */
    //Set to "true" to activate Firebase Push (download the google-services.json file and replace the existing one via Mac Finder/Windows Explorer)
    public static final boolean FIREBASE_PUSH_ENABLED = false;

    public static final String firebasechanneltopic = "NONE"; //Topic name of Firebase channel

    //Set to "true" if you want to extend URL request by ?firebase_push_id=XYZ (set the OneSignal IDs in the build.gradle file)
    public static final boolean FIREBASE_PUSH_ENHANCE_WEBVIEW_URL = false;



    /**
     * Ad Options
     */
    //Set to "true" if you want to display AdMob banner ads (set the AdMob IDs in the strings.xml file)
    public static final boolean SHOW_BANNER_AD = false;

    //Set to "true" if you want to display AdMob fullscreen interstitial ads after X website clicks (set the AdMob IDs in the strings.xml file)
    public static final boolean SHOW_FULL_SCREEN_AD = false;

    //X website clicks for AdMob interstitial ads (set the AdMob IDs in the strings.xml file)
    public static final int SHOW_AD_AFTER_X = 5;

    //Set to "true" to hide the AdMob ads after a successful In-App Purchase
    public static boolean HIDE_ADS_FOR_PURCHASE = false;

    //Set to "true" to use Facebook ads instead of AdMob ads (set the Ad IDs in the strings.xml file)
    public static boolean USE_FACEBOOK_ADS = false;

    //Add the file formats that should trigger the file downloader functionality
    public static List<String> downloadableExtension =
            Collections.unmodifiableList(
                    Arrays.asList(".epub", ".pdf", ".pptx", ".docx", ".doc", ".xlsx", ".mp3", ".mp4", ".wav") //Add them here!
            );

    /**
     * Android Permission Options
     */
    static boolean requireLocation = true; //Set to "false" if you do NOT require location services/GPS coordinates
    static boolean requireStorage = true; //Set to "false" if you do NOT require APIs related to downloads or uploads
    static boolean requireCamera = true; //Set to "false" if you do NOT require APIs related to camera images / camera videos
    static boolean requireRecordAudio = true; //Set to "false" if you do NOT require APIs related to recording audio



}