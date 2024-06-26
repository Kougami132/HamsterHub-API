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
import com.hamsterhub.service.RedisService;
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
import java.io.*;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
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

    @Operation(summary ="查询文件是否存在(token)")
    @GetMapping(value = "/isExist")
    @Token
    public Response isExist(@RequestParam("root") String root,
                            @RequestParam("hash") String hash) {
        StrategyDTO strategyDTO = strategyService.query(root);
        return Response.success().data(rFileService.isExist(hash, strategyDTO.getId()));
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
            vFileDTOs = vFileService.query(accountDTO.getId(), vFileDTO.getStrategyId(), vFileDTO.getParentId(), vFileDTO.getName());
        }


        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);

        // 是目录则把文件数存入size字段
        if (data.getType().equals(0))
            data.setSize(vFileService.queryCount(vFileDTO.getId()).toString());

        List<VFileResponse> dataList = new ArrayList<>();
        if (vFileDTO.getVersion().equals(1))
            dataList.add(data);
        else
            dataList = VFileConvert.INSTANCE.dto2resBatch(vFileDTOs);
        return Response.success().data(dataList);
    }

    @Operation(summary ="查看文件列表(token)")
    @GetMapping(value = "/queryList")
    @Token
    public Response queryList(@RequestParam("root") String root,
                              @RequestParam("parentId") Long parentId,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "limit", required = false) Integer limit) {
        if (limit == null)
            limit = 10;

        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<VFileDTO> vFileDTOs;
        if (page == null) // 未分页
            vFileDTOs = vFileService.queryBatch(accountDTO.getId(), root, parentId);
        else
            vFileDTOs = vFileService.queryBatch(accountDTO.getId(), root, parentId, page, limit);

        List<VFileResponse> data = VFileConvert.INSTANCE.dto2resBatch(vFileDTOs);
        return Response.success().data(data);
    }

    @Operation(summary ="创建目录(token)")
    @PostMapping(value = "/mkdir")
    @Token
    public Response mkdir(@RequestParam("root") String root,
                          @RequestParam("parentId") Long parentId,
                          @RequestParam("name") String name) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        StrategyDTO strategyDTO = strategyService.query(root);
        VFileDTO vFileDTO = VFileDTO.newDir(name, strategyDTO.getId(), parentId, accountDTO.getId());
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileService.createDir(vFileDTO));

        return Response.success().data(data);
    }

    @Operation(summary ="查询目录内文件数(token)")
    @GetMapping(value = "/queryFileCount")
    @Token
    public Response queryFileCount(@RequestParam("fileId") Long fileId) {
        return Response.success().data(vFileService.queryCount(fileId));
    }

    @SneakyThrows
    @Operation(summary ="上传文件(hash可选)(token)")
    @PostMapping(value = "/upload")
    @Token
    public Response upload(@RequestParam("root") String root,
                           @RequestParam("parentId") Long parentId,
                           @RequestParam("file") MultipartFile file,
                           @RequestParam(value = "hash", required = false) String hash) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        String name = file.getOriginalFilename();
        // 文件名为空
        if (StringUtil.isBlank(name))
            throw new BusinessException(CommonErrorCode.E_600004);

        // 文件大小校验
        double imageSize = (double) file.getSize() / 1024 / 1024;
        if (imageSize > 200)
            throw new BusinessException(CommonErrorCode.E_500005);

        // 覆盖
        // 文件是否存在,存在则版本号+1
        Integer version = 1;
        if (vFileService.isExist(accountDTO.getId(), root, parentId, name))
            version = vFileService.query(accountDTO.getId(), root, parentId, name).size() + 1;

        StrategyDTO strategyDTO = strategyService.query(root);
        // 存储文件
        RFileDTO rFileDTO;
        if (!StringUtil.isBlank(hash)) { // 上传已有文件
            if (!rFileService.isExist(hash, strategyDTO.getId()))
                throw new BusinessException(CommonErrorCode.E_500001);
            rFileDTO = rFileService.query(hash, strategyDTO.getId());
        }
        else { // 上传新文件
            File tmpFile = File.createTempFile("tmp", "tmp");
            file.transferTo(tmpFile);
            rFileDTO = fileService.upload(tmpFile, strategyDTO);
        }

        VFileDTO vFileDTO = VFileDTO.newFile(name, strategyDTO.getId(), parentId, rFileDTO, accountDTO.getId());
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileService.create(vFileDTO));

        return Response.success().msg("上传成功").data(data);
    }

    @Operation(summary ="获取文件直链(token)")
    @GetMapping(value = "/getDownloadUrl")
    @Token
    public Response getDownloadUrl(@RequestParam("vFileId") Long vFileId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
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
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());

        // 本地不存在此文件
        if (!deviceDTO.getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_500001);

        String result = fileService.download(rFileDTO);

        // 返回文件 断点续传
        File file = new File(result);
        if (!file.exists()) {
            System.out.println("实际文件不存在");
            return;
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
    public Response delete(@RequestParam("vFileId") Long vFileId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
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
        return Response.success().msg("文件删除成功");
    }

    @Operation(summary ="重命名文件(token)")
    @PostMapping(value = "/rename")
    @Token
    public Response rename(@RequestParam("vFileId") Long vFileId,
                           @RequestParam("name") String name) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);

        vFileService.rename(vFileId, name);
        return Response.success().msg("重命名成功");
    }

    @Operation(summary ="复制文件(token)")
    @PostMapping(value = "/copy")
    @Token
    public Response copy(@RequestParam("vFileId") Long vFileId,
                         @RequestParam("parentId") Long parentId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
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
        while (vFileService.isExist(vFileDTO.getAccountID(), vFileDTO.getStrategyId(), parentId, vFileDTO.getName()))
            vFileDTO.setName(StringUtil.generateCopy(vFileDTO.getName()));
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

        return Response.success().msg("复制成功");
    }

    @Operation(summary ="移动文件(token)")
    @PostMapping(value = "/move")
    @Token
    public Response move(@RequestParam("vFileId") Long vFileId,
                         @RequestParam("parentId") Long parentId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
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
        while (vFileService.isExist(vFileDTO.getAccountID(), vFileDTO.getStrategyId(), parentId, vFileDTO.getName()))
            vFileDTO.setName(StringUtil.generateCopy(vFileDTO.getName()));
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
