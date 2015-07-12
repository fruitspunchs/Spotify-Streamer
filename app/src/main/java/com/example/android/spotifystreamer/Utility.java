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
    private static Random randomNumber = new Random();

    public static ColorDrawable randomColorDrawable() {
        return new ColorDrawable(Color.rgb(randomNumber.nextInt(256), randomNumber.nextInt(256), randomNumber.nextInt(256)));
    }

    public static void displayToast(Context context, Toast toast, String message) {
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }
}
