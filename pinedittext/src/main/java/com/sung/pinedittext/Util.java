package com.sung.pinedittext;

import android.content.res.Resources;

/**
 * Created by John Sung 3/30/2022
 */
public class Util {
    public static float dpToPx(float dp) {
        return (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
