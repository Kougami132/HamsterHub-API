package com.hamsterhub.common.domain;


/**
 * 系统设置的key
 */
public class ConfigKey  {
	public final static String CAN_REGISTER = "user.register";

	public static boolean isTrue(String value){
		return "true".equalsIgnoreCase(value);
	}

}
