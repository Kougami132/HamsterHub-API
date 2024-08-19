package com.hamsterhub.service.device.ext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.service.RedisService;
import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.service.device.Storage;

import com.hamsterhub.database.dto.DeviceDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
public class AliDrive extends Storage {

    private RestTemplate restTemplate;
    private RedisService redisService;

    private Integer code = 1;
    private String name = "阿里云盘";
    private DeviceDTO device;
    private String refreshToken;
    private String token;
    private String userId;
    private Long total = 0L;
    private Long usable = 0L;
    private String driveId;
    private long partSize = 1024 * 1024 * 5;

    private HttpHeaders headers;
    private String privateKey;
    private String publicKey;
    private String xDeviceId;
    private String signature;
    private final String appId = "5dde4e1bdf9e4966b387ba58f4b3fdc3";
    private Integer nonce = 0;

    public AliDrive(DeviceDTO deviceDTO) {
        super(deviceDTO);
        this.device = deviceDTO;

        // 获取依赖
        this.restTemplate = GetBeanUtil.getBean(RestTemplate.class);
        this.redisService = GetBeanUtil.getBean(RedisService.class);

        // 初始化
        this.init();
    }

    private void init() {
        try {
            JSONObject param = JSON.parseObject(this.device.getParam());
            this.refreshToken = param.getString("refreshToken");
            queryToken(this.refreshToken);
            getSession();
            queryDriveId();
            this.device.setConnected(true);
        }
        catch (Exception e) {
            e.printStackTrace();
//            throw new BusinessException(CommonErrorCode.E_300006);
            this.device.setConnected(false);
        }
    }

//    @Override
//    public AliDrive withDevice(DeviceDTO device) {
//        AliDrive aliDrive = new AliDrive(device);
//        // 手动给新的实例手动依赖注入
//        applicationContext.getAutowireCapableBeanFactory().autowireBean(aliDrive);
//        aliDrive.init();
//        return aliDrive;
//    }

    @Override
    public String upload(File file, String name, String hash) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("/yyyy/MM/dd"));
        String path = "hamster-hub/uploads" + today;
        String[] folders = path.split("/");
        String parentFleId = "root";
        for (String folderName: folders)
            parentFleId = mkdir(parentFleId, folderName);


        List<String> fileInfo = createFile(parentFleId, file);
        String fileId = fileInfo.get(0),
                uploadId = fileInfo.get(1);
        uploadPart(file, fileInfo.subList(2, fileInfo.size()));
        uploadComplete(fileId, uploadId);
        return fileId;
    }

    @Override
    public String downLoad(String url) {
        return getDownloadUrl(url);
    }

    @Override
    public void delete(String url) {
        trash(url);
    }

    @Override
    public Long getTotalSize() {
        try {
            if (this.total.equals(0L))
                querySize();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return this.total;
    }

    @Override
    public Long getUsableSize() {
        try {
            if (this.usable.equals(0L))
                querySize();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return this.usable;
    }

    @Override
    public boolean verify(DeviceDTO deviceDTO) {
        try {
            JSONObject param = JSON.parseObject(deviceDTO.getParam());
            String refreshToken = param.getString("refreshToken");
            queryToken(refreshToken);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    private void getSession() {
        if (redisService.isAliSessionExist(this.device.getId())) {
            JSONObject data = JSONObject.parseObject(redisService.getAliSession(this.device.getId()));
            this.privateKey = data.getString("privateKey");
            this.publicKey = data.getString("publicKey");
            this.xDeviceId = data.getString("xDeviceId");
            this.signature = data.getString("signature");
            this.headers.set("x-device-id", this.xDeviceId);
            this.headers.set("x-signature", this.signature + "01");
            renewSession();
        }
        else {
            createSession();
            JSONObject data = new JSONObject();
            data.put("privateKey", this.privateKey);
            data.put("publicKey", this.publicKey);
            data.put("xDeviceId", this.xDeviceId);
            data.put("signature", this.signature);
            redisService.setAliSession(this.device.getId(), data.toJSONString());
        }
    }

    private void createSession() {
        // 随机UUID
        this.xDeviceId = UUID.randomUUID().toString();

        // 生成公私钥对
        BigInteger privateKeyInt = new BigInteger(256, new SecureRandom());
        BigInteger publicKeyInt = Sign.publicKeyFromPrivate(privateKeyInt);
        this.privateKey = privateKeyInt.toString(16);
        this.publicKey = publicKeyInt.toString(16);

        // 拼接内容并用私钥签名
        String message = String.format("%s:%s:%s:%s", this.appId, this.xDeviceId, this.userId, this.nonce);
        byte[] dataBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] dataHash = Hash.sha256(dataBytes);
        ECKeyPair keyPair = new ECKeyPair(privateKeyInt, publicKeyInt);
        Sign.SignatureData signatureInfo = Sign.signMessage(dataHash, keyPair, false);
        this.signature = Hex.toHexString(signatureInfo.getR()) + Hex.toHexString(signatureInfo.getS());

        String url = "https://api.aliyundrive.com/users/v1/users/device/create_session";

        // body
        JSONObject body = new JSONObject();
        body.put("pubKey", "04" + this.publicKey);
        body.put("deviceName", "HamsterHub");
        body.put("modelName", "私人网盘");

        this.headers.set("x-device-id", this.xDeviceId);
        this.headers.set("x-signature", this.signature + "01");

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, this.headers);
        restTemplate.postForObject(url, entity, JSONObject.class);
    }

    private void renewSession() {
        String url = "https://api.aliyundrive.com/users/v1/users/device/create_session";

        // body
        JSONObject body = new JSONObject();
        body.put("pubKey", "04" + this.publicKey);
        body.put("deviceName", "HamsterHub");
        body.put("modelName", "私人网盘");

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, this.headers);
        restTemplate.postForObject(url, entity, JSONObject.class);
    }

    private void queryToken(String refreshToken) {
        String url = "https://auth.aliyundrive.com/v2/account/token";

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setOrigin("https://www.aliyundrive.com");
        headers.set("Referer", "https://www.aliyundrive.com/");
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.41");
        headers.set("X-Canary", "client=web,app=adrive,version=v4.9.0");

        // body
        JSONObject body = new JSONObject();
        body.put("grant_type", "refresh_token");
        body.put("refresh_token", refreshToken);

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, headers);

        JSONObject response = restTemplate.postForObject(url, entity, JSONObject.class);
        this.token = response.getString("access_token");
        this.userId = response.getString("user_id");

        headers.set("Authorization", "Bearer " + this.token);

        this.headers = headers;
    }

    private void queryDriveId() {
        String url = "https://user.aliyundrive.com/v2/user/get";

        HttpEntity<String> entity = new HttpEntity<>(null, this.headers);
        JSONObject response = restTemplate.postForObject(url, entity, JSONObject.class);
        this.driveId = response.getString("default_drive_id");
    }



    private void querySize() {
        String url = "https://api.aliyundrive.com/adrive/v1/user/getUserCapacityInfo";

        HttpEntity<String> entity = new HttpEntity<>(null, this.headers);
        JSONObject response = restTemplate.postForObject(url, entity, JSONObject.class);
        this.total = response.getJSONObject("drive_capacity_details").getLong("drive_total_size");
        this.usable = this.total - response.getJSONObject("drive_capacity_details").getLong("drive_used_size");
    }

    private String mkdir(String parentFileId, String name) {
        String url = "https://api.aliyundrive.com/adrive/v2/file/createWithFolders";

        // body
        JSONObject body = new JSONObject();
        body.put("drive_id", this.driveId);
        body.put("parent_file_id", parentFileId);
        body.put("type", "folder"); // 文件类型，file、folder
        body.put("check_name_mode", "refuse"); // 必须加，否则会创建同名目录
        body.put("name", name);

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, this.headers);
        JSONObject response = restTemplate.postForObject(url, entity, JSONObject.class);
        String fileId = response.getString("file_id");
        return fileId;
    }

    private void trash(String fileId) {
        String url = "https://api.aliyundrive.com/v2/recyclebin/trash";

        // body
        JSONObject body = new JSONObject();
        body.put("drive_id", this.driveId);
        body.put("file_id", fileId);

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, this.headers);
        restTemplate.postForObject(url, entity, JSONObject.class);
    }

    private String getDownloadUrl(String fileId) {
        String url = "https://api.aliyundrive.com/v2/file/get_download_url";

        // body
        JSONObject body = new JSONObject();
        body.put("drive_id", this.driveId);
        body.put("file_id", fileId);

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, this.headers);
        JSONObject response = restTemplate.postForObject(url, entity, JSONObject.class);
        return response.getString("url");
    }

    private List<String> createFile(String parentFileId, File file) {
        String url = "https://api.aliyundrive.com/adrive/v2/file/createWithFolders";

        // body
        JSONObject body = new JSONObject();
        body.put("drive_id", this.driveId);
        body.put("parent_file_id", parentFileId);
        body.put("type", "file"); // 文件类型，file、folder
        body.put("name", MD5Util.getMd5(file));
        body.put("size", file.length());

        Integer partCount = (int)(file.length() % partSize == 0 ? file.length() / partSize : file.length() / partSize + 1);
        JSONObject[] partInfoList = new JSONObject[partCount];
        for (int i = 1; i <= partInfoList.length; i ++)
            partInfoList[i - 1] = new JSONObject().fluentPut("part_number", i);
        body.put("part_info_list", partInfoList);

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, this.headers);
        JSONObject response = restTemplate.postForObject(url, entity, JSONObject.class);

        String fileId = response.getString("file_id"),
                uploadId = response.getString("upload_id");
        List<String> fileInfo = new ArrayList<>();
        fileInfo.add(fileId);
        fileInfo.add(uploadId);
        JSONArray part_info_list = response.getJSONArray("part_info_list");
        for (int i = 0; i < part_info_list.size(); i ++)
            fileInfo.add(part_info_list.getJSONObject(i).getString("upload_url"));
        return fileInfo;
    }

    private void uploadPart(File file, List<String> urls) {
        try (InputStream fis = new FileInputStream(file)) {
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);

                // 计算分片在本地文件中的位置
                long pos = i * this.partSize;
                long size = Math.min(file.length() - pos, this.partSize);
                byte[] partContent = new byte[(int) size];

                // 跳到分片起始位置
                fis.skip(pos);
                // 将文件分片读取到内存中
                fis.read(partContent, 0, (int) size);

                // 上传分片
                RequestBody body = RequestBody.create(null, partContent);
                Request request = new Request.Builder()
                        .url(url)
                        .header("Content-Length", String.valueOf(size))
                        .put(body)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_700002);
        }
    }

    private void uploadComplete(String fileId, String uploadId) {
        String url = "https://api.aliyundrive.com/adrive//v2/file/complete";

        // body
        JSONObject body = new JSONObject();
        body.put("drive_id", this.driveId);
        body.put("file_id", fileId);
        body.put("upload_id", uploadId);

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, this.headers);
        restTemplate.postForObject(url, entity, JSONObject.class);
    }

}
