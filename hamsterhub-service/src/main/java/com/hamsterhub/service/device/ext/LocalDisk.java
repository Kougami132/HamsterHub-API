package com.hamsterhub.service.device.ext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.device.ListStorage;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.FileLinkDTO;
import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.service.FileLinkService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class LocalDisk extends ListStorage {

    private Integer code = 0;
    private String name = "本地";
    private DeviceDTO device;
    private String path = "";

    private FileLinkService fileLinkService;

    @Override
    public String downloadIndexAdapt(RFileDTO rFileDTO){
        // 默认使用path的内容作为文件的索引
        return rFileDTO.getId().toString();
    }

    public LocalDisk(DeviceDTO deviceDTO) {
        super(deviceDTO);
        this.device = deviceDTO;

        JSONObject param = JSON.parseObject(deviceDTO.getParam());
        if (param != null){
            this.path = param.getString("path");
            if (StringUtil.isNotBlank(this.path) && !this.path.endsWith("/")){
                this.path += "/";
            }

        }

        if (StringUtil.isBlank(this.path)){
            this.path = "";
        }

        fileLinkService = GetBeanUtil.getBean(FileLinkService.class);

        this.device.setConnected(true);
    }

    @Override
    public String upload(File file, String name, String hash) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("/yyyy/MM/dd"));
        File dir = new File(path + "uploads" + today);
        if (!dir.exists()) dir.mkdirs();
//        String hash = MD5Util.getMd5(file);
        String url = dir.getAbsolutePath() + File.separator + name;
        try {
            Files.copy(file.toPath(), Paths.get(url), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public String downLoad(String url) {
        // 传入的变量应当是 rFileDTO id

        Long rFileDTOId = Long.parseLong(url);
        FileLinkDTO fileLinkDTO;
        String ticket;
        if (fileLinkService.isExist(rFileDTOId)) { // 文件直链已存在
            fileLinkDTO = fileLinkService.query(rFileDTOId);
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

            fileLinkDTO = new FileLinkDTO(ticket, rFileDTOId, LocalDateTime.now().plusMinutes(10));
            fileLinkService.create(fileLinkDTO);
        }
        return String.format("/download?ticket=%s", fileLinkDTO.getTicket());
    }

    @Override
    public void delete(String url) {
        File file = new File(url);
        try {
            file.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long getTotalSize() {
        File dir = new File(path + "uploads");
        if (!dir.exists()) dir.mkdirs();
        return dir.getTotalSpace();
    }

    @Override
    public Long getUsableSize() {
        File dir = new File(path + "uploads");
        if (!dir.exists()) dir.mkdirs();
        return dir.getUsableSpace();
    }

    private String mergePath(String path) {
        String filePath = this.path;
        if (path.startsWith("/")){
            filePath += path.substring(1);
        }else{
            filePath += path;
        }
        return filePath;
    }

    private String createParentPathId(String parent) {
        String filePath = parent;

        if (!parent.startsWith("/")){
            filePath = "/" + parent;
        }

        if (!filePath.endsWith("/")){
            filePath += "/";
        }

        return filePath;
    }

    private VFileDTO createVFileDTOByPath(Path path){
        VFileDTO vFileDTO = new VFileDTO();

        try {
            vFileDTO.setName(path.getFileName().toString());

            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

            // 转换为unix时间戳
            vFileDTO.setCreated (attr.creationTime().toInstant().toEpochMilli());

            vFileDTO.setModified (attr.creationTime().toInstant().toEpochMilli());

            if (attr.isDirectory()){
                vFileDTO.setType(0);
                vFileDTO.setSize(0L);
            }else {
                vFileDTO.setType(1);
                vFileDTO.setSize(attr.size());
            }

            vFileDTO.setVersion(1);

        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        return vFileDTO;
    }

    @Override
    public VFileDTO queryFile(String fileIndex){
        // 标准化目录
        String filePath = mergePath(fileIndex);
        Path path = Paths.get(filePath);

        // 不存在
        CommonErrorCode.checkAndThrow(!Files.exists(path),CommonErrorCode.E_500001);

        VFileDTO vFileDTO = createVFileDTOByPath(path);
        vFileDTO.setId(fileIndex);
        return vFileDTO;
    }

    @Override
    public List<VFileDTO> queryDirectory(String parentIndex, Integer page, Integer limit){
        String filePath = mergePath(parentIndex);
        Path path = Paths.get(filePath);

        String parentFilePath = createParentPathId(parentIndex);

        // 不存在或者不是文件夹
        CommonErrorCode.checkAndThrow(!Files.isDirectory(path),CommonErrorCode.E_500001);

        List<VFileDTO> vFileDTOList = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                VFileDTO vFileDTO = createVFileDTOByPath(entry);
                vFileDTO.setId(parentFilePath + vFileDTO.getName());
                vFileDTOList.add(vFileDTO);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return vFileDTOList;
    }

    @Override
    public VFileDTO makeDirectory(String parentIndex, String name){
        String parentPathId = createParentPathId(parentIndex);
        String filePath = mergePath(parentPathId + name);
        Path path = Paths.get(filePath);

        // 创建目录
        try {
            Files.createDirectories(path);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        VFileDTO vFileDTO = createVFileDTOByPath(path);
        vFileDTO.setId(parentPathId + name);
        return vFileDTO;
    }

    private void deleteDirectoryAndContents(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Override
    public void deleteByPath(String fileIndex) {
        String filePath = mergePath(fileIndex);
        Path path = Paths.get(filePath);
        try {
            deleteDirectoryAndContents(path);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VFileDTO uploadTo(String parentIndex, File file, String name) {
        String parentPathId = createParentPathId(parentIndex);
        String parentFilePath = mergePath(parentPathId);
        String filePath = parentFilePath + name;
        Path parentPath = Paths.get(parentFilePath);
        Path path = Paths.get(filePath);
        // 父目录不存在
        CommonErrorCode.checkAndThrow(!Files.exists(parentPath),CommonErrorCode.E_500001);
        file.renameTo(new File(filePath));
        VFileDTO vFileDTO = createVFileDTOByPath(path);
        vFileDTO.setId(filePath);
        return vFileDTO;
    }

    @Override
    public String downLoadByPath(String fileIndex) {
        String filePath = mergePath(fileIndex);
        Path path = Paths.get(filePath);

        // 目标不存在
        CommonErrorCode.checkAndThrow(!Files.exists(path),CommonErrorCode.E_500001);

        // 目标是文件夹
        CommonErrorCode.checkAndThrow(Files.isDirectory(path),CommonErrorCode.E_500012);

        Long rFileDTOId = -1L;
        FileLinkDTO fileLinkDTO;
        String ticket;

        do {
            ticket = StringUtil.generateRandomString(10);
        }
        while (fileLinkService.isExist(ticket));

        fileLinkDTO = new FileLinkDTO(ticket, rFileDTOId, LocalDateTime.now().plusMinutes(10), filePath);
        fileLinkService.create(fileLinkDTO);

        return String.format("/download?ticket=%s", fileLinkDTO.getTicket());
    }

    @Override
    public void rename(String index, String name){
        String filePath = mergePath(index);
        Path sourcePath = Paths.get(filePath);
        String newName = name.replaceAll("[/\\\\]","");
        // 目标不存在
        CommonErrorCode.checkAndThrow(!Files.exists(sourcePath),CommonErrorCode.E_500001);

        Path directoryPath = sourcePath.getParent();
        Path targetPath = directoryPath.resolve(newName);

        // 新名称已经存在
        CommonErrorCode.checkAndThrow(Files.exists(targetPath),CommonErrorCode.E_500001);

        try {
            // 使用 Files.move 方法重命名文件
            Files.move(sourcePath, targetPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 复制目录及其内容
    private static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetSubDir = targetDir.resolve(sourceDir.relativize(dir));
                if (Files.notExists(targetSubDir)) {
                    Files.createDirectory(targetSubDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = targetDir.resolve(sourceDir.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void copyTo(String index, String parent){
        String filePath = mergePath(index);
        String parentPath = mergePath(parent);
        Path sourcePath = Paths.get(filePath);
        String newName = sourcePath.getFileName().toString();

//        if (StringUtil.isBlank(name)){
//            newName = sourcePath.getFileName().toString();
//        }else {
//            newName = name.replaceAll("[/\\\\]","");
//        }
        // 目标不存在
        CommonErrorCode.checkAndThrow(!Files.exists(sourcePath),CommonErrorCode.E_500001);

        Path directoryPath = Paths.get(parentPath);
        Path targetPath = directoryPath.resolve(newName);

        try {
            if (Files.isDirectory(sourcePath)){
                copyDirectory(sourcePath, targetPath);
            }else{
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void moveDirectory(Path sourceDir, Path targetDir) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetSubDir = targetDir.resolve(sourceDir.relativize(dir));
                if (Files.notExists(targetSubDir)) {
                    Files.createDirectory(targetSubDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = targetDir.resolve(sourceDir.relativize(file));
                Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // 删除空目录
                if (dir != sourceDir) {
                    Files.delete(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void moveTo(String index, String parent, String name){
        String filePath = mergePath(index);
        String parentPath = mergePath(parent);
        Path sourcePath = Paths.get(filePath);
        String newName = null;

        if (StringUtil.isBlank(name)){
            newName = sourcePath.getFileName().toString();
        }else {
            newName = name.replaceAll("[/\\\\]","");
        }

        // 目标不存在
        CommonErrorCode.checkAndThrow(!Files.exists(sourcePath),CommonErrorCode.E_500001);

        Path directoryPath = Paths.get(parentPath);
        Path targetPath = directoryPath.resolve(newName);

        try {
            if (Files.isDirectory(sourcePath)){
                moveDirectory(sourcePath, targetPath);
                Files.delete(sourcePath);
            }else{
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
