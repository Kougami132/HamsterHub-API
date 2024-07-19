package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RestController
@Tag(name = "文件传输 数据接口")
public class FileController {

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
    @Autowired
    private FileStorageService fileStorageService;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024 * 1024; // 10GB
    private static final Pattern FILENAME_PATTERN = Pattern.compile("filename=\"(.*?)\"");
    private static final long MAX_UPLOAD_SPEED = 100 * 1024 * 1024; // 10MB/s per second


    @Operation(summary ="查询文件是否存在(token)")
    @GetMapping(value = "/isExist")
    @Token
    public Response isExist(@RequestParam("root") String root,
                            @RequestParam("hash") String hash) {
        return Response.success().data(fileStorageService.isExist(root,hash));
    }

    @Operation(summary ="查看文件详情(token)")
    @GetMapping(value = "/queryFile")
    @Token
    public Response queryFile(@RequestParam("root") String root,
                              @RequestParam("url") String url) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        // 路径格式错误
        if (!MatchUtil.isPathMatches(url))
            throw new BusinessException(CommonErrorCode.E_600002);

        List<VFileDTO> vFileDTOs = fileStorageService.queryFile(root, url, accountDTO);
        List<VFileResponse> dataList = VFileConvert.INSTANCE.dto2resBatch(vFileDTOs);
        return Response.success().data(dataList);
    }

    @Operation(summary ="查看文件列表(token)")
    @GetMapping(value = "/queryList")
    @Token
    public Response queryList(@RequestParam("root") String root,
                              @RequestParam("parentId") String parentId,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "limit", required = false) Integer limit) {
        if (limit == null)
            limit = 10;

        if (page == null)
            page = 0;

        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<VFileDTO> vFileDTOs;
        vFileDTOs = fileStorageService.queryDirectory(root,parentId,accountDTO,page,limit);
        List<VFileResponse> data = VFileConvert.INSTANCE.dto2resBatch(vFileDTOs);
        return Response.success().data(data);
    }

    @Operation(summary ="创建目录(token)")
    @PostMapping(value = "/mkdir")
    @Token
    public Response mkdir(@RequestParam("root") String root,
                          @RequestParam("parentId") String parent,
                          @RequestParam("name") String name) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        // 防止空空字符串
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(name),CommonErrorCode.E_100001);
        VFileDTO vFileDTO = fileStorageService.makeDirectory(root, parent, name, accountDTO);
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);
        return Response.success().data(data);
    }

    @Operation(summary ="查询目录内文件数(token)")
    @GetMapping(value = "/queryFileCount")
    @Token
    public Response queryFileCount(@RequestParam("root") String root,
                                   @RequestParam("fileId") String index) {
        return Response.success().data(fileStorageService.queryFileCount(root,index));
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("Content-Disposition");
        if (contentDisposition != null) {
            Matcher matcher = FILENAME_PATTERN.matcher(contentDisposition);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    @SneakyThrows
    @Operation(summary ="上传文件(hash可选)(token)")
    @PostMapping(value = "/upload")
    @Token
    public Response upload(@RequestParam("root") String root,
                           @RequestParam("parentId") String parent,
                           @RequestParam(value = "hash", required = false) String hash,
                           HttpServletRequest request) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        Collection<Part> parts = request.getParts();
        Part filePart = null;
        for (Part part : parts) {
            if ("file".equals(part.getName())) {
                filePart = part;
                break;
            }
        }

        CommonErrorCode.checkAndThrow(filePart == null, CommonErrorCode.E_500010);

        assert filePart != null;
        long declaredFileSize = filePart.getSize();
        String name = getFileName(filePart);
        // 验证大小
        CommonErrorCode.checkAndThrow(declaredFileSize > MAX_FILE_SIZE, CommonErrorCode.E_500005);

        // 防止文件名为空
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(name), CommonErrorCode.E_500011);

        // file存储实现层的上传之前的验证
        fileStorageService.uploadBefore(root,parent,name,accountDTO);

        File targetFile = null;

        // 如果没上传hash则说明试图上传一个新文件
        if (StringUtil.isBlank(hash)) {

            // 保存文件
            InputStream inputStream = filePart.getInputStream();
            targetFile = new File("temp/uploads/" , UUID.randomUUID().toString());

            // 创建父目录
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }

            OutputStream outputStream = new FileOutputStream(targetFile);


            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = 0;
            long startTime = System.currentTimeMillis();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead > declaredFileSize) {
                    outputStream.close();
                    targetFile.delete();
                    throw new BusinessException(CommonErrorCode.E_500009);
                }

                // 控制上传速度
                long elapsedTime = System.currentTimeMillis() - startTime;
                long expectedTime = (totalBytesRead * 1000L) / MAX_UPLOAD_SPEED;
                if (expectedTime > elapsedTime) {
                    Thread.sleep(expectedTime - elapsedTime);
                }

                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            if (totalBytesRead != declaredFileSize) {
                targetFile.delete();
                throw new BusinessException(CommonErrorCode.E_500009);
            }
        }


        VFileDTO upload = fileStorageService.upload(root, targetFile, parent, name, declaredFileSize, hash, accountDTO);
        VFileResponse data = VFileConvert.INSTANCE.dto2res(upload);

        return Response.success().msg("上传成功").data(data);
    }

    @Operation(summary ="获取文件直链(token)")
    @GetMapping(value = "/getDownloadUrl")
    @Token
    public Response getDownloadUrl(@RequestParam("root") String root,
                                   @RequestParam("vFileId") String index,
                                   @RequestParam(value = "preference", required = false) Long preference) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
//        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
//        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
//            throw new BusinessException(CommonErrorCode.E_600005);
//        RFileDTO rFileDTO = rFileService.query(vFileDTO.getRFileId());
//        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());

        String url;
        url = fileStorageService.getDownloadUrl(root,index,accountDTO,preference);
//        if (deviceDTO.getType().equals(0)) {// 本地硬盘
//            FileLinkDTO fileLinkDTO;
//            String ticket;
//            if (fileLinkService.isExist(rFileDTO.getId())) { // 文件直链已存在
//                fileLinkDTO = fileLinkService.query(rFileDTO.getId());
//                if (fileLinkDTO.getExpiry().isBefore(LocalDateTime.now())) { // 直链已过期
//                    do {
//                        fileLinkDTO.setTicket(StringUtil.generateRandomString(10));
//                    }
//                    while (fileLinkService.isExist(fileLinkDTO.getTicket()));
//                }
//                fileLinkDTO.setExpiry(LocalDateTime.now().plusMinutes(10));
//                fileLinkService.update(fileLinkDTO);
//            }
//            else {
//                do {
//                    ticket = StringUtil.generateRandomString(10);
//                }
//                while (fileLinkService.isExist(ticket));
//
//                fileLinkDTO = new FileLinkDTO(ticket, rFileDTO.getId(), LocalDateTime.now().plusMinutes(10));
//                fileLinkService.create(fileLinkDTO);
//            }
//
//            url = String.format("/download?ticket=%s&fileName=%s", fileLinkDTO.getTicket(), vFileDTO.getName());
//        }
//        else // 网盘
//            url = fileService.download(rFileDTO);
        return Response.success().data(url);
    }

    @Operation(summary ="下载文件")
    @GetMapping(value = "/download")
    public void download(@RequestParam("ticket") String ticket,
                             @RequestParam("fileName") String fileName,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        FileLinkDTO fileLinkDTO = fileLinkService.query(ticket);
        // 直链已过期
        if (fileLinkDTO.getExpiry().isBefore(LocalDateTime.now()))
            throw new BusinessException(CommonErrorCode.E_600018);
        RFileDTO rFileDTO = rFileService.query(fileLinkDTO.getRFileId());
        String result = null;

        if (rFileDTO.getDeviceId().equals(-1L)){// 临时目录
            result = rFileDTO.getPath();
        }else {
            DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());

            // 本地不存在此文件
            if (!deviceDTO.getType().equals(0))
                throw new BusinessException(CommonErrorCode.E_500001);

            result = fileService.download(rFileDTO);
        }

        // 返回文件 断点续传
        File file = new File(result);
        if (!file.exists()) {
            throw new BusinessException(CommonErrorCode.E_500001);
//            System.out.println("实际文件不存在");
//            return;
        }
        try (OutputStream outputStream = response.getOutputStream();
             RandomAccessFile targetFile = new RandomAccessFile(file, "r")) {
            response.reset();
            // 获取请求头中Range的值
            String rangeString = request.getHeader(HttpHeaders.RANGE);

            long fileLength = targetFile.length();
            long requestSize = (int) fileLength;
            // 分段下载视频
            if (StringUtils.hasText(rangeString)) {
                // 从Range中提取需要获取数据的开始和结束位置
                long requestStart = 0, requestEnd = 0;
                String[] ranges = rangeString.split("=");
                if (ranges.length > 1) {
                    String[] rangeDatas = ranges[1].split("-");
                    requestStart = Integer.parseInt(rangeDatas[0]);
                    if (rangeDatas.length > 1)
                        requestEnd = Integer.parseInt(rangeDatas[1]);
                }
                if (requestEnd != 0 && requestEnd > requestStart)
                    requestSize = requestEnd - requestStart + 1;
                // 根据协议设置请求头
                response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
                response.setHeader(HttpHeaders.CONTENT_TYPE, "video/mp4");
                if (!StringUtils.hasText(rangeString))
                    response.setHeader(HttpHeaders.CONTENT_LENGTH, fileLength + "");
                else {
                    long length;
                    if (requestEnd > 0) {
                        length = requestEnd - requestStart + 1;
                        response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + length);
                        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + requestStart + "-" + requestEnd + "/" + fileLength);
                    }
                    else {
                        length = fileLength - requestStart;
                        response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + length);
                        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + requestStart + "-" + (fileLength - 1) + "/" + fileLength);
                    }
                }
                // 断点传输下载视频返回206
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                //设置targetFile，从自定义位置开始读取数据
                targetFile.seek(requestStart);
            }
            else {
                // 如果Range为空则下载整个视频
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
                // 设置文件长度
                response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength));
            }
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            // 从磁盘读取数据流返回
            byte[] cache = new byte[4096];
            try {
                while (requestSize > 0) {
                    int len = targetFile.read(cache);
                    if (requestSize < cache.length)
                        outputStream.write(cache, 0, (int) requestSize);
                    else {
                        outputStream.write(cache, 0, len);
                        if (len < cache.length)
                            break;
                    }
                    requestSize -= cache.length;
                }
            }
            catch (IOException e) {
                // tomcat原话。写操作IO异常几乎总是由于客户端主动关闭连接导致，所以直接吃掉异常打日志
                // 比如使用video播放视频时经常会发送Range为0- 的范围只是为了获取视频大小，之后就中断连接了
                System.out.println(e.getMessage());
            }
            outputStream.flush();
        }
        catch (Exception e) {
            System.out.println("文件传输错误");
        }

    }

    @Operation(summary ="删除文件(token)")
    @PostMapping(value = "/delete")
    @Token
    public Response delete(@RequestParam("root") String root,
                           @RequestParam("vFileId") String vFileId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(root),CommonErrorCode.E_100001);
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(vFileId),CommonErrorCode.E_100001);
        fileStorageService.delete(root,vFileId,accountDTO);
        return Response.success().msg("文件删除成功");
    }

    @Operation(summary ="重命名文件(token)")
    @PostMapping(value = "/rename")
    @Token
    public Response rename(@RequestParam("root") String root,
                           @RequestParam("vFileId") String vFileId,
                           @RequestParam("name") String name) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(name),CommonErrorCode.E_100001);
        fileStorageService.rename(root,vFileId,name,accountDTO);
        return Response.success().msg("重命名成功");
    }

    @Operation(summary ="复制文件(token)")
    @PostMapping(value = "/copy")
    @Token
    public Response copy(@RequestParam("root") String root,
                         @RequestParam("vFileId") String vFileId,
                         @RequestParam("parentId") String parent) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        fileStorageService.copyTo(root,vFileId,parent,accountDTO);
        return Response.success().msg("复制成功");
    }

    @Operation(summary ="移动文件(token)")
    @PostMapping(value = "/move")
    @Token
    public Response move(@RequestParam("root") String root,
                         @RequestParam("vFileId") String vFileId,
                         @RequestParam("parentId") String parent) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        fileStorageService.moveTo(root,vFileId,parent,accountDTO);
        return Response.success().msg("移动成功");
    }

    @Operation(summary ="上传头像(token)")
    @PostMapping(value = "/uploadAvatar")
    @Token
    public Response uploadAvatar(@RequestParam("file") MultipartFile file) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        // 格式校验
        List<String> uploadImageTypes = Stream.of("image/png", "image/jpeg").collect(toList());
        String fileType = file.getContentType();
        boolean flag = false;
        for (String type : uploadImageTypes) {
            if (fileType.equals(type)) {
                flag = true;
                break;
            }
        }
        if (!flag)
            throw new BusinessException(CommonErrorCode.E_500004);

        // 文件大小校验
        double imageSize = (double) file.getSize() / 1024 / 1024;
        if (imageSize > 2)
            throw new BusinessException(CommonErrorCode.E_500005);

        fileService.uploadAvatar(accountDTO.getId(), file);

        return Response.success().msg("上传成功");
    }

    @Operation(summary ="获取头像")
    @GetMapping(value = "/queryAvatar")
    public void queryAvatar(@RequestParam("accountId") Long accountId,
                            HttpServletResponse response) {
        String imageUrl = fileService.queryAvatar(accountId);

        FileInputStream in = null;
        OutputStream out = null;
        try {
            File file = new File(imageUrl);
            in = new FileInputStream(imageUrl);
            int i = in.available();
            byte[] buffer = new byte[i];
            in.read(buffer);
            //设置输出流内容格式为图片格式
            response.setContentType("image/jpeg");
            //response的响应的编码方式为utf-8
            response.setCharacterEncoding("utf-8");
            out = response.getOutputStream();
            out.write(buffer);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_NETWORK_ERROR);
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                throw new BusinessException(CommonErrorCode.E_NETWORK_ERROR);
            }
        }
    }

}
