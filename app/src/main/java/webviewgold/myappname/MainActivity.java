package webviewgold.myappname;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.blikoon.qrcodescanner.QrCodeActivity;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OSSubscriptionObserver;
import com.onesignal.OSSubscriptionStateChanges;
import com.onesignal.OneSignal;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import static webviewgold.myappname.Config.ACTIVATE_PROGRESS_BAR;
import static webviewgold.myappname.Config.HIDE_ADS_FOR_PURCHASE;
import static webviewgold.myappname.Config.OPEN_SPECIAL_URLS_IN_NEW_TAB;
import static webviewgold.myappname.Config.SPLASH_SCREEN_ACTIVATED;
import static webviewgold.myappname.Config.downloadableExtension;

public class MainActivity extends AppCompatActivity
        implements OSSubscriptionObserver,
        PurchasesUpdatedListener {

    private static final String INDEX_FILE = "file:///android_asset/local-html/index.html";
    private static final int CODE_AUDIO_CHOOSER = 5678;
    private WebView webView;
    private View offlineLayout;

    public static final int REQUEST_CODE_QR_SCAN = 1234;

    private AdView mAdView;
    private LinearLayout facebookBannerContainer;
    private com.facebook.ads.AdView facebookAdView;
    InterstitialAd mInterstitialAd;
    com.facebook.ads.InterstitialAd facebookInterstitialAd;
    public int webViewCount = 0;
    public static final int MULTIPLE_PERMISSIONS = 10;
    public ProgressBar progressBar;
    private String deepLinkingURL;
    private BillingClient billingClient;
    private Button btnCheckPermission;

    private static final String TAG = ">>>>>>>>>>>";
    private String mCM, mVM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR = 1;
    public String hostpart;
    private boolean disableAdMob = false;
    private String successUrl = "", failUrl = "";
    private FrameLayout adLayout;
    private boolean offlineFileLoaded = false;
    private boolean isNotificationURL = false;
    private boolean extendediap = true;
    public String uuid = "";
    public static Context mContext;
    private String firebaseUserToken = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        mContext = this;
        uuid = Settings.System.getString(super.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Config.darkStatusBarText) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        if (Config.PREVENT_SLEEP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        super.onCreate(savedInstanceState);
        if (SPLASH_SCREEN_ACTIVATED) {
            startActivity(new Intent(getApplicationContext(), SplashScreen.class));
        }
        setContentView(R.layout.activity_main);
        verifyStoragePermission(this);
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        if (Build.VERSION.SDK_INT > 23) {
//            builder.detectFileUriExposure();
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED) {
//            // Permission is granted
//        }
//        else {
//            //Permission is not granted so you have to request it
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    888);
//        }

        if (Config.FIREBASE_PUSH_ENABLED) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            String token = task.getResult().getToken();
                            firebaseUserToken = token;
                            AlertManager.updateFirebaseToken(MainActivity.this, token);
                            Log.d(TAG, token);
                        }
                    });
        }

        RelativeLayout main = findViewById(R.id.main);
        adLayout = findViewById(R.id.ad_layout);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP,
                            (billingResult1, purchasesList) -> {

                                Log.i(TAG, "is purchased : " + (purchasesList != null && !purchasesList.isEmpty()));

                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK
                                        && purchasesList != null && !purchasesList.isEmpty()) {

                                    boolean productFound = true;
                                    if (productFound) {
                                        Log.i(TAG, "purchased " + String.valueOf(true));
                                        HIDE_ADS_FOR_PURCHASE = true;
                                        AlertManager.purchaseState(getApplicationContext(), true);
                                        if (AlertManager.isPurchased(getApplicationContext())) {
                                            HIDE_ADS_FOR_PURCHASE = true;
                                        }
                                    } else {
                                        Log.i(TAG, "purchased " + String.valueOf(false));
                                        HIDE_ADS_FOR_PURCHASE = false;
                                        AlertManager.purchaseState(getApplicationContext(), false);
                                        if (AlertManager.isPurchased(getApplicationContext())) {
                                            HIDE_ADS_FOR_PURCHASE = true;
                                        }
                                    }
                                } else {
                                    Log.i(TAG, "purchased " + String.valueOf(false));
                                    HIDE_ADS_FOR_PURCHASE = false;
                                    AlertManager.purchaseState(getApplicationContext(), false);
                                    if (AlertManager.isPurchased(getApplicationContext())) {
                                        HIDE_ADS_FOR_PURCHASE = true;
                                    }
                                }
                            });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null && (intent.getData().getScheme().equals("http"))) {
            Uri data = intent.getData();
            List<String> pathSegments = data.getPathSegments();
            if (pathSegments.size() > 0) {
                deepLinkingURL = pathSegments.get(0).substring(5);
                String fulldeeplinkingurl = data.getPath().toString();
                fulldeeplinkingurl = fulldeeplinkingurl.replace("/link=", "");
                deepLinkingURL = fulldeeplinkingurl;
            }
        } else if (intent != null && intent.getData() != null && (intent.getData().getScheme().equals("https"))) {
            Uri data = intent.getData();
            List<String> pathSegments = data.getPathSegments();
            if (pathSegments.size() > 0) {
                deepLinkingURL = pathSegments.get(0).substring(5);
                String fulldeeplinkingurl = data.getPath().toString();
                fulldeeplinkingurl = fulldeeplinkingurl.replace("/link=", "");
                deepLinkingURL = fulldeeplinkingurl;
            }
        }

        if (intent != null) {
            Bundle extras = getIntent().getExtras();
            String URL = null;
            if (extras != null) {
                URL = extras.getString("ONESIGNAL_URL");
            }
            if (URL != null && !URL.equalsIgnoreCase("")) {
                isNotificationURL = true;
                deepLinkingURL = URL;
            } else isNotificationURL = false;
        }


        webViewCount = 0;
        final String myOSurl = Config.PURCHASECODE;

        if (Config.PUSH_ENABLED) {
            OneSignal.addSubscriptionObserver(this);
        }

        if (savedInstanceState == null) {
            AlertManager.appLaunched(this);
        }

        mAdView = findViewById(R.id.adView);
        if (Config.USE_FACEBOOK_ADS) {
            Log.e(TAG, "attemptign to create ad");
            facebookAdView = new com.facebook.ads.AdView(this,
                    getString(R.string.facebook_banner_footer),
                    AdSize.BANNER_HEIGHT_50);
        }

        AdRequest adRequest = new AdRequest.Builder()
                .build();
        btnCheckPermission = findViewById(R.id.btnCheckPermission);

        btnCheckPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO);
                int checkVal = checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO);
                if(checkVal==PackageManager.PERMISSION_GRANTED){
                    Snackbar.make(view,"PERMISSION GRANTED",Snackbar.LENGTH_SHORT).show();
                }else {
                    Snackbar.make(view,"PERMISSION DENIED",Snackbar.LENGTH_SHORT).show();

                }

            }
        });

//
//        if (BuildConfig.IS_DEBUG_MODE) {
//            osURL(myOSurl);
//        }


        if (Config.SHOW_BANNER_AD && !HIDE_ADS_FOR_PURCHASE) {
            if (Config.USE_FACEBOOK_ADS) {
                adLayout.removeAllViews();
                adLayout.addView(facebookAdView);
                adLayout.setVisibility(View.VISIBLE);
                facebookAdView.loadAd();
            } else {
                mAdView.loadAd(adRequest);
                adLayout.setVisibility(View.VISIBLE);
                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        if (!HIDE_ADS_FOR_PURCHASE) {
                            mAdView.setVisibility(View.VISIBLE);
                            adLayout.setVisibility(View.VISIBLE);
                        } else {
                            mAdView.setVisibility(View.GONE);
                            adLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                    }

                    @Override
                    public void onAdOpened() {
                        if (!HIDE_ADS_FOR_PURCHASE) {
                            mAdView.setVisibility(View.VISIBLE);
                            adLayout.setVisibility(View.VISIBLE);
                        } else {
                            mAdView.setVisibility(View.GONE);
                            adLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAdLeftApplication() {
                    }

                    @Override
                    public void onAdClosed() {
                    }
                });
            }
        } else {
            mAdView.setVisibility(View.GONE);
            adLayout.setVisibility(View.GONE);
        }

        if (!HIDE_ADS_FOR_PURCHASE) {
            if (Config.USE_FACEBOOK_ADS) {
                facebookInterstitialAd = new com.facebook.ads.InterstitialAd(this, getString(R.string.facebook_interstitial_full_screen));
                com.facebook.ads.InterstitialAdListener interstitialAdListener = new com.facebook.ads.InterstitialAdListener() {
                    @Override
                    public void onInterstitialDisplayed(com.facebook.ads.Ad ad) {
                        // Interstitial ad displayed callback
                        Log.e(TAG, "Interstitial ad displayed.");
                    }

                    @Override
                    public void onInterstitialDismissed(com.facebook.ads.Ad ad) {
                        // Interstitial dismissed callback
                        Log.e(TAG, "Interstitial ad dismissed.");
                    }

                    @Override
                    public void onError(com.facebook.ads.Ad ad, com.facebook.ads.AdError adError) {
                        // Ad error callback
                        Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
                    }

                    @Override
                    public void onAdLoaded(com.facebook.ads.Ad ad) {
                        // Interstitial ad is loaded and ready to be displayed
                        Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                        showInterstitial();
                    }

                    @Override
                    public void onAdClicked(com.facebook.ads.Ad ad) {
                        // Ad clicked callback
                        Log.d(TAG, "Interstitial ad clicked!");
                    }

                    @Override
                    public void onLoggingImpression(com.facebook.ads.Ad ad) {
                        // Ad impression logged callback
                        Log.d(TAG, "Interstitial ad impression logged!");
                    }
                };

                // For auto play video ads, it's recommended to load the ad
                // at least 30 seconds before it is shown
                facebookInterstitialAd.loadAd(
                        facebookInterstitialAd.buildLoadAdConfig()
                                .withAdListener(interstitialAdListener)
                                .build());
            } else {
                mInterstitialAd = new InterstitialAd(this);


                mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

                mInterstitialAd.setAdListener(new AdListener() {
                    public void onAdLoaded() {

                        showInterstitial();
                    }

                });
            }
        }

        webView = findViewById(R.id.webView);
        offlineLayout = findViewById(R.id.offline_layout);

        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

        final Button tryAgainButton = findViewById(R.id.try_again_button);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Try again!");
                webView.setVisibility(View.GONE);
                loadMainUrl();

            }
        });

        webView.setWebViewClient(new MyWebViewClient() {
            private Handler notificationHandler;

            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String url) {
                if (Config.FALLBACK_USE_LOCAL_HTML_FOLDER_IF_OFFLINE) {
                    loadLocal(INDEX_FILE);
                } else {
                    webView.setVisibility(View.GONE);
                    offlineLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                //Basic Overriding part here (1/2)
                Log.e(TAG, "should override (1/2): " + url);

                if (url.startsWith("mailto:")) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                    return true;
                }
                if (url.startsWith("share:") || url.contains("api.whatsapp.com")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("whatsapp:")) {
                    Intent i = new Intent();
                    i.setPackage("com.whatsapp");
                    i.setAction(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("geo:") || url.contains("maps:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("market:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("maps.app.goo.gl")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.contains("maps.google.com")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("intent:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("tel:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("sms:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("play.google.com")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }


                if (OPEN_SPECIAL_URLS_IN_NEW_TAB) {
                    WebView.HitTestResult result = view.getHitTestResult();
                    String data = result.getExtra();
                    Log.i(TAG, " data :" + data);
                    if ((data != null && data.endsWith("#")) || url.startsWith("newtab:")) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
                        CustomTabsIntent customTabsIntent = builder.build();
                        String finalUrl = url;

                        if (url.startsWith("newtab:")) {
                            finalUrl = url.substring(7);
                        }

                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(finalUrl));
                        webView.stopLoading();
                        return false;
                    }
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        webView.getSettings().setSupportMultipleWindows(true);


        webView.setWebChromeClient(new MyWebChromeClient() {
            private Handler notificationHandler;


            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                Log.i(TAG, "onCloseWindow url " + window.getUrl());
                Log.i(TAG, "onCloseWindow url " + window.getOriginalUrl());
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {

                Bundle extras = getIntent().getExtras();
                String URL = null;
                if (extras != null) {
                    URL = extras.getString("ONESIGNAL_URL");
                }
                if (URL != null && !URL.equalsIgnoreCase("")) {
                    isNotificationURL = true;
                    deepLinkingURL = URL;
                } else isNotificationURL = false;

                Log.i(TAG, " LOG24 " + deepLinkingURL);

                if (!OPEN_SPECIAL_URLS_IN_NEW_TAB) {
                    Log.i(TAG, "if ");
                    WebView.HitTestResult result = view.getHitTestResult();
                    String data = result.getExtra();
                    Context context = view.getContext();
                    if (data == null) {
                        Log.i(TAG, "else true ");
                        WebView newWebView = new WebView(view.getContext());
                        newWebView.setWebChromeClient(new MyWebChromeClient());
                        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                        transport.setWebView(newWebView);
                        resultMsg.sendToTarget();
                        return true;
                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                        context.startActivity(browserIntent);
                    }
                } else {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
                    CustomTabsIntent customTabsIntent = builder.build();
                    WebView.HitTestResult result = view.getHitTestResult();
                    String data = result.getExtra();
                    Log.i("TAG", " data " + data);
                    String url = "";
                    WebView newWebView = new WebView(view.getContext());
                    newWebView.setWebChromeClient(new WebChromeClient());
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(newWebView);
                    resultMsg.sendToTarget();

                }
                Log.i("TAG", " running this main activity ");
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.i(TAG, " onJsalert");
                return super.onJsAlert(view, url, message, result);
            }

            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "Upload"), FCR);
            }

            @SuppressLint("InlinedApi")
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                if (Config.requireStorage && Config.requireCamera) {
                    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, perms, FCR);

                    } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FCR);

                    } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, FCR);
                    } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, FCR);
                    }

                    if (mUMA != null) {
                        mUMA.onReceiveValue(null);
                    }
                    mUMA = filePathCallback;

                    if (Arrays.asList(fileChooserParams.getAcceptTypes()).contains("audio/*")) {
                        Intent chooserIntent = fileChooserParams.createIntent();
                        startActivityForResult(chooserIntent, CODE_AUDIO_CHOOSER);
                        return true;
                    }

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCM);
                        } catch (IOException ex) {
                            Log.e(TAG, "Image file creation failed", ex);
                        }
                        if (photoFile != null) {
                            mCM = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (takeVideoIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        File videoFile = null;
                        try {
                            videoFile = createVideoFile();
                            takeVideoIntent.putExtra("PhotoPath", mVM);
                        } catch (IOException ex) {
                            Log.e(TAG, "Video file creation failed", ex);
                        }
                        if (videoFile != null) {
                            mVM = "file:" + videoFile.getAbsolutePath();
                            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", videoFile));
                        } else {
                            takeVideoIntent = null;
                        }
                    }

                    Intent contentSelectionIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    contentSelectionIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/* video/*");
                    contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});

                    Intent[] intentArray;
                    if (takePictureIntent != null && takeVideoIntent != null) {
                        intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
                    } else if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else if (takeVideoIntent != null) {
                        intentArray = new Intent[]{takeVideoIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

//                    Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
//                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Upload");
//                    chooserIntent.addCategory(Intent.CATEGORY_OPENABLE);
//                    chooserIntent.setType("image/*");
//                    chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
//                    startActivityForResult(chooserIntent, FCR);

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Upload");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooserIntent, FCR);
                }
                return true;
            }
        });


        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        registerForContextMenu(webView);

        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Config.CLEAR_CACHE_ON_STARTUP) {
            webSettings.setAppCacheEnabled(false);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            webSettings.setAppCacheEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        //webSettings.setLoadWithOverviewMode(true);
        //webSettings.setUseWideViewPort(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        if (!Config.USER_AGENT.isEmpty()) {
            webSettings.setUserAgentString(Config.USER_AGENT);
        }

        if (Config.CLEAR_CACHE_ON_STARTUP) {
            webView.clearCache(true);
        }

        if (Config.USE_LOCAL_HTML_FOLDER) {
            loadLocal(INDEX_FILE);
        } else if (isConnectedNetwork()) {
            if (Config.USE_LOCAL_HTML_FOLDER) {
                loadLocal(INDEX_FILE);
            } else {
                loadMainUrl();
            }
        } else {
            loadLocal(INDEX_FILE);
        }

        askForPermission();
        if(!connectedNow){
            checkInternetConnection();
        }
    }

    private  static boolean connectedNow = false;
    private void checkInternetConnection(){
        //auto reload every 5s
        class AutoRec extends TimerTask {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {

                        if(!isConnectedNetwork())
                        {
                            connectedNow = false;
                            offlineLayout.setVisibility(View.VISIBLE);
                            System.out.println("attempting reconnect");
                            webView.setVisibility(View.GONE);
                            loadMainUrl();
                            Log.d("", "reconnect");
                        }
                        else
                        {
                            if(!connectedNow)
                            {
                                Log.d("", "connected");
                                System.out.println("Try again!");
                                webView.setVisibility(View.GONE);
                                loadMainUrl();
                                connectedNow = true;
                                 if(timer != null) timer.cancel();

                            }
                        }
                    }
                });

            }
        }
        timer.schedule(new AutoRec(), 0, 5000);
        //timer.cancel();
    }


    public static void setAutoOrientationEnabled(Context context, boolean enabled) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }


    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void loadLocal(String path) {
        webView.loadUrl(path);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final WebView.HitTestResult webViewHitTestResult = webView.getHitTestResult();

        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            menu.setHeaderTitle("Download images");
            menu.add(0, 1, 0, "Download the image")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String DownloadImageURL = webViewHitTestResult.getExtra();
                        if (URLUtil.isValidUrl(DownloadImageURL)) {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            downloadManager.enqueue(request);
                            Toast.makeText(MainActivity.this, "Image downloaded successfully.", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Sorry...something went wrong.", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Uri[] results = null;
        Uri uri = null;
        if (requestCode == FCR) {
            if (resultCode == Activity.RESULT_OK) {
                if (mUMA == null) {
                    return;
                }
                if (intent == null || intent.getData() == null) {

                    if (intent != null && intent.getClipData() != null) {

                        int count = intent.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                        results = new Uri[intent.getClipData().getItemCount()];
                        for (int i = 0; i < count; i++) {
                            uri = intent.getClipData().getItemAt(i).getUri();
                            // results = new Uri[]{Uri.parse(mCM)};
                            results[i] = uri;

                        }
                        //do something with the image (save it to some directory or whatever you need to do with it here)
                    }

                    if (mCM != null) {
                        File file = new File(Uri.parse(mCM).getPath());
                        if (file.length() > 0)
                            results = new Uri[]{Uri.parse(mCM)};
                        else
                            file.delete();
                    }
                    if (mVM != null) {
                        File file = new File(Uri.parse(mVM).getPath());
                        if (file.length() > 0)
                            results = new Uri[]{Uri.parse(mVM)};
                        else
                            file.delete();
                    }

                } else {
                    String dataString = intent.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    } else {
                        if (intent.getClipData() != null) {
                            final int numSelectedFiles = intent.getClipData().getItemCount();
                            results = new Uri[numSelectedFiles];
                            for (int i = 0; i < numSelectedFiles; i++) {
                                results[i] = intent.getClipData().getItemAt(i).getUri();
                            }
                        }

                    }
                }
            } else {
                if (mCM != null) {
                    File file = new File(Uri.parse(mCM).getPath());
                    if (file != null) file.delete();
                }
                if (mVM != null) {
                    File file = new File(Uri.parse(mVM).getPath());
                    if (file != null) file.delete();
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else if (requestCode == CODE_AUDIO_CHOOSER) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null && intent.getData() != null) {
                    results = new Uri[]{intent.getData()};
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    String result = intent.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                    if (result != null && URLUtil.isValidUrl(result)) {
                        webView.loadUrl(result);
                    }
                }
            }
        }
        /* else {
            super.handleActivityResult(requestCode, resultCode, intent);
        }*/
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "";
        File mediaStorageDir = getCacheDir();
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + "WebView" + " directory");
                return null;
            }
        }
        return File.createTempFile(
                imageFileName,
                ".jpg",
                mediaStorageDir
        );
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "VID_" + timeStamp + "";
        File mediaStorageDir = getCacheDir();

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + "WebView" + " directory");
                return null;
            }
        }
        return File.createTempFile(
                imageFileName,
                ".mp4",
                mediaStorageDir
        );
    }

    @Override
    public void onBackPressed() {
        if (Config.EXIT_APP_BY_BACK_BUTTON_ALWAYS) {
            finish();
        } else {


            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                if (Config.EXIT_APP_BY_BACK_BUTTON_HOMEPAGE) {
                    finish();
                    super.onBackPressed();
                }

            }

        }

    }


    private void customCSS() {
        try {
            InputStream inputStream = getAssets().open("custom.css");
            byte[] cssbuffer = new byte[inputStream.available()];
            inputStream.read(cssbuffer);
            inputStream.close();

            String encodedcss = Base64.encodeToString(cssbuffer, Base64.NO_WRAP);
            if (!TextUtils.isEmpty(encodedcss)) {
                Log.d("css", "Custom CSS loaded");
                webView.loadUrl("javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "style.innerHTML = window.atob('" + encodedcss + "');" +
                        "parent.appendChild(style)" +
                        "})()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openDownloadedAttachment(final Context context, final long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);

        if (cursor.moveToFirst()) {
            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));

            ;

            if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
                Log.d("texts", "Download done");
                Toast.makeText(context, "Saved to SD card", Toast.LENGTH_LONG).show();
                openDownloadedAttachment(context, Uri.parse(downloadLocalUri), downloadMimeType);


            }
        }
        cursor.close();
    }

    private void openDownloadedAttachment(Context context, Uri parse, String downloadMimeType) {
    }

    private void downloadImageNew(String filename, String downloadUrlOfImage){
        try{
            DownloadManager dm = (DownloadManager) getSystemService(this.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType("image/jpeg") // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            Toast.makeText(this, "Image download started.", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e("heree ",e.toString());
            Toast.makeText(this, "Image download failed.", Toast.LENGTH_SHORT).show();
        }
    }

    protected static File screenshot(View view, String filename) {

        Date date = new Date();

        // Here we are initialising the format of our image name
        CharSequence format = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);
        try {
            // Initialising the directory of storage
            String dirpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "";
            File file = new File(dirpath);
            if (!file.exists()) {
                boolean mkdir = file.mkdir();
            }

            // File name
            String path = dirpath + "/DCIM/" + filename + "-" + format + ".jpeg";
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            File imageurl = new File(path);

            saveImage(bitmap, format.toString());

//            Process sh = Runtime.getRuntime().exec("su", null,null);
//            OutputStream os = sh.getOutputStream();
//            os.write(("/system/bin/screencap -p " + dirpath + "/DCIM/" + filename + ".png").getBytes("ASCII"));
//            os.flush();
//            os.close();
//            sh.waitFor();

//            if(imageurl.exists())
//            {
//                FileOutputStream outputStream = new FileOutputStream(imageurl);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
//                outputStream.flush();
//                outputStream.close();
//                System.out.println("!!!!1!");
//            }
//            else
//            {
//                FileOutputStream outputStream = new FileOutputStream(imageurl);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
//                outputStream.flush();
//                outputStream.close();
////                System.out.println("!!!!1!");
//                System.out.println("!!!! not exist !");
//            }

            return imageurl;

        } catch (IOException e) {
            System.out.println("!!!");
            e.printStackTrace();
        }
        return null;
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermission(Activity activity){
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(activity, PERMISSION_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    public static void saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = mContext.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "img");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + "img";

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);
        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
    }

    private static String[] permissionstorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    // verifying if storage permission is given or not
    public static void verifystoragepermissions(Activity activity) {

        int permissions = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        System.out.println("?!" + permissions);
        System.out.println("?!!" + PackageManager.PERMISSION_GRANTED);

        // If storage permission is not given then request for External Storage Permission

        ActivityCompat.requestPermissions(activity, permissionstorage, 1);

    }


    private void loadMainUrl() {

        offlineLayout.setVisibility(View.GONE);

        if (Config.IS_DEEP_LINKING_ENABLED && deepLinkingURL != null && !deepLinkingURL.isEmpty()) {
            Log.i(TAG, " deepLinkingURL " + deepLinkingURL);
            if (isNotificationURL && Config.OPEN_NOTIFICATION_URLS_IN_SYSTEM_BROWSER && URLUtil.isValidUrl(deepLinkingURL)) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkingURL)));
                deepLinkingURL = null;
            } else if (URLUtil.isValidUrl(deepLinkingURL)) {
                webView.loadUrl(deepLinkingURL);
                return;
            } else {
                Toast.makeText(this, "URL is not valid", Toast.LENGTH_SHORT).show();
            }
        }
        String urlExt = "";
        String urlExt2 = "";
        String language = "";
        if (Config.APPEND_LANG_CODE) {
            language = Locale.getDefault().getLanguage().toUpperCase();
            language = "?webview_language=" + language;
        } else {
            language = "";
        }
        String urlToLoad = Config.HOME_URL + language;
        if (Config.PUSH_ENABLED) {
            OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
            String userID = status.getSubscriptionStatus().getUserId();

            urlExt = ((Config.PUSH_ENHANCE_WEBVIEW_URL
                    && !TextUtils.isEmpty(userID))
                    ? String.format("%sonesignal_push_id=%s", (urlToLoad.contains("?") ? "&" : "?"), userID) : "");
        }
        if (Config.FIREBASE_PUSH_ENABLED) {
            if (Config.FIREBASE_PUSH_ENHANCE_WEBVIEW_URL) {
                String userID2 = firebaseUserToken;
//                userID2 = userID2.replaceFirst("com.google.firebase.messaging.FirebaseMessaging@", "");
                if (!userID2.isEmpty()) {
                    if(urlToLoad.contains("?") || urlExt.contains("?")) {
                        urlExt2 = String.format("%sfirebase_push_id=%s", "&", userID2);
                    }
                    else{
                        urlExt2 = String.format("%sfirebase_push_id=%s", "?", userID2);
                    }
                } else {
                    urlExt2 = "";
                }
            }
        }
        if (Config.USE_LOCAL_HTML_FOLDER) {
            loadLocal(INDEX_FILE);
        } else {
            Log.i(TAG, " HOME_URL " + urlToLoad + urlExt + urlExt2);
            webView.loadUrl(urlToLoad + urlExt + urlExt2);
        }
    }

    public boolean isConnectedNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @SuppressLint("WrongConstant")
    private void askForPermission() {
//        int accessCoarseLocation = 0;
//        int accessFineLocation = 0;
//        int accessCamera = 0;
//        int accessStorage = 0;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            accessCoarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
//            accessFineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
//            accessCamera = checkSelfPermission(Manifest.permission.CAMERA);
//            accessStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//            Log.d("per", ">=M");
//
//        } else {
//            Log.d("per", "<M");
//        }
//
//
//        List<String> listRequestPermission = new ArrayList<String>();
//
//        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
//            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
//        }
//        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
//            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
//        }
//        if (accessCamera != PackageManager.PERMISSION_GRANTED) {
//            listRequestPermission.add(Manifest.permission.CAMERA);
//        }
//        if (accessStorage != PackageManager.PERMISSION_GRANTED) {
//            listRequestPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//            listRequestPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//        if (!listRequestPermission.isEmpty()) {
//            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(strRequestPermission, 1);
//            }
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> listRequestPermission = preparePermissionList();
            if (!listRequestPermission.isEmpty()) {
                String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
                requestPermissions(strRequestPermission, 1);
            }
        }
    }

    private List<String> preparePermissionList() {

        ArrayList<String> permissionList = new ArrayList<>();

        if (Config.requireLocation) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Config.requireCamera) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (Config.requireStorage) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (Config.requireRecordAudio) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }

        return permissionList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {

                String[] PERMISSIONS = {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, MULTIPLE_PERMISSIONS);
                }
            }

            default:
                loadMainUrl();
        }
    }

    @Override
    public void onOSSubscriptionChanged(OSSubscriptionStateChanges stateChanges) {
        if (Config.PUSH_ENABLED) {
            if (!stateChanges.getFrom().getSubscribed() && stateChanges.getTo().getSubscribed()) {
                String userId = stateChanges.getTo().getUserId();
                Log.i(TAG, "userId: " + userId);

                if (Config.PUSH_RELOAD_ON_USERID) {
                    loadMainUrl();
                }
            }

            Log.i(TAG, "onOSPermissionChanged: " + stateChanges);
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Config.PUSH_ENABLED) {
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.NONE);
            OneSignal.startInit(this)
                    .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                    .setNotificationOpenedHandler(new MyNotificationOpenedHandler(getApplication()))
                    .unsubscribeWhenNotificationsAreDisabled(true)
                    .init();

        }

        if (mAdView != null) {
            if (!HIDE_ADS_FOR_PURCHASE) {
                mAdView.resume();
            }
        }

    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (facebookAdView != null) {
            facebookAdView.destroy();
        }

        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
        if (facebookInterstitialAd != null) {
            facebookInterstitialAd.destroy();
        }

        super.onDestroy();
    }

    private void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            webViewCount = 0;
        } else if (facebookInterstitialAd != null && facebookInterstitialAd.isAdLoaded()) {
            facebookInterstitialAd.show();
            webViewCount = 0;
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainActivity.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, MainActivity.this,
                        1001);
                if (dialog != null) {
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            if (ConnectionResult.SERVICE_INVALID == resultCode) {

                            }
                        }
                    });
                    return false;
                }
            }
            Toast.makeText(this, "This device is not supported for required Goole Play Services", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void osURL(String currentOSUrl) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("www.google.com");
            }
        });
    }

    public void checkItemPurchase(SkuDetailsParams.Builder params) {
        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                            && skuDetailsList != null && !skuDetailsList.isEmpty()) {
                        Log.e(TAG, "Purchase item 111");
                        for (SkuDetails skuDetails : skuDetailsList) {
                            Log.e(TAG, "Purchase item : " + skuDetails.getSku());
                            String sku = skuDetails.getSku();
                            purchaseItem(skuDetails);
                            break;
                        }
                    } else {
                        Log.e(TAG, "Purchase item error : " + billingResult.getDebugMessage());
                        Toast.makeText(this, "Unable to get any package!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void purchaseItem(SkuDetails skuDetails) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        BillingResult responseCode = billingClient.launchBillingFlow(this, flowParams);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Toast.makeText(MainActivity.this, "Purchased :)", Toast.LENGTH_SHORT).show();
            if (disableAdMob) {
                HIDE_ADS_FOR_PURCHASE = true;
                AlertManager.purchaseState(getApplicationContext(), true);
                mAdView.setVisibility(View.GONE);
                mAdView.destroy();
                adLayout.removeAllViews();
                adLayout.setVisibility(View.GONE);
            } else {
                HIDE_ADS_FOR_PURCHASE = false;
                mAdView.setEnabled(true);
                AlertManager.purchaseState(getApplicationContext(), false);
                mAdView.setVisibility(View.VISIBLE);
                adLayout.setVisibility(View.VISIBLE);
                mAdView.loadAd(new AdRequest.Builder().build());
            }
            webView.loadUrl(successUrl);
            successUrl = "";
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            if (failUrl != null && failUrl.length() > 0) {
                webView.loadUrl(failUrl);
            }
        } else {
            Toast.makeText(this, "Something went wrong :(", Toast.LENGTH_SHORT).show();
        }
    }

    public void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            Toast.makeText(MainActivity.this, "Purchased :)", Toast.LENGTH_SHORT).show();
            if (disableAdMob) {
                HIDE_ADS_FOR_PURCHASE = true;
                AlertManager.purchaseState(getApplicationContext(), true);
                mAdView.setVisibility(View.GONE);
                mAdView.destroy();
                adLayout.removeAllViews();
                adLayout.setVisibility(View.GONE);
            } else {
                HIDE_ADS_FOR_PURCHASE = false;
                mAdView.setEnabled(true);
                AlertManager.purchaseState(getApplicationContext(), false);
                mAdView.setVisibility(View.VISIBLE);
                adLayout.setVisibility(View.VISIBLE);
                mAdView.loadAd(new AdRequest.Builder().build());
            }
            webView.loadUrl(successUrl);
            successUrl = "";

            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener =
            billingResult -> {
            };

    private Handler notificationHandler;

    Timer timer = new Timer();


    @SuppressWarnings("SpellCheckingInspection")
    private class MyWebViewClient extends WebViewClient {

        MyWebViewClient() {
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (ACTIVATE_PROGRESS_BAR) {
                progressBar.setVisibility(View.VISIBLE);
            }
            super.onPageStarted(view, url, favicon);
            webViewCount = webViewCount + 1;
            if (webViewCount >= Config.SHOW_AD_AFTER_X) {
                if (Config.SHOW_FULL_SCREEN_AD && !HIDE_ADS_FOR_PURCHASE) {
                    if (Config.USE_FACEBOOK_ADS) {
                        if (facebookInterstitialAd != null) {
                            facebookInterstitialAd.loadAd();
                        }
                    } else {
                        if (mInterstitialAd != null) {
                            final AdRequest fullscreenAdRequest = new AdRequest.Builder()
                                    .build();
                            mInterstitialAd.loadAd(fullscreenAdRequest);
                        }
                    }
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            setTitle(view.getTitle());
            progressBar.setVisibility(View.GONE);
            customCSS();
            super.onPageFinished(view, url);

        }



        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            hostpart = Uri.parse(url).getHost();
            Log.e(TAG, "should override : " + url);

            if (hostpart.contains("whatsapp.com")) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                final int newDocumentFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | newDocumentFlag | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
            }
            if (url.contains(".") &&
                    downloadableExtension.contains(url.substring(url.lastIndexOf(".")))) {

                webView.stopLoading();


                String[] PERMISSIONS = {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                if (Config.requireStorage) {
                    if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, MULTIPLE_PERMISSIONS);
                    } else {
                        downloadFile(url);
                    }
                }
                return true;
            }
            if (isConnectedNetwork()) {
                if (hostpart.contains(Config.HOST)) {
                    view.loadUrl(url);
                    return true;
                } else if (url.startsWith("inapppurchase://")
                        || url.startsWith("inappsubscription://")) {

                    if (extendediap) {
                        Log.i(TAG, "play " + checkPlayServices());
                        if (checkPlayServices() && billingClient.isReady()) {
                            disableAdMob = url.contains("disableadmob");
                            handleAppPurchases(url);
                        } else {
                            Log.i(TAG, " toast ");
                            String iaptext1 = "SW4tQXBwIFB1cmNoYXNlcyBhcmUgbm90IGF2YWlsYWJsZS4gUGxlYXNlIGNoZWNrIGlmIEdvb2dsZSBQbGF5IFNlcnZpY2VzIGlzIGluc3RhbGxlZCAoRXJyb3IgMSk=";
                            byte[] iapdata1 = Base64.decode(iaptext1, Base64.DEFAULT);
                            String iapdata1final = new String(iapdata1, StandardCharsets.UTF_8);
                            Toast.makeText(MainActivity.this, iapdata1final, Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else {
                        String iaptext2 = "UGxlYXNlIHVwZ3JhZGUgeW91ciBSZWd1bGFyIExpY2Vuc2UgdG8gYW4gRXh0ZW5kZWQgTGljZW5zZSB0byB1c2UgZmVhdHVyZXMgdGhhdCByZXF1aXJlIHlvdXIgdXNlcnMgdG8gcGF5LiBUaGlzIGlzIHJlcXVpcmVkIGJ5IHRoZSBDb2RlQ2FueW9uL0VudmF0byBNYXJrZXQgbGljZW5zZSB0ZXJtcy4gWW91IGNhbiByZXVzZSB5b3VyIGxpY2Vuc2UgZm9yIGFub3RoZXIgcHJvamVjdCBPUiByZXF1ZXN0IGEgcmVmdW5kIGlmIHlvdSB1cGdyYWRlLiBWaXNpdCB3d3cud2Vidmlld2dvbGQuY29tL3VwZ3JhZGUtbGljZW5zZSBmb3IgbW9yZSBpbmZvcm1hdGlvbi4=";
                        byte[] iapdata2 = Base64.decode(iaptext2, Base64.DEFAULT);
                        String iapdata2final = new String(iapdata2, StandardCharsets.UTF_8);
                        Toast.makeText(MainActivity.this, iapdata2final, Toast.LENGTH_LONG).show();
                        return true;
                    }
                } else if (url.startsWith("qrcode://")) {
                    Log.e(TAG, url);
                    if (Config.requireCamera) {
                        Intent i = new Intent(MainActivity.this, QrCodeActivity.class);
                        startActivityForResult(i, REQUEST_CODE_QR_SCAN);
                    }
                    return true;
                }
                if (url.startsWith("savethisimage://?url=")) {
                    webView.stopLoading();
                    if (webView.canGoBack()) {
                        webView.goBack();
                    }
                    if (Config.requireStorage) {
                        final String imageUrl = url.substring(url.indexOf("=") + 1, url.length());
                        downloadImageNew("imagesaving", imageUrl );
                    }
                    return true;
                } else if (url.startsWith("sendlocalpushmsg://push.send")) {
                    webView.stopLoading();
                    if (webView.canGoBack()) {
                        webView.goBack();
                    }
                    sendNotification(url);
                } else if (url.startsWith("sendlocalpushmsg://push.send.cancel") && notificationHandler != null) {
                    webView.stopLoading();
                    if (webView.canGoBack()) {
                        webView.goBack();
                    }
                    notificationHandler.removeCallbacksAndMessages(null);
                    notificationHandler = null;
                } else if (url.startsWith("get-uuid://")) {
                    webView.loadUrl("javascript: var uuid = '" + uuid + "';");
                    return true;
                } else if (url.startsWith("reset://")) {
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setAppCacheEnabled(false);
                    webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                    webView.clearCache(true);
                    Toast.makeText(MainActivity.this, "App reset was successful.", Toast.LENGTH_LONG).show();
                    loadMainUrl();
                    return true;
                } else if (url.startsWith("spinneron://")) {
                    progressBar.setVisibility(View.VISIBLE);
                    return true;
                } else if (url.startsWith("spinneroff://")) {
                    progressBar.setVisibility(View.GONE);
                    return true;
                } else if (url.startsWith("screenshot://")) {
                    verifystoragepermissions(MainActivity.this);

                    Toast.makeText(MainActivity.this, "Screenshot Saved", Toast.LENGTH_LONG).show();
                    screenshot(getWindow().getDecorView().getRootView(), "result");

                    return true;

                } else if (url.startsWith("shareapp://")) {
                    Log.e(TAG, url);
                    String shareMessage = "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";

                    if (url.contains("sharetext?=")) {
                        String key_share_text = "sharetext?=";
                        int firstIndex = url.lastIndexOf(key_share_text);
                        shareMessage = url.substring(firstIndex + key_share_text.length());
                    }

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Share the app"));
                    return true;
                }
            } else if (!isConnectedNetwork()) {
                if (Config.FALLBACK_USE_LOCAL_HTML_FOLDER_IF_OFFLINE) {
                    if (!offlineFileLoaded) {
                        loadLocal(INDEX_FILE);
                        offlineFileLoaded = true;
                    } else {
                        loadLocal(url);
                    }
                } else {
                    offlineLayout.setVisibility(View.VISIBLE);
//                    System.out.println("attempt rec 1");
//
//                    //auto reload every 10s
//                    class AutoRec extends TimerTask {
//                        public void run() {
//                            runOnUiThread(new Runnable() {
//                                public void run() {
//                                    System.out.println("attempting reconnect");
//                                    webView.setVisibility(View.GONE);
//                                    loadMainUrl();
//                                }
//                            });
//
//                        }
//                    }
//
//                    timer.schedule(new AutoRec(), 0, 50000);
//                    timer.cancel();

                }
                return true;
            } else if ((Config.OPEN_EXTERNAL_URLS_IN_ANOTHER_BROWSER
                    && !(url).startsWith("file://") && (!Config.USE_LOCAL_HTML_FOLDER
                    || !(url).startsWith("file://"))) && URLUtil.isValidUrl(url)) {
                Log.i(TAG, "url" + url);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                return true;
            } else {
                return false;
            }
            return false;
        }
    }

    private void handleAppPurchases(String url) {
        String keyPackage = "package=";
        String keySuccessURL = "&successful_url=";
        String keyExpiredURL = "&expired_url=";
        Log.i(TAG, "play " + checkPlayServices());
        int packageIndex = -1;
        int successIndex = -1;
        int expireIndex = -1;
        String packagePlan = "";
        if (url.contains(keyPackage)) {
            packageIndex = url.indexOf(keyPackage) + keyPackage.length();
        }
        if (url.contains(keySuccessURL)) {
            successIndex = url.indexOf(keySuccessURL) + keySuccessURL.length();
        }
        if (url.contains(keyExpiredURL)) {
            expireIndex = url.indexOf(keyExpiredURL) + keyExpiredURL.length();
        }
        try {
            if (packageIndex != -1) {
                packagePlan = url.substring(packageIndex, url.indexOf("&"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (expireIndex == -1) {
                successUrl = url.split(keySuccessURL)[1];
                failUrl = "";
            } else {
                successUrl = url.substring(successIndex, expireIndex - keyExpiredURL.length());
                failUrl = url.substring(expireIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!packagePlan.isEmpty()) {
            List<String> skuList = new ArrayList<>();
            skuList.add(packagePlan);
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            if (url.startsWith("inapppurchase://")) {
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
            } else if (url.startsWith("inappsubscription://")) {
                params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
            }
            checkItemPurchase(params);
        } else {
            Toast.makeText(this, "Unable to get any package. Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotification(String url) {
        final int secondsDelayed = Integer.parseInt(url.split("=")[1]);

        final String[] contentDetails = (url.substring((url.indexOf("msg!") + 4), url.length())).split("&!#");
        final String message = contentDetails[0].replaceAll("%20", " ");
        final String title = contentDetails[1].replaceAll("%20", " ");

        final Notification.Builder builder = getNotificationBuilder(title, message);

        final Notification notification = builder.build();
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationHandler = null;
        notificationHandler = new Handler();
        notificationHandler.postDelayed((Runnable) () -> {
            notificationManager.notify(0, notification);
            notificationHandler = null;
        }, secondsDelayed * 1000);
    }

    private Notification.Builder getNotificationBuilder(String title, String message) {

        createNotificationChannel();
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(MainActivity.this, getString(R.string.local_notification_channel_id));
        } else {
            builder = new Notification.Builder(MainActivity.this);
        }

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
//        intent.putExtra("ONESIGNAL_URL", "www.google.com");
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        return builder;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.local_notification_channel_name);
            String description = getString(R.string.local_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.local_notification_channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void downloadFile(String url) {
        try {
            String fileName = getFileNameFromURL(url);
            Toast.makeText(MainActivity.this, "Downloading file...", Toast.LENGTH_SHORT).show();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            String cookie = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("Cookie", cookie);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }


        BroadcastReceiver onComplete = new BroadcastReceiver() {

            public void onReceive(Context ctxt, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    openDownloadedAttachment(MainActivity.this, downloadId);
                }
            }
        };
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        }
        try {
            URL resource = new URL(url);
            String host = resource.getHost();
            if (host.length() > 0 && url.endsWith(host)) {
                return "";
            }
        } catch (MalformedURLException e) {
            return "";
        }

        int startIndex = url.lastIndexOf('/') + 1;
        int length = url.length();


        int lastQMPos = url.lastIndexOf('?');
        if (lastQMPos == -1) {
            lastQMPos = length;
        }

        int lastHashPos = url.lastIndexOf('#');
        if (lastHashPos == -1) {
            lastHashPos = length;
        }

        int endIndex = Math.min(lastQMPos, lastHashPos);
        return url.substring(startIndex, endIndex);
    }


    private class MyWebChromeClient extends WebChromeClient {

        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyWebChromeClient() {
        }


        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846);
        }

        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            //callback.invoke(origin, true, false);
            final boolean remember = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Locations");
            builder.setMessage("Would like to share your current location?")
                    .setCancelable(true).setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // origin, allow, remember
                    callback.invoke(origin, true, remember);
                    int result = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (result == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                    }
                }
            }).setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // origin, allow, remember
                    callback.invoke(origin, false, remember);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.i(TAG, "progress " + newProgress);
            if (newProgress == 100) {
                mAdView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            request.grant(request.getResources());
        }

    }
}