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
	E_100007(100007,"任务不存在"),
	E_100008(100008,"离线下载队列已满"),
	E_100009(100008,"功能被禁用"),
	E_100010(100008,"存在参数为空"),

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
	E_200017(200017,"每个手机号每日仅限发送三次验证码，请明天再试"),
	E_200018(200018,"每两次发送验证码需相隔60秒，请稍后再试"),
	E_200019(200019,"暂未开放注册"),

	// 存储设备异常编码 300
	E_300001(300001,"存储设备不存在"),
	E_300002(300002,"设备名已存在"),
	E_300003(300003,"设备已绑定其他策略"),
	E_300004(300004,"不存在该设备类型编号"),
	E_300005(300005,"设备参数格式错误"),
	E_300006(300006,"设备连接失败"),
	E_300007(300007,"设备剩余空间不足"),
	E_300008(300008,"该本地目录已存在设备"),

	// 存储策略异常编码 400
	E_400001(400001,"存储策略不存在"),
	E_400002(400002,"存储策略名称已存在"),
	E_400003(400003,"存储策略配置不存在"),
	E_400004(400004,"存储策略类型不存在"),
	E_400005(400005,"存储策略模式不存在"),
	E_400006(400006,"存储策略根目录已存在"),
	E_400007(400007,"存储策略未就绪"),

	// 实际文件异常编码 500
	E_500001(500001,"文件不存在(r)"),
	E_500002(500002,"相同hash值的文件已存在"),
	E_500003(500003,"hash值为空"),
	E_500004(500004,"图片格式错误"),
	E_500005(500005,"文件过大"),
	E_500006(500006,"上传失败"),
	E_500007(500007,"头像初始化失败"),
	E_500008(500008,"离线下载请求出错，请联系管理员"),
	E_500009(500009,"实际文件大小与声明大小不相符"),
	E_500010(500010,"错误上传格式"),
	E_500011(500011,"文件名为空"),
	E_500012(500012,"目标为文件夹"),

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
	E_600017(600017,"直链不存在"),
	E_600018(600018,"直链已过期"),
	E_600019(600019,"ticket已存在"),
	E_600020(600020,"ticket与vFileId不匹配"),
	E_600021(600021,"分享中的文件不得隐藏或显示"),
	E_600022(600022,"文件与目标目录不属于同策略"),
	E_600023(600023,"目标目录是文件的子目录"),
	E_600024(600024,"root与策略id不一致"),

	// 网盘或其他设备连接异常编码 700
	E_700001(700001,"阿里云盘签名计算错误"),
	E_700002(700002,"阿里云盘文件上传出错"),
	E_700003(700003,"qBittorrent未连接"),
	E_700004(700004,"Bot连接失败"),
	E_700005(700005,"阿里云盘登录二维码请求失败"),

	// 系统设置相关 800
	E_800001(800001,"key为空"),

	// RSS异常编码 110
	E_110001(110001,"您的账号下不存在指定目标"),

	// 离线下载异常编码 120
	E_120001(120001,"指定下载器不存在"),
	E_120002(120002,"指定下载器未就绪"),
	E_120003(120003,"删除异常"),

	// 消息推送异常编码 130
	E_130001(130001,"推送类型不存在"),
	E_130002(130002,"推送配置格式错误"),

	// 其它
	E_NETWORK_ERROR(999996, "网络错误"),
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

	public static void checkAndThrow(Boolean state, CommonErrorCode error) {
		if (state){
			throw new BusinessException(error);
		}
	}
}
