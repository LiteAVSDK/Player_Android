package com.tencent.liteav.demo.play.utils;

import android.content.Context;

/**
 * UI单位转换工具
 */
public class TCDensityUtil {

	/**
	 * 获取控件高度，单位px
	 *
	 * @param context
	 * @return
	 */
	public static final float getHeightInPx(Context context) {
		final float height = context.getResources().getDisplayMetrics().heightPixels;
		return height;
	}

	/**
	 * 获取控件宽度，单位px
	 *
	 * @param context
	 * @return
	 */
	public static final float getWidthInPx(Context context) {
		final float width = context.getResources().getDisplayMetrics().widthPixels;
		return width;
	}

	/**
	 * 获取控件高度，单位dp
	 *
	 * @param context
	 * @return
	 */
	public static final int getHeightInDp(Context context) {
		final float height = context.getResources().getDisplayMetrics().heightPixels;
		int heightInDp = px2dip(context, height);
		return heightInDp;
	}

	/**
	 * 获取控件宽度，单位dp
	 *
	 * @param context
	 * @return
	 */
	public static final int getWidthInDp(Context context) {
		final float height = context.getResources().getDisplayMetrics().heightPixels;
		int widthInDp = px2dip(context, height);
		return widthInDp;
	}

	/**
	 * dp单位转px
	 *
	 * @param context
	 * @param dpValue
	 * @return
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * px单位转dp
	 *
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * px单位转sp
	 *
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

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
