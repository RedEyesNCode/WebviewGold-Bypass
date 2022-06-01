package webviewgold.myappname;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;

import static webviewgold.myappname.Config.SHOW_FACEBOOK_DIALOG;
import static webviewgold.myappname.Config.SHOW_FIRSTRUN_DIALOG;
import static webviewgold.myappname.Config.SHOW_RATE_DIALOG;


class AlertManager {

    private static final String STRING_PREFS = "rate_prefs";
    private static final String PURCHASE_PREFS = "purchase_prefs";
    private static final String PURCHASED = "purchased";
    private static final String STRING_DONT_SHOW_AGAIN_RATE = "don't_show_again_rater";
    private static final String STRING_DONT_SHOW_AGAIN_FACEBOOK = "don't_show_again_facebook";
    private static final String STRING_LAUNCHED_COUNT_RATE = "launched_count_rate";
    private static final String STRING_LAUNCHED_COUNT_FACEBOOK = "launched_count_facebook";
    private static final String STRING_FIRST_LAUNCH_DATE = "first_launch_date";

    public static void appLaunched(final Context context){
        final SharedPreferences prefs = context.getSharedPreferences(STRING_PREFS, Context.MODE_PRIVATE);

        final boolean dontShowRateAgain = prefs.getBoolean(STRING_DONT_SHOW_AGAIN_RATE, false);
        final boolean dontShowFacebookAgain = prefs.getBoolean(STRING_DONT_SHOW_AGAIN_FACEBOOK, false);

        if (dontShowRateAgain && dontShowFacebookAgain) return;

        final SharedPreferences.Editor editor = prefs.edit();

        // "Thanks for downloading!" first run dialog
        long firstLaunchDate = prefs.getLong(STRING_FIRST_LAUNCH_DATE, 0);
        if (firstLaunchDate == 0) {
            firstLaunchDate = System.currentTimeMillis();
            editor.putLong(STRING_FIRST_LAUNCH_DATE, firstLaunchDate);
            if (SHOW_FIRSTRUN_DIALOG) {
                showFirstLaunchDialog(context);
            }

        }

        boolean showRateDialog = false;

        // "Rate this app" dialog
        if (!dontShowRateAgain) {
            final int launchedCount = prefs.getInt(STRING_LAUNCHED_COUNT_RATE, 0) + 1;
            showRateDialog = launchedCount >= Config.RATE_LAUNCHES_UNTIL_PROMPT && System.currentTimeMillis() >= firstLaunchDate + (Config.RATE_DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000);
            if (showRateDialog) {
                if (SHOW_RATE_DIALOG) {
                    showRateDialog(context);
                }
                editor.putInt(STRING_LAUNCHED_COUNT_RATE, 0);
            } else {
                editor.putInt(STRING_LAUNCHED_COUNT_RATE, launchedCount);
            }
        }

        // "Follow on Facebook" dialog
        if (!dontShowFacebookAgain) {
            final int launchedCount = prefs.getInt(STRING_LAUNCHED_COUNT_FACEBOOK, 0) + 1;
            if (!showRateDialog && launchedCount >= Config.FACEBOOK_LAUNCHES_UNTIL_PROMPT && System.currentTimeMillis() >= firstLaunchDate + (Config.FACEBOOK_DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                if (SHOW_FACEBOOK_DIALOG) {
                    showFacebookDialog(context);
                }
                editor.putInt(STRING_LAUNCHED_COUNT_FACEBOOK, 0);
            } else {
                editor.putInt(STRING_LAUNCHED_COUNT_FACEBOOK, launchedCount);
            }
        }

        editor.apply();

    }

    private static SharedPreferences getPurchasePreferences(Context context) {
        return context.getSharedPreferences(PURCHASE_PREFS, Context.MODE_PRIVATE);

    }

    public static void updateFirebaseToken(Context context, String token) {
        final SharedPreferences prefs = context.getSharedPreferences(STRING_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString("FirebaseToken", token).apply();
    }

    public static void getFirebaseToken(Context context, String defValue) {
        final SharedPreferences prefs = context.getSharedPreferences(STRING_PREFS, Context.MODE_PRIVATE);
        prefs.getString("FirebaseToken", defValue);
    }

    public static void purchaseState(Context context,boolean purchased) {
        SharedPreferences.Editor editor = getPurchasePreferences(context).edit();
        editor.putBoolean(PURCHASED, purchased).apply();
    }

    public static boolean isPurchased(Context context) {
        return getPurchasePreferences(context).getBoolean(PURCHASED, false);


    }

    private static void showFirstLaunchDialog(final Context context) {
        new androidx.appcompat.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                .setTitle(R.string.first_launch_title)
                .setMessage(R.string.first_launch_message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    private static void showRateDialog(final Context context){
        final Resources resources = context.getResources();
        final SharedPreferences.Editor editor = context.getSharedPreferences(STRING_PREFS, Context.MODE_PRIVATE).edit();
        new androidx.appcompat.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                .setTitle(resources.getString(R.string.rate_title))
                .setMessage(resources.getString(R.string.rate_message))
                .setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean(STRING_DONT_SHOW_AGAIN_RATE, true);
                        editor.apply();
                    }
                })
                .setNeutralButton(resources.getString(R.string.later), null)
                .setPositiveButton(resources.getString(R.string.rate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                        final Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        @SuppressWarnings({"NewApi", "deprecation"})
                        final int newDocumentFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | newDocumentFlag | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                        try {
                            context.startActivity(goToMarket);
                        } catch (ActivityNotFoundException ex) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
                        }

                        editor.putBoolean(STRING_DONT_SHOW_AGAIN_RATE, true);
                        editor.apply();
                    }
                })
                .create()
                .show();
    }

    private static void showFacebookDialog(final Context context) {
        final SharedPreferences.Editor editor = context.getSharedPreferences(STRING_PREFS, Context.MODE_PRIVATE).edit();
        new androidx.appcompat.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                .setTitle(R.string.facebook_title)
                .setMessage(R.string.facebook_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Uri uri = Uri.parse(Config.FACEBOOK_URL);
                        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        @SuppressWarnings({"NewApi", "deprecation"})
                        final int newDocumentFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | newDocumentFlag | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.startActivity(intent);

                        editor.putBoolean(STRING_DONT_SHOW_AGAIN_FACEBOOK, true);
                        editor.apply();
                    }
                })
                .setNeutralButton(R.string.later, null)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.putBoolean(STRING_DONT_SHOW_AGAIN_FACEBOOK, true);
                        editor.apply();
                    }
                })
                .create()
                .show();
    }
}

