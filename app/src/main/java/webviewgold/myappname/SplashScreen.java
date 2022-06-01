package webviewgold.myappname;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import pl.droidsonroids.gif.GifImageView;

public class SplashScreen extends AppCompatActivity {

    private static SplashScreen instance;
    private GifImageView splashImage;

    public static SplashScreen getInstance() {
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
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
        setContentView(R.layout.splash_screen);
        splashImage = findViewById(R.id.splash);

        try {
            subScribePushChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (Config.SPLASH_SCREEN_ACTIVATED) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Display display = getWindowManager().getDefaultDisplay();
            display.getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;

            ViewGroup.LayoutParams params = splashImage.getLayoutParams();
            if (width < height
                    /*&& (display.getRotation() == Surface.ROTATION_0
                    || display.getRotation() == Surface.ROTATION_180)*/) {
                if (0 <= Config.SCALE_SPLASH_IMAGE && Config.SCALE_SPLASH_IMAGE <= 100) {
                    params.width = (int) (width * Config.SCALE_SPLASH_IMAGE / 100);
                } else {
                    params.width = width;
                }
            } else {
                if (0 <= Config.SCALE_SPLASH_IMAGE && Config.SCALE_SPLASH_IMAGE <= 100) {
                    params.width = (int) (height * Config.SCALE_SPLASH_IMAGE / 100);
                } else {
                    params.width = height;
                }
            }
            params.height = params.width;
            splashImage.setLayoutParams(params);

            Handler handler = new Handler();

            handler.postDelayed(this::finish, Config.SPLASH_TIMEOUT);


            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorWhite));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));

        } else {
            splashImage.setVisibility(View.GONE);
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                finish();
                overridePendingTransition(0, 0);
            }, 0);
        }
    }


    private void subScribePushChannel() {
        try {
            Log.e("Splash_Screen", "Subscribing to topic");

            // [START subscribe_topics]
            FirebaseMessaging.getInstance().subscribeToTopic(Config.firebasechanneltopic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = getString(R.string.msg_subscribed);
                            Log.e("Token", String.valueOf(FirebaseMessaging.getInstance()));
                            if (!task.isSuccessful()) {
                                msg = getString(R.string.msg_subscribe_failed);
                            }
                            Log.e("Splash_Screen", msg);
                        }
                    });
            // [END subscribe_topics]
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}
