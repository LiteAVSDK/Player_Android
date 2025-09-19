package com.tencent.liteav.demo.superplayer.helper.annoation;

import androidx.annotation.IntDef;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IntDef({SuperPlayerDef.PlayerRenderType.CLOUD_VIEW, SuperPlayerDef.PlayerRenderType.SURFACE_VIEW})
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface SuperPlayerRenderTypeParams {
}
