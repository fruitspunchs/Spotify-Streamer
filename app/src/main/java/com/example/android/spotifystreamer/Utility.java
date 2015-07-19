package com.example.android.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Random;

/**
 * Helper classes
 */
public class Utility {
    private static Random sRandomNumber = new Random();
    private static Toast sToast;

    public static ColorDrawable randomColorDrawable() {
        return new ColorDrawable(Color.rgb(sRandomNumber.nextInt(256), sRandomNumber.nextInt(256), sRandomNumber.nextInt(256)));
    }

    public static void displayToast(Context context, String message) {
        if (sToast != null) {
            sToast.cancel();
        }

        sToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        sToast.show();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            displayToast(context, context.getString(R.string.toast_no_network_connectivity));
        }

        return isConnected;

    }

    public static Intent createShareTrackIntent(String uriString) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, uriString);
        return shareIntent;
    }

    public static boolean isNotificationsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_notifications_key), context.getResources().getBoolean(R.bool.pref_notifications_default));
    }
}
