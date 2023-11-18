package com.hamsterhub.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchUtil {

    public static Boolean isPhoneMatches(String phone) {
        String regex = "^1(3\\d|4[5-9]|5[0-35-9]|6[567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    public static Boolean isEmailMatches(String email) {
        String regex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static Boolean isUsernameMatches(String username) {
        String regex = "^[a-zA-Z0-9_-]{3,16}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(username);
        return m.matches();
    }

    public static Boolean isPasswordMatches(String password) {
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"; //长度至少为8，至少含有一个字母和一个数字
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        return m.matches();
    }

    public static Boolean isPathMatches(String path) {
        String regex = "^\\/(?:[^\\s\\/]+\\/)*[^\\s\\/]+(?:\\.\\w+)?$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(path);
        return m.matches();
    }

}
