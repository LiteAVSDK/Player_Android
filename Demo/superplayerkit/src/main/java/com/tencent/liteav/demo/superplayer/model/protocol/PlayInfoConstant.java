package com.tencent.liteav.demo.superplayer.model.protocol;

public class PlayInfoConstant {

    public enum EncryptedURLType {

        SIMPLEAES("SimpleAES"),
        WIDEVINE("widevine");

        EncryptedURLType(String type) {
            value = type;
        }

        private String value;

        public String getValue() {
            return value;
        }
    }
}
