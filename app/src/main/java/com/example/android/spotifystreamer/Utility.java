package com.example.android.spotifystreamer;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.util.Random;

/**
 * Created by User on 7/10/2015.
 */
public class Utility {
    private static Random randomNumber = new Random();

    public static ColorDrawable randomColorDrawable() {
        return new ColorDrawable(Color.rgb(randomNumber.nextInt(256), randomNumber.nextInt(256), randomNumber.nextInt(256)));
    }
}
