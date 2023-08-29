package com.tencent.liteav.demo.superplayer.model.utils;

import java.util.Random;

public class RandomUtils {

    /**
     * Get a random number within a range.
     *
     * 获取范围内的随机数
     */
    public static Integer getRandomNumber(int min, int max) {
        Random random = new Random();
        int result = random.nextInt(max) % (max - min + 1) + min;
        return result;
    }

}
