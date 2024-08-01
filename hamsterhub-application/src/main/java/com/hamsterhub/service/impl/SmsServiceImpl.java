package com.hamsterhub.service.impl;

//import com.aliyuncs.DefaultAcsClient;
//import com.aliyuncs.IAcsClient;
//import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
//import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
//import com.aliyuncs.exceptions.ClientException;
//import com.aliyuncs.profile.DefaultProfile;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.SmsService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

//    @Value("${sms.ali.accessKeyId}")
//    private String accessKeyId;
//    @Value("${sms.ali.accessKeySecret}")
//    private String accessKeySecret;
//    @Value("${sms.ali.sysRegionId}")
//    private String sysRegionId;
//    @Value("${sms.ali.signName}")
//    private String signName;
//    @Value("${sms.ali.templateCode}")
//    private String templateCode;

    @Value("${sms.tencent.secretId}")
    private String secretId;
    @Value("${sms.tencent.secretKey}")
    private String secretKey;
    @Value("${sms.tencent.region}")
    private String region;
    @Value("${sms.tencent.sdkAppId}")
    private String sdkAppId;
    @Value("${sms.tencent.signName}")
    private String signName;
    @Value("${sms.tencent.templateId}")
    private String templateId;

    @Override
    public void sendAliCode(Long phone, String code) throws BusinessException {
//        DefaultProfile profile = DefaultProfile.getProfile(sysRegionId, accessKeyId, accessKeySecret);
//        IAcsClient client = new DefaultAcsClient(profile);
//        SendSmsRequest request = new SendSmsRequest();
//        request.setSysRegionId(sysRegionId);
//        request.setPhoneNumbers(phone.toString());
//        request.setSignName(signName);
//        request.setTemplateCode(templateCode);
//        request.setTemplateParam("{\"code\":\"" + code + "\"}");
//        try {
//            SendSmsResponse response = client.getAcsResponse(request);
//            System.out.println("短信发送" + phone + "：" + response.getMessage());
//        } catch (ClientException e) {
//            e.printStackTrace();
//            System.out.println(e);
//        }
    }

    @Override
    public void sendTencentCode(Long phone, String code) throws BusinessException {
        try {
            /* 必要步骤：
             * 实例化一个认证对象，入参需要传入腾讯云账户密钥对secretId，secretKey。
             * 这里采用的是从环境变量读取的方式，需要在环境变量中先设置这两个值。
             * 您也可以直接在代码中写死密钥对，但是小心不要将代码复制、上传或者分享给他人，
             * 以免泄露密钥对危及您的财产安全。
             * SecretId、SecretKey 查询: https://console.cloud.tencent.com/cam/capi */
            Credential cred = new Credential(secretId, secretKey);

            /* 实例化要请求产品(以sms为例)的client对象
             * 第二个参数是地域信息，可以直接填写字符串ap-guangzhou，支持的地域列表参考 https://cloud.tencent.com/document/api/382/52071#.E5.9C.B0.E5.9F.9F.E5.88.97.E8.A1.A8 */
            SmsClient client = new SmsClient(cred, region);
            /* 实例化一个请求对象，根据调用的接口和实际情况，可以进一步设置请求参数
             * 您可以直接查询SDK源码确定接口有哪些属性可以设置
             * 属性可能是基本类型，也可能引用了另一个数据结构
             * 推荐使用IDE进行开发，可以方便的跳转查阅各个接口和数据结构的文档说明 */
            SendSmsRequest req = new SendSmsRequest();

            req.setSmsSdkAppId(sdkAppId);
            req.setSignName(signName);
            req.setTemplateId(templateId);

            /* 模板参数: 模板参数的个数需要与 TemplateId 对应模板的变量个数保持一致，若无模板参数，则设置为空 */
            String[] templateParamSet = {code, "2"};
            req.setTemplateParamSet(templateParamSet);

            /* 下发手机号码，采用 E.164 标准，+[国家或地区码][手机号]
             * 示例如：+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号 */
            String[] phoneNumberSet = {"+86" + phone};
            req.setPhoneNumberSet(phoneNumberSet);

            /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
             * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
            SendSmsResponse res = client.SendSms(req);

            // 输出json格式的字符串回包
            log.info(SendSmsResponse.toJsonString(res));
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }

    }
}
