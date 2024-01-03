package com.hamsterhub.common.domain;


/**
 * 异常编码
 */
public enum CommonErrorCode implements ErrorCode {

	// 公用异常编码 100
	E_100001(100001,"传入对象为空"),
	E_100002(100002,"传入参数与接口不匹配"),
	E_100003(100003,"查询结果为空"),
	E_100004(100004,"ID格式不正确或超出Long存储范围"),
	E_100005(100005,"上传错误"),
	E_100006(100006,"发送验证码错误"),

	// 用户异常编码 200
	E_200001(200001,"用户名为空"),
	E_200002(200002,"密码为空"),
	E_200003(200003,"手机号为空"),
	E_200004(200004,"用户名格式不正确，请输入3到16位由数字、字母或下划线组成的用户名"),
	E_200005(200005,"密码格式不正确，请输入长度至少为8，至少含有一个字母和一个数字的密码"),
	E_200006(200006,"手机号格式不正确"),
	E_200007(200007,"用户名已存在"),
	E_200008(200008,"手机号已存在"),
	E_200009(200009,"密码不正确"),
	E_200010(200010,"当前用户已存在相同名称的配置"),
	E_200011(200011,"验证码错误"),
	E_200012(200012,"验证码为空"),
	E_200013(200013,"账号ID不存在"),
	E_200014(200014,"邮箱格式不正确"),
	E_200015(200015,"用户名不存在"),
	E_200016(200016,"密码错误"),

	// 存储设备异常编码 300
	E_300001(300001,"存储设备不存在"),
	E_300002(300002,"设备名已存在"),
	E_300003(300003,"设备已绑定其他策略"),
	E_300004(300004,"不存在该设备类型编号"),
	E_300005(300005,"设备参数格式错误"),
	E_300006(300006,"设备连接失败"),
	E_300007(300007,"设备剩余空间不足"),

	// 存储策略异常编码 400
	E_400001(400001,"存储策略不存在"),
	E_400002(400002,"存储策略名称已存在"),
	E_400003(400003,"存储策略配置不存在"),
	E_400004(400004,"存储策略类型不存在"),
	E_400005(400005,"存储策略模式不存在"),
	E_400006(400006,"存储策略根目录已存在"),

	// 实际文件异常编码 500
	E_500001(500001,"文件不存在(r)"),
	E_500002(500002,"相同hash值的文件已存在"),
	E_500003(500003,"hash值为空"),

	// 虚拟文件异常编码 600
	E_600001(600001,"文件不存在(v)"),
	E_600002(600002,"路径格式错误"),
	E_600003(600003,"目录不存在(v)"),
	E_600004(600004,"文件名为空"),
	E_600005(600005,"文件与用户不匹配"),
	E_600006(600006,"该文件正在分享"),
	E_600007(600007,"分享码不存在"),
	E_600008(600008,"请输入提取码"),
	E_600009(600009,"提取码错误"),
	E_600010(600010,"该分享已过期"),
	E_600011(600011,"分享ID不存在"),
	E_600012(600012,"分享与用户不匹配"),
	E_600013(600013,"父文件必须为目录"),
	E_600014(600014,"目录已存在"),
	E_600015(600015,"新文件名已存在"),
	E_600016(600016,"目标目录已存在同名文件"),

	// 网盘连接异常编码 700
	E_700001(700001,"阿里云盘签名计算错误"),
	E_700002(700002,"阿里云盘文件上传出错"),

	// 其它
	E_NO_PERMISSION(999997, "访问权限不足"),
	E_NO_AUTHORITY(999998,"未登录或登录信息已过期"),
	/**
	 * 未知错误
	 */
	UNKNOWN(999999,"未知错误");


	private int code;
	private String desc;

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	private CommonErrorCode(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}


	public static CommonErrorCode setErrorCode(int code) {
       for (CommonErrorCode errorCode : CommonErrorCode.values()) {
           if (errorCode.getCode()==code) {
               return errorCode;
           }
       }
	       return null;
	}
}
