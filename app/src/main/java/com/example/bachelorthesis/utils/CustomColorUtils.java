package com.example.bachelorthesis.utils;

import android.graphics.Color;

/**
 * @author Finn Zimmer
 */
public class CustomColorUtils {
    /**
     * Calculates whether a colour needs a bright or dark color on top for best contrast
     * @param color the base colour
     * @return true if the colour is bright (i.e. needs a dark colour on it)
     */
    public static boolean isBrightColor(int color) {
        if (android.R.color.transparent == color)
            return false;

        boolean rtnValue = false;

        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};

        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
                * rgb[1] * .691 + rgb[2] * rgb[2] * .068);

        // color is light
        if (brightness >= 200) {
            rtnValue = true;
        }

        return rtnValue;
    }
}
