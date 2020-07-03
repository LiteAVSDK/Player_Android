package com.tencent.liteav.demo.play.utils;

import android.content.Context;

/**
 * UI单位转换工具
 */
public class TCDensityUtil {

    /**
     * sp单位转px
     *
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (spValue * scale + 0.5f);
    }

}
