package com.hamsterhub.common.util;

import java.util.Base64;

public class Base64Util {
    /**
     * 普通Base64编码
     * @param input 要编码的字符串
     * @return 编码后的字符串
     */
    public static String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    /**
     * 普通Base64解码
     * @param encoded 要解码的字符串
     * @return 解码后的字符串
     */
    public static String decode(String encoded) {
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return new String(decodedBytes);
    }

    /**
     * URL安全的Base64编码
     * @param input 要编码的字符串
     * @return 编码后的字符串
     */
    public static String encodeUrlSafe(String input) {
        return Base64.getUrlEncoder().encodeToString(input.getBytes());
    }

    /**
     * URL安全的Base64解码
     * @param encoded 要解码的字符串
     * @return 解码后的字符串
     */
    public static String decodeUrlSafe(String encoded) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
        return new String(decodedBytes);
    }
}
