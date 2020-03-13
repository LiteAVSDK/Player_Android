package com.tencent.liteav.demo.play;

import android.view.View;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by hans on 2019/3/25.
 *
 * 用于生成签名的工具
 *
 * 1. v2 与 v3 协议需要的参数不一样：
 *  v2参考：https://cloud.tencent.com/document/product/266/14424#key-.E9.98.B2.E7.9B.97.E9.93.BE；
 *  v3参考：https://cloud.tencent.com/document/product/266/34101
 *
 * 2. v2需要参数 KEY+appId+fileId+t+exper+us （exper是可选的）
 * 3. v3需要参数 KEY+appId+fileId+playDefinition+t+rlimit+us （rlimit是可选的）
 * 3. v4需要参数 KEY+appId+fileId+pcfg+t+exper+rlimit+us （pcfg、exper、rlimit、us是可选的)
 */
public class SuperPlayerSignUtils {

    private static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
