package com.hamsterhub.webdav;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.StrategyConvert;
import com.hamsterhub.convert.VFileConvert;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.StrategyResponse;
import com.hamsterhub.response.VFileResponse;
import com.hamsterhub.service.FileService;
import com.hamsterhub.service.RedisService;
import com.hamsterhub.service.dto.*;
import com.hamsterhub.service.service.*;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.webdav.resource.WebFileResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hamsterhub.controller.StrategyController.separatePermission;
import static java.util.stream.Collectors.toList;

@Component
public class FileTool {

    @Autowired
    private RFileService rFileService;
    @Autowired
    private VFileService vFileService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private FileLinkService fileLinkService;
    @Autowired
    private FileService fileService;
    @Autowired
    private RedisService redisService;

    Pattern splitUrlPattern = Pattern.compile("^/([^/]+)(/.*)?");
    Pattern splitUrlBackPattern = Pattern.compile("^(.*)/([^/]+)$");

    public String[] splitUrl(String url) {

        Matcher matcher = splitUrlPattern.matcher(url);

        String firstDir = "";
        String remainingPath = "";

        if (matcher.find()) {
            firstDir = matcher.group(1)!= null ? matcher.group(1) : "";
            remainingPath = matcher.group(2) != null ? matcher.group(2) : "";
        }

        return new String[]{firstDir, remainingPath};
    }

    public String[] splitUrlBack(String url) {
        Matcher matcher = splitUrlBackPattern.matcher(url);

        if (matcher.find()) {
            // 获取前半部分和后半部分
            String firstPart = matcher.group(1);
            String secondPart = matcher.group(2);
            return new String[]{firstPart, secondPart};
        }

        // 如果没有匹配成功，返回原路径和空字符串
        return new String[]{url, ""};
    }

    public Response isExist(String root, String hash) {
        StrategyDTO strategyDTO = strategyService.query(root);
        return Response.success().data(rFileService.isExist(hash, strategyDTO.getId()));
    }

    public VFileDTO queryFile(String root, String url, AccountDTO accountDTO) {
//        AccountDTO accountDTO = SecurityUtil.getAccount();

        // 路径格式错误
        if (!MatchUtil.isPathMatches(url))
            throw new BusinessException(CommonErrorCode.E_600002);

        List<String> split = Arrays.asList(url.split("/"));
        Integer num = split.size() - 1;
        // 找到最深的有缓存的目录
        String path = "";
        Long vFileId = null;
        while (num >= 1) {
            path = "/" + split.subList(1, num + 1)
                    .stream()
                    .collect(Collectors.joining("/"));
            // 读取redis里缓存的ID
            vFileId = redisService.getFileId(root, accountDTO.getId(), path);
            if (vFileId != null) // 拿到缓存则跳出
                break;
            num --;
        }

        // 全路径无缓存
        if (vFileId == null) {
            vFileId = 0L;
            path = "";
        }
        num ++;
        VFileDTO vFileDTO = null;
        List<VFileDTO> vFileDTOs = null;
        while (num <= split.size() - 1) {
            String name = split.get(num);
            vFileDTOs = vFileService.query(accountDTO.getId(), root, vFileId, name);
            vFileDTO = vFileDTOs.get(0);

            vFileId = vFileDTO.getId();

            path += "/" + name;
            // 把路径与ID键值对写入redis
            redisService.setFileId(root, accountDTO.getId(), path, vFileId);

            // 文件类型不是目录
            if (!vFileDTO.getType().equals(0)) {
                if (num == split.size() - 1)
                    break;
                else
                    throw new BusinessException(CommonErrorCode.E_600003);
            }

            num ++;
        }

        if (vFileDTO == null) {
            vFileDTO = vFileService.query(vFileId);
        }

        return  vFileDTO;
    }

    public List<VFileDTO> queryList(String root,Long parentId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        return vFileService.queryBatch(accountDTO.getId(), root, parentId);
    }
    public List<WebFileResource> queryList(String url, int depth, AccountDTO accountDTO) throws UnsupportedEncodingException {

        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];
        // 返回值
        List<WebFileResource> data = new ArrayList<>();

        Long parentId = 0L;
        if (!"".equals(fileUrl) && !"/".equals(fileUrl)){
            // 如果为空说明是根目录查询,否则需要获取父目录id
            VFileDTO vFileDTO = queryFile(root, fileUrl, accountDTO);
            if(depth<=0){
                data.add(new WebFileResource(url,vFileDTO));
                return data;
            }
            parentId = vFileDTO.getId();
        }

        // 获取列表
        List<VFileDTO> vFileDTOs = vFileService.queryBatch(accountDTO.getId(), root, parentId);

        // 转换为webdav的数据
        for (VFileDTO i: vFileDTOs) {
            data.add(new WebFileResource(url,i));
        }

        return data;
    }


    public List<WebFileResource> queryRoot(AccountDTO accountDTO) throws UnsupportedEncodingException {
        List<StrategyDTO> strategyDTOs = strategyService.queryBatch();
        List<WebFileResource> data = new ArrayList<>();
        for (StrategyDTO i:strategyDTOs) {
            // 验证访问权限
            if (accountDTO.isAdmin() || separatePermission(i.getPermission()).contains(accountDTO.getType())){
                WebFileResource temp = new WebFileResource();

                temp.setName(i.getRoot());
                temp.setIsCollection(true); // 策略一定是文件夹
//                temp.setHref("/" + i.getRoot() + "/");
                temp.setHrefAndEncode("/" + i.getRoot() + "/");
                data.add(temp);
            }
        }
        return data;
    }

    public List<WebFileResource> queryFile(String url, AccountDTO accountDTO) throws UnsupportedEncodingException {
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];
        // 返回值
        List<WebFileResource> data = new ArrayList<>();

        VFileDTO vFileDTO = queryFile(root, fileUrl, accountDTO);

        WebFileResource temp = new WebFileResource(url,vFileDTO);

//        String downloadUrl = getDownloadUrl(vFileDTO.getId(),accountDTO);
//        temp.setDownloadHrefAndEncode(downloadUrl);

        data.add(temp);

        return data;
    }

    public String getDownloadUrl(Long vFileId, AccountDTO accountDTO) {

        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);
        RFileDTO rFileDTO = rFileService.query(vFileDTO.getRFileId());
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());

        String url;
        if (deviceDTO.getType().equals(0)) {// 本地硬盘
            FileLinkDTO fileLinkDTO;
            String ticket;
            if (fileLinkService.isExist(rFileDTO.getId())) { // 文件直链已存在
                fileLinkDTO = fileLinkService.query(rFileDTO.getId());
                if (fileLinkDTO.getExpiry().isBefore(LocalDateTime.now())) { // 直链已过期
                    do {
                        fileLinkDTO.setTicket(StringUtil.generateRandomString(10));
                    }
                    while (fileLinkService.isExist(fileLinkDTO.getTicket()));
                }
                fileLinkDTO.setExpiry(LocalDateTime.now().plusMinutes(10));
                fileLinkService.update(fileLinkDTO);
            }
            else {
                do {
                    ticket = StringUtil.generateRandomString(10);
                }
                while (fileLinkService.isExist(ticket));

                fileLinkDTO = new FileLinkDTO(ticket, rFileDTO.getId(), LocalDateTime.now().plusMinutes(10));
                fileLinkService.create(fileLinkDTO);
            }

            url = String.format("/download?ticket=%s&fileName=%s", fileLinkDTO.getTicket(), vFileDTO.getName());
        }
        else // 网盘
            url = fileService.download(rFileDTO);
        return url;
    }

    public String getFileUrl(String url, AccountDTO accountDTO) throws UnsupportedEncodingException {
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];

        VFileDTO vFileDTO = queryFile(root, fileUrl, accountDTO);

        return getDownloadUrl(vFileDTO.getId(),accountDTO);
    }
    public String encodeUrl(String url) throws UnsupportedEncodingException {
        // 避免特殊字符的影响需要url编码，同时由于历史原因需要将+ 转为为%20 以保证解码结果正确
        return URLEncoder.encode(url, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
    }


    public RFileDTO getRFileObj(String url) throws UnsupportedEncodingException {
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];

        // 构造user
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(0L);
        VFileDTO vFileDTO = queryFile(root, fileUrl, accountDTO);

        return rFileService.query(vFileDTO.getRFileId());
    }


    public Boolean delFileUrl(String url, AccountDTO accountDTO) throws UnsupportedEncodingException {
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];

        VFileDTO vFileDTO = queryFile(root, fileUrl, accountDTO);

        return delFile(vFileDTO.getId(), accountDTO);
    }

    public Boolean delFile(Long vFileId, AccountDTO accountDTO) {
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);

        // 删除缓存
        redisService.delFileId(strategyService.query(vFileDTO.getStrategyId()).getRoot(), accountDTO.getId(), vFileId);

        List<Long> delete = vFileService.delete(vFileDTO.getId());
        for (Long i: delete) {
            fileService.delete(rFileService.query(i));
            rFileService.delete(i);
        }
        return true;
    }

    public Boolean mkdirUrl(String url, AccountDTO accountDTO) {
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];

        String[] arr = splitUrlBack(fileUrl);
        String parentUrl = arr[0];
        String dirName = arr[1];

        if(StringUtil.isBlank(dirName)){
            return false;
        }
        Long parentId = 0L;

        if(!StringUtil.isBlank(parentUrl)){
            VFileDTO vFileDTO = queryFile(root, parentUrl, accountDTO);
            parentId = vFileDTO.getId();
        }

        return mkdir(root,parentId,dirName,accountDTO);
    }

    public Boolean mkdir(String root, Long parentId, String name, AccountDTO accountDTO) {
        StrategyDTO strategyDTO = strategyService.query(root);
        VFileDTO vFileDTO = new VFileDTO(null, 0, name, parentId, 0L, 0, LocalDateTime.now(), LocalDateTime.now(), accountDTO.getId(), 0L, strategyDTO.getId(), 0);
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileService.createDir(vFileDTO));

        return true;
    }




}
