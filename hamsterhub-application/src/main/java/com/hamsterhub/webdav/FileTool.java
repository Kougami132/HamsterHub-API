package com.hamsterhub.webdav;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.VFileConvert;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.VFileResponse;
import com.hamsterhub.service.FileService;
import com.hamsterhub.common.service.RedisService;
import com.hamsterhub.service.dto.*;
import com.hamsterhub.service.service.*;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.webdav.resource.FilePathData;
import com.hamsterhub.webdav.resource.WebFileResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hamsterhub.controller.StrategyController.separatePermission;

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

//        List<Long> delete = vFileService.delete(vFileDTO.getId());
//        for (Long i: delete) {
//            fileService.delete(rFileService.query(i));
//            rFileService.delete(i);
//        }
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
        VFileDTO vFileDTO = new VFileDTO(null, 0, name, parentId, 0L, 0, LocalDateTime.now(), LocalDateTime.now(), accountDTO.getId(), 0L, strategyDTO.getId(), 0,"");
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileService.createDir(vFileDTO));

        return true;
    }

    public FilePathData parseUrl(String url){
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }
        String root = paths[0];
        String[] arr = splitUrlBack(paths[1]);

        // 处理为空和不以/开头的情况
        String parentUrl = null;
        if(StringUtil.isBlank(arr[0])){
            parentUrl = "/";
        }else{
            parentUrl = arr[0].startsWith("/") ?arr[0]:"/"+arr[0];
        }


        return new FilePathData(paths[0],paths[1],parentUrl,arr[1]);
    }

    public Boolean copy(String url, String destination, AccountDTO accountDTO) {


        FilePathData targetFile = parseUrl(url);
        FilePathData destinationFile = parseUrl(destination);

        if(targetFile == null || destinationFile == null ){
            return false;
        }

        if(StringUtil.isBlank(targetFile.getName())){
            return false;
        }
        Long fileId = 0L;

        if(!StringUtil.isBlank(targetFile.getFileUrl())){
            VFileDTO vFileDTO = queryFile(targetFile.getRoot(), targetFile.getFileUrl(), accountDTO);
            fileId = vFileDTO.getId();
        }

        Long parentId = 0L;

        if(!destinationFile.getParentUrl().equals("/")){
            VFileDTO vFileDTO = queryFile(destinationFile.getRoot(), destinationFile.getParentUrl(), accountDTO);
            parentId = vFileDTO.getId();
        }

        return copy(fileId,parentId,accountDTO);
    }


    public Boolean copy(Long vFileId, Long parentId, AccountDTO accountDTO) {
        VFileDTO vFileDTO = vFileService.query(vFileId);
        VFileDTO vParentDTO;
        if (parentId.equals(0L)) {
            vParentDTO = VFileDTO.rootFileDTO();
            vParentDTO.setAccountID(accountDTO.getId());
            vParentDTO.setStrategyId(vFileDTO.getStrategyId());
        }
        else
            vParentDTO = vFileService.query(parentId);

        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);

        // 目标文件不为目录
        if (!vParentDTO.isDir())
            throw new BusinessException(CommonErrorCode.E_600013);
        // 文件与目标目录不属于同策略
        if (!vFileDTO.getStrategyId().equals(vParentDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_600022);
        // 目标目录已存在同名文件
        if (vFileService.isExist(vFileDTO.getAccountID(), vFileDTO.getStrategyId(), parentId, vFileDTO.getName()))
            throw new BusinessException(CommonErrorCode.E_600016);
        // 目标目录是文件的子目录
        while (!vParentDTO.getId().equals(0L) && !vParentDTO.getParentId().equals(0L)) {
            if (vParentDTO.getParentId().equals(vFileDTO.getId()))
                throw new BusinessException(CommonErrorCode.E_600023);
            vParentDTO = vFileService.query(vParentDTO.getParentId());
        }

        // BFS复制文件
        Queue<VFileDTO> queue = new LinkedList<>();
        Map<Long, Long> map =  new HashMap<>();
        queue.offer(vFileDTO);
        map.put(vFileDTO.getParentId(), parentId);
        while (!queue.isEmpty()) {
            VFileDTO cur = queue.poll();
            if (cur.isDir()) {
                List<VFileDTO> vFileDTOs = vFileService.queryBatch(cur.getAccountID(), cur.getStrategyId(), cur.getId());
                for (VFileDTO i: vFileDTOs)
                    queue.offer(i);
            }
            cur.setParentId(map.get(cur.getParentId()));
            VFileDTO newVFileDTO = vFileService.create(cur);
            map.put(cur.getId(), newVFileDTO.getId());
        }

        return true;
    }

    public Boolean move(String url, String destination, AccountDTO accountDTO) {


        FilePathData targetFile = parseUrl(url);
        FilePathData destinationFile = parseUrl(destination);

        if(targetFile == null || destinationFile == null ){
            return false;
        }

        if(StringUtil.isBlank(targetFile.getName())){
            return false;
        }
        Long fileId = 0L;

        if(!StringUtil.isBlank(targetFile.getFileUrl())){
            VFileDTO vFileDTO = queryFile(targetFile.getRoot(), targetFile.getFileUrl(), accountDTO);
            fileId = vFileDTO.getId();
        }

        Long parentId = 0L;

        if(!destinationFile.getParentUrl().equals("/")){
            VFileDTO vFileDTO = queryFile(destinationFile.getRoot(), destinationFile.getParentUrl(), accountDTO);
            parentId = vFileDTO.getId();
        }

        return move(fileId,parentId,accountDTO);
    }


    public Boolean move(Long vFileId, Long parentId, AccountDTO accountDTO) {
        VFileDTO vFileDTO = vFileService.query(vFileId);
        VFileDTO vParentDTO;
        if (parentId.equals(0L)) {
            vParentDTO = VFileDTO.rootFileDTO();
            vParentDTO.setAccountID(accountDTO.getId());
            vParentDTO.setStrategyId(vFileDTO.getStrategyId());
        }
        else
            vParentDTO = vFileService.query(parentId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);

        // 目标文件不为目录
        if (!vParentDTO.isDir())
            throw new BusinessException(CommonErrorCode.E_600013);
        // 文件与目标目录不属于同策略
        if (!vFileDTO.getStrategyId().equals(vParentDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_600022);
        // 目标目录已存在同名文件
        if (vFileService.isExist(vFileDTO.getAccountID(), vFileDTO.getStrategyId(), parentId, vFileDTO.getName()))
            throw new BusinessException(CommonErrorCode.E_600016);
        // 目标目录是文件的子目录
        while (!vParentDTO.getId().equals(0L) && !vParentDTO.getParentId().equals(0L)) {
            if (vParentDTO.getParentId().equals(vFileDTO.getId()))
                throw new BusinessException(CommonErrorCode.E_600023);
            vParentDTO = vFileService.query(vParentDTO.getParentId());
        }

        List<VFileDTO> vFileDTOs = vFileService.query(vFileDTO.getAccountID(), vFileDTO.getStrategyId(), vFileDTO.getParentId(), vFileDTO.getName());
        for (VFileDTO i: vFileDTOs) {
            i.setParentId(parentId);
            vFileService.update(i);
        }

        // 移动后需要把原来路径的缓存删除
        redisService.delFileId(strategyService.query(vFileDTO.getStrategyId()).getRoot(), accountDTO.getId(), vFileId);

        return true;
    }


    public Boolean upload(File file,String url, AccountDTO accountDTO) {

        FilePathData filePathData = parseUrl(url);

        if(filePathData == null){
            return false;
        }

        StrategyDTO strategyDTO = strategyService.query(filePathData.getRoot());
        Long strategyId = strategyDTO.getId();
        String hash = MD5Util.getMd5(file);
        RFileDTO rFileDTO;
        if (rFileService.isExist(hash, strategyId))
            rFileDTO = rFileService.query(hash, strategyId);
        else
            rFileDTO = fileService.upload(file, strategyDTO);

        Long parentId = 0L;
        if(!filePathData.getParentUrl().equals("/")){
            VFileDTO parentFile = queryFile(filePathData.getRoot(), filePathData.getParentUrl(), accountDTO);
            parentId = parentFile.getId();
        }

        VFileDTO f = VFileDTO.newFile(filePathData.getName(), strategyId, parentId, rFileDTO, accountDTO.getId());
        vFileService.create(f);

        return true;
    }



}
