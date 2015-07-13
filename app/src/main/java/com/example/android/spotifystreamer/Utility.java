package com.example.android.spotifystreamer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
}
