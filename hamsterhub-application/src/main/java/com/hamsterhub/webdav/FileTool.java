package com.hamsterhub.webdav;

import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.dto.StrategyDTO;
import com.hamsterhub.database.dto.VFileDTO;
import com.hamsterhub.database.service.DeviceService;
import com.hamsterhub.database.service.RFileService;
import com.hamsterhub.database.service.StrategyService;
import com.hamsterhub.database.service.VFileService;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.service.FileService;
import com.hamsterhub.service.service.RedisService;
import com.hamsterhub.service.service.*;
import com.hamsterhub.webdav.resource.FilePathData;
import com.hamsterhub.webdav.resource.WebFileResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private FileService fileService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private FileStorageService fileStorageService;

// ^/([^/]+)(/.*)?
    static Pattern splitUrlPattern = Pattern.compile("^/([^/]+)(/.*)?");
    static Pattern splitUrlBackPattern = Pattern.compile("^(.*?)/?([^/]+)/?$");

    public static String[] splitUrl(String url) {

        Matcher matcher = splitUrlPattern.matcher(url);

        String firstDir = "";
        String remainingPath = "";

        if (matcher.find()) {
            firstDir = matcher.group(1)!= null ? matcher.group(1) : "";
            remainingPath = matcher.group(2) != null ? matcher.group(2) : "";
        }

        return new String[]{firstDir, remainingPath};
    }

    public static String[] splitUrlBack(String url) {
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

    public VFileDTO queryFile(String root, String url, UserDTO userDTO) {
        return fileStorageService.queryFile(root, url, userDTO).get(0);
    }

    public List<WebFileResource> queryList(String url, int depth, UserDTO userDTO) throws UnsupportedEncodingException {

        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];
        // 返回值
        List<WebFileResource> data = new ArrayList<>();

        String parentId = "0";
        VFileDTO vFileDTO = queryFile(root, fileUrl, userDTO);
        if (!"".equals(fileUrl) && !"/".equals(fileUrl) && !"\\".equals(fileUrl)){
            // 如果为空说明是根目录查询,否则需要获取父目录id
            data.add(new WebFileResource(url,vFileDTO,true));
            parentId = vFileDTO.getId();
        }else {
            WebFileResource rootWebFileResource= createWebFileResourceForRoot(root, userDTO);
            data.add(rootWebFileResource);
        }

        if(depth<=0){
            return data;
        }

        // 获取列表
        List<VFileDTO> vFileDTOs = fileStorageService.queryDirectory(root,parentId,userDTO,null,null);

        // 转换为webdav的数据
        for (VFileDTO i: vFileDTOs) {
            data.add(new WebFileResource(url,i));
        }

        return data;
    }

    public boolean hasPermissionRoot(StrategyDTO strategyDTO,UserDTO userDTO){
        return userDTO.isAdmin() ||
                separatePermission(strategyDTO.getPermission()).contains(userDTO.getType());
    }

    public WebFileResource createWebFileResourceForRoot(String root , UserDTO userDTO)
            throws UnsupportedEncodingException {

        WebFileResource res = null;

        StrategyDTO strategyDTO = strategyService.query(root);
        if (hasPermissionRoot(strategyDTO,userDTO)){
            res = new WebFileResource();
            res.setName(strategyDTO.getRoot());
            res.setIsCollection(true); // 策略一定是文件夹
            res.setHref("/" + encodeUrl(strategyDTO.getRoot()) + "/");
        }

        return res;
    }


    public List<WebFileResource> queryRoot(UserDTO userDTO) throws UnsupportedEncodingException {
        List<StrategyDTO> strategyDTOs = strategyService.queryBatch();
        List<WebFileResource> data = new ArrayList<>();
        for (StrategyDTO i:strategyDTOs) {
            // 验证访问权限
            if (hasPermissionRoot(i,userDTO)){
                WebFileResource temp = new WebFileResource();
                temp.setName(i.getRoot());
                temp.setIsCollection(true); // 策略一定是文件夹
                temp.setHref("/" + encodeUrl(i.getRoot()) + "/");
                data.add(temp);
            }
        }
        return data;
    }

    public List<WebFileResource> queryFile(String url, UserDTO userDTO) throws UnsupportedEncodingException {
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];
        // 返回值
        List<WebFileResource> data = new ArrayList<>();
        WebFileResource temp;

        if ( "".equals(fileUrl) || "/".equals(fileUrl) || "\\".equals(fileUrl)){
            // 如果为根目录
            temp = createWebFileResourceForRoot(root, userDTO);
        }else{
            VFileDTO vFileDTO = queryFile(root, fileUrl, userDTO);
            temp = new WebFileResource(url,vFileDTO,true);
        }

        if (temp != null){
            data.add(temp);
        }
        return data;
    }

    public String getFileUrl(String url, UserDTO userDTO) throws UnsupportedEncodingException {
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];

        VFileDTO vFileDTO = queryFile(root, fileUrl, userDTO);
        return fileStorageService.getDownloadUrl(root,vFileDTO.getId(), userDTO,null);
    }

    public String encodeUrl(String url) throws UnsupportedEncodingException {
        // 避免特殊字符的影响需要url编码，同时由于历史原因需要将+ 转为为%20 以保证解码结果正确
        return URLEncoder.encode(url, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
    }

    public Boolean delFileUrl(String url, UserDTO userDTO) throws UnsupportedEncodingException {
        String[] paths = splitUrl(url);

        if("".equals(paths[0])){
            return null;
        }

        String root = paths[0];
        String fileUrl = paths[1];

        VFileDTO vFileDTO = queryFile(root, fileUrl, userDTO);

        return delFile(root ,vFileDTO.getId(), userDTO);
    }

    public Boolean delFile( String root, String vFileId, UserDTO userDTO) {
        fileStorageService.delete(root,vFileId,userDTO);
        return true;
    }

    public Boolean mkdirUrl(String url, UserDTO userDTO) {
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

        String parent = "0";

        if(!StringUtil.isBlank(parentUrl)){
            VFileDTO vFileDTO = queryFile(root, parentUrl, userDTO);
            parent = vFileDTO.getId();
        }

        fileStorageService.makeDirectory(root, parent,dirName,userDTO);
        return true;
    }


    public static FilePathData parseUrl(String url){
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

    public Boolean copy(String url, String destination, UserDTO userDTO) {
        FilePathData targetFile = parseUrl(url);
        FilePathData destinationFile = parseUrl(destination);

        if(targetFile == null || destinationFile == null ){
            return false;
        }

        if (!targetFile.getRoot().equals(destinationFile.getRoot())) {
            return false;
        }

        if(StringUtil.isBlank(targetFile.getName())){
            return false;
        }

        String fileId = "0";

        if(!StringUtil.isBlank(targetFile.getFileUrl())){
            VFileDTO vFileDTO = queryFile(targetFile.getRoot(), targetFile.getFileUrl(), userDTO);
            fileId = vFileDTO.getId();
        }

        String parent = "0";

        if(!destinationFile.getParentUrl().equals("/")){
            VFileDTO vFileDTO = queryFile(destinationFile.getRoot(), destinationFile.getParentUrl(), userDTO);
            parent = vFileDTO.getId();
        }

        fileStorageService.copyTo(targetFile.getRoot(),fileId,parent,userDTO);
        return true;
    }

    public Boolean move(String url, String destination, UserDTO userDTO) {


        FilePathData targetFile = parseUrl(url);
        FilePathData destinationFile = parseUrl(destination);

        if(targetFile == null || destinationFile == null ){
            return false;
        }

        if (!targetFile.getRoot().equals(destinationFile.getRoot())) {
            return false;
        }

        if(StringUtil.isBlank(targetFile.getName())){
            return false;
        }
        String fileId = "0";

        if(!StringUtil.isBlank(targetFile.getFileUrl())){
            VFileDTO vFileDTO = queryFile(targetFile.getRoot(), targetFile.getFileUrl(), userDTO);
            fileId = vFileDTO.getId();
        }

        String parentId = "0";

        if(!destinationFile.getParentUrl().equals("/")){
            VFileDTO vFileDTO = queryFile(destinationFile.getRoot(), destinationFile.getParentUrl(), userDTO);
            parentId = vFileDTO.getId();
        }


        fileStorageService.moveTo(targetFile.getRoot(),fileId,parentId,destinationFile.getName(),userDTO);
        return true;
    }

    public Boolean upload(File file,String url,Long size, UserDTO userDTO) {

        FilePathData filePathData = parseUrl(url);

        if(filePathData == null){
            return false;
        }

        fileStorageService.upload(filePathData.getRoot(),
                file,filePathData.getParentUrl(),
                filePathData.getName(),size,"",userDTO);

        return true;
    }



}
