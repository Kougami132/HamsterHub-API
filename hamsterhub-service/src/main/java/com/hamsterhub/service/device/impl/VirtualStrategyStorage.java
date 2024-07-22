package com.hamsterhub.service.device.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.service.RedisService;
import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.service.device.Storage;
import com.hamsterhub.service.device.ext.AliDrive;
import com.hamsterhub.service.device.ext.LocalDisk;
import com.hamsterhub.service.device.ext.OneDrive;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.Strategy;
import com.hamsterhub.service.entity.VFile;
import com.hamsterhub.service.mapper.VFileMapper;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.service.service.RFileService;
import com.hamsterhub.service.service.VFileService;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VirtualStrategyStorage implements ListFiler {

    public static final Integer fileSystemType = 0;

    private DeviceService deviceService;
    private VFileService vFileService;
    private RFileService rFileService;
    private RedisService redisService;
    private VFileMapper vFileMapper;

    private Long id;
    private String name;
    private Integer type;
    private Integer mode;
    private Integer permission;
    private String root;
    private Integer fileSystem;
    private String param;
    private Integer[] priority;
    private Integer backupTime = 1;
    private List< List<Storage> > devices = new ArrayList<>();
    private Set<Storage> devicesSet = new HashSet<>();
    private static Map<Long, Storage> deviceMap = new ConcurrentHashMap<>();
    private Boolean ready = false;
    private Storage tempDevice = new LocalDisk(new DeviceDTO());

    public VirtualStrategyStorage(Strategy strategy){
        this.id = strategy.getId();
        this.name = strategy.getName();
        this.type = strategy.getType();
        this.mode = strategy.getMode();
        this.permission = strategy.getPermission();
        this.root = strategy.getRoot();
        this.fileSystem = strategy.getFileSystem();
        this.param = strategy.getParam();

        // 获取依赖
        this.deviceService = GetBeanUtil.getBean(DeviceService.class);
        this.vFileService = GetBeanUtil.getBean(VFileService.class);
        this.rFileService = GetBeanUtil.getBean(RFileService.class);
        this.redisService = GetBeanUtil.getBean(RedisService.class);
        this.vFileMapper = GetBeanUtil.getBean(VFileMapper.class);

        // 构建临时的设备
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setId(-1L);
        this.tempDevice = new LocalDisk(deviceDTO);

        this.init();
    }

    public void init() {
        if (StringUtil.isBlank(this.param)){
            this.ready = false;
            return;
        }
        int startPos = 0;
        String[] combines = this.param.split("#");

        if (combines.length > 1){
            // 第一个表示备份次数
            startPos = 1;
            this.backupTime = Integer.parseInt(combines[0]);
        }else{
            this.backupTime = 1;
        }

        for (int i = startPos; i < combines.length; i++) {
            String[] split = combines[i].split(",");
            List<Storage> temp = new ArrayList<>();
            for (String s : split) {
                Long StorageId = Long.parseLong(s);
                Storage storageDevices = deviceMap.get(StorageId);
                if (storageDevices == null){
                    storageDevices = createDevices(s);
                    // 全局备用
                    deviceMap.put(StorageId, storageDevices);
                }

                // 策略需要使用的
                devicesSet.add(storageDevices);
                temp.add(storageDevices);
            }

            devices.add(temp);
        }


        this.ready = true;
    }

    public Storage createDevices(String deviceId) {
        DeviceDTO deviceDTO = this.deviceService.query(Long.parseLong(deviceId));

        // 默认Storage是未就绪的
        Storage res = new Storage();
        if (deviceDTO != null){
            Integer deviceType = deviceDTO.getType();

            switch (deviceType){
                case 0:
                    res = new LocalDisk(deviceDTO);
                    break;
                case 1:
                    res = new AliDrive(deviceDTO);
                    break;
                case 2:
                    res = new OneDrive();
                    break;
            }
        }
        return res;
    }

    @Override
    public Integer getFileSystem() {
        return fileSystem;
    }

    @Override
    public Boolean isReady() {
        return  this.ready;
    }

    @Override
    public Boolean isExist(String hash){
        // 三种状态
        // 完全没有，不可用
        // 有，但是只有临时存储区，可用
        // 有，但是不是完全符合策略规则，可用
        // 有，但是device没在策略下，不可用
        // 对于不可用返回false，对于可用返回true
        // 临时的设备id为-1
        List<RFileDTO> rFileDTOS = rFileService.queryByHash(hash);

        if (rFileDTOS.isEmpty()){
            return false;
        }
        Long id = null;
        for (RFileDTO rFileDTO : rFileDTOS){
            id = rFileDTO.getId();
            // 临时的设备id为-1
            if (devicesSet.contains(id) || id.equals(-1L)){
                return true;
            }
        }

        return false;
    }

    public Boolean isExist(String hash, Long size){
        // 三种状态
        // 完全没有，不可用
        // 有，但是只有临时存储区，可用
        // 有，但是不是完全符合策略规则，可用
        // 有，但是device没在策略下，不可用
        // 对于不可用返回false，对于可用返回true
        // 临时的设备id为-1
        List<RFileDTO> rFileDTOS = rFileService.queryByHash(hash,size);

        if (rFileDTOS.isEmpty()){
            return false;
        }
        Long id = null;
        for (RFileDTO rFileDTO : rFileDTOS){
            id = rFileDTO.getDeviceId();
            // 临时的设备id为-1
            if (devicesSet.contains(id) || id.equals(-1L)){
                return true;
            }
        }

        return false;
    }

    @Override
    public List<VFileDTO> queryFile(String url, Long userId) {

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
            vFileId = redisService.getFileId(root, userId, path);
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
            vFileDTOs = vFileService.query(userId, root, vFileId, name);
            vFileDTO = vFileDTOs.get(0);
            vFileId = vFileDTO.getId();
            path += "/" + name;
            // 把路径与ID键值对写入redis
            redisService.setFileId(root, userId, path, vFileId);

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
            vFileDTOs = vFileService.query(userId, vFileDTO.getStrategyId(), vFileDTO.getParentId(), vFileDTO.getName());
        }

        // 是目录则把文件数存入size字段
        if (vFileDTO.getType().equals(0))
            vFileDTO.setSize(Long.valueOf(vFileService.queryCount(vFileDTO.getId())));

        List<VFileDTO> dataList = new ArrayList<>();
        if (vFileDTO.getVersion().equals(1))
            dataList.add(vFileDTO);
        else
            dataList = vFileDTOs;
        return dataList;
    }

    @Override
    public List<VFileDTO> queryDirectory(String parentId, Long userId, Integer page, Integer limit) {
//        vFileDTOs = vFileService.queryBatch(userId, root, Long.parseLong(parentId) , page, limit);
        // 暂时不考虑分页
        return vFileService.queryBatch(userId, root, Long.parseLong(parentId) );
    }

    @Override
    public VFileDTO makeDirectory(String parent, String name, Long userId){
        VFileDTO vFileDTO = VFileDTO.newDir(name, this.id, Long.parseLong(parent), userId);
        return vFileService.createDir(vFileDTO);
    }

    @Override
    public Integer queryFileCount(String index){
        return vFileService.queryCount(Long.parseLong(index)) ;
    }

    @Override
    public void delete(String index, Long userId){
        VFileDTO vFileDTO = vFileService.query(Long.parseLong(index));

        // root与策略id不一致，大概率初始化时出现错误
        CommonErrorCode.checkAndThrow(!vFileDTO.getStrategyId().equals(this.id), CommonErrorCode.E_600024);

        // 文件与用户不匹配
        CommonErrorCode.checkAndThrow(!vFileDTO.getAccountID().equals(userId), CommonErrorCode.E_600005);

        // 删除缓存
        redisService.delFileId(this.root, userId,Long.parseLong(index));

        List<String> deleteHash = vFileService.delete(vFileDTO.getId());

        for (String i: deleteHash) {
            List<RFileDTO> rFileDTOS = rFileService.queryByHash(i);
            for (RFileDTO rFileDTO : rFileDTOS) {
                Long deviceId = rFileDTO.getDeviceId();
                String path = rFileDTO.getPath();
                // 临时目录
                if (deviceId.equals(-1L)){
                    new File(path).delete();
                }else{ // 调用对应设备删除
                    Storage storage = deviceMap.get(deviceId);
                    storage.delete(path);
                }

                rFileService.delete(rFileDTO.getId());
            }

        }

    }

    @Override
    public void rename(String index, String name, Long userId) {

        VFileDTO vFileDTO = vFileService.query(Long.parseLong(index));
        // root与策略id不一致，大概率初始化时出现错误
        CommonErrorCode.checkAndThrow(!vFileDTO.getStrategyId().equals(this.id), CommonErrorCode.E_600024);

        // 文件与用户不匹配
        CommonErrorCode.checkAndThrow(!vFileDTO.getAccountID().equals(userId), CommonErrorCode.E_600005);

        // 新文件名已存在
        if (vFileService.isExist(userId, vFileDTO.getStrategyId(), vFileDTO.getParentId(), name))
            throw new BusinessException(CommonErrorCode.E_600015);

        LambdaUpdateWrapper<VFile> updateWrapper = new LambdaUpdateWrapper<VFile>()
                .eq(VFile::getAccountID, vFileDTO.getAccountID())
                .eq(VFile::getStrategyId, vFileDTO.getStrategyId())
                .eq(VFile::getParentId, vFileDTO.getParentId())
                .eq(VFile::getName, vFileDTO.getName())
                .set(VFile::getName, name);

        vFileMapper.update(null, updateWrapper);
    }

    private VFileDTO createParent(Long userId ,Long parentId){
        VFileDTO vParentDTO;
        if (parentId.equals(0L) )
            vParentDTO = VFileDTO.rootFileDTO(userId,this.id);
        else
            vParentDTO = vFileService.query(parentId);
        return vParentDTO;
    }

    @Override
    public void copyTo(String index, String parent, Long userId) {
        VFileDTO vFileDTO = vFileService.query(Long.parseLong(index));

        // root与策略id不一致，大概率初始化时出现错误
        CommonErrorCode.checkAndThrow(!vFileDTO.getStrategyId().equals(this.id), CommonErrorCode.E_600024);

        Long parentId = Long.parseLong(parent);
        VFileDTO vParentDTO = this.createParent(userId, parentId);

        // 文件与用户不匹配
        CommonErrorCode.checkAndThrow(!vFileDTO.getAccountID().equals(userId), CommonErrorCode.E_600005);

        // 目标文件不为目录
        CommonErrorCode.checkAndThrow(!vParentDTO.isDir(), CommonErrorCode.E_600013);

        // 文件与目标目录不属于同策略
        CommonErrorCode.checkAndThrow(!vParentDTO.getStrategyId().equals(this.id), CommonErrorCode.E_600022);

        // 目标目录已存在同名文件
        while (vFileService.isExist(userId, this.id, parentId, vFileDTO.getName()))
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

    }

    @Override
    public void moveTo(String index, String parent, Long userId) {
        VFileDTO vFileDTO = vFileService.query(Long.parseLong(index));

        // root与策略id不一致，大概率初始化时出现错误
        CommonErrorCode.checkAndThrow(!vFileDTO.getStrategyId().equals(this.id), CommonErrorCode.E_600024);

        Long parentId = Long.parseLong(parent);
        VFileDTO vParentDTO = this.createParent(userId, parentId);

        // 文件与用户不匹配
        CommonErrorCode.checkAndThrow(!vFileDTO.getAccountID().equals(userId), CommonErrorCode.E_600005);

        // 目标文件不为目录
        CommonErrorCode.checkAndThrow(!vParentDTO.isDir(), CommonErrorCode.E_600013);

        // 文件与目标目录不属于同策略
        CommonErrorCode.checkAndThrow(!vParentDTO.getStrategyId().equals(this.id), CommonErrorCode.E_600022);

        // 目标目录已存在同名文件
        while (vFileService.isExist(userId, this.id, parentId, vFileDTO.getName()))
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
        redisService.delFileId(this.root, userId, vFileDTO.getId());

    }

    @Override
    public void uploadBefore(String parent, String name, Long userId) {

        // 获取父目录id
        VFileDTO vFileDTO;
        if ("0".equals(parent)) {
            vFileDTO = VFileDTO.rootFileDTO(userId, this.id);
        }else {
            vFileDTO = vFileService.query(Long.parseLong(parent));
        }

        // root与策略id不一致，大概率初始化时出现错误
        CommonErrorCode.checkAndThrow(!vFileDTO.getStrategyId().equals(this.id), CommonErrorCode.E_600024);

        // 文件与用户不匹配
        CommonErrorCode.checkAndThrow(!vFileDTO.getAccountID().equals(userId), CommonErrorCode.E_600005);

    }

    @Override
    public VFileDTO upload(String parent, File file, String name, Long size, Long userId, String hash) {
        Long parentId = Long.parseLong(parent);

        // 版本控制功能导致复杂度提高，故移除
        // 取出版本最新的VFile
//        List<VFile> vFiles = vFileMapper.selectList(new LambdaQueryWrapper<VFile>()
//                .eq(VFile::getAccountID, userId)
//                .eq(VFile::getStrategyId, this.id)
//                .eq(VFile::getParentId, parentId)
//                .eq(VFile::getName, name));
//        vFiles.sort((o1, o2) -> o2.getVersion() - o1.getVersion());
//
//        Integer version = vFiles.size() + 1;
//
//        // version == 1 说明vFiles.size()为0，vFiles.get(0)会报错
//        if (version!=1 && version.equals(vFiles.get(0).getVersion())) {
//            version = vFiles.get(0).getVersion() + 1;
//        }

        // 创建真实文件记录
        if (!this.isExist(hash, size)){

            // 如果file为null说明想要秒传
            CommonErrorCode.checkAndThrow(file == null, CommonErrorCode.E_600001);

            RFileDTO rFileDTO = RFileDTO.createTemp(file.getName() ,hash, file.getPath(), size);
            rFileService.createTemp(rFileDTO);
        }

        // 记录先创建
        VFileDTO vFileDTO = VFileDTO.newFile(name, this.id, parentId, size, userId, hash);
        vFileDTO.setVersion(1); // 取消版本控制功能
        vFileDTO = vFileService.create(vFileDTO);

        return vFileDTO;

    }

    private RFileDTO selectRFileDTO(List<RFileDTO> rFileDTOS, Long target){
        if (target == null){
            return null;
        }

        for (RFileDTO i: rFileDTOS) {
            Long deviceId = i.getDeviceId();
            if (target.equals(deviceId)) {
                return i;
            }
        }

        return null;
    }

    private RFileDTO findRFileDTOInDevices(List<RFileDTO> rFileDTOS){
        RFileDTO res = null;
        for (List<Storage> combine : devices){
            for (Storage device: combine){
                res = selectRFileDTO(rFileDTOS, device.getDevice().getId());
                // 找到退出
                if (res != null){
                    return res;
                }
            }
        }

        return res;
    }

    @Override
    public String getDownloadUrl(String index, Long userId, Long preference){
        VFileDTO vFileDTO = vFileService.query(Long.parseLong(index));

        // root与策略id不一致，大概率初始化时出现错误
        CommonErrorCode.checkAndThrow(!vFileDTO.getStrategyId().equals(this.id), CommonErrorCode.E_600024);

        // 文件与用户不匹配
        CommonErrorCode.checkAndThrow(!vFileDTO.getAccountID().equals(userId), CommonErrorCode.E_600005);

        List<RFileDTO> rFileDTOS = rFileService.queryByHash(vFileDTO.getHash());

        RFileDTO selectedRFileDTO = null;

        if (preference == null){// 如果没有指定偏
            // 根据列表逐个搜索，默认越前面的设备优先级越高
            selectedRFileDTO = findRFileDTOInDevices(rFileDTOS);

            // 如果全部查不到就尝试从临时设备找
            if (preference == null){
                selectedRFileDTO = selectRFileDTO(rFileDTOS, -1L);
            }
        }else { // 如果指定偏好
            // 则只搜索指定的id
            selectedRFileDTO = selectRFileDTO(rFileDTOS, preference);
        }

        // 找不到说明文件不存在
        CommonErrorCode.checkAndThrow(selectedRFileDTO == null, CommonErrorCode.E_500001);

        Long deviceId = selectedRFileDTO.getDeviceId();

        if (deviceId == -1L){
            return this.tempDevice.downLoad(selectedRFileDTO.getId().toString()) +
                    "&fileName=" +
                    vFileDTO.getName();
        }

        Storage storage = deviceMap.get(selectedRFileDTO.getDeviceId());

        String url = storage.downLoad(selectedRFileDTO.getPath());

        if (storage.getDevice().getType().equals(0)){ // 本地硬盘时，为统一接口，不把东西传进去
            url = url + "&fileName=" + vFileDTO.getName();
        }

        return url;
    }

    @Override
    public Integer getCombineNumber(){
        return devices.size();
    }

    @Override
    public Long getTotalSize(Integer combineOption) {
        int position = 0;
        if (combineOption != null){
            position = combineOption;
        }

        if (position>= devices.size()){
            return 0L;
        }

        List<Storage> combine = devices.get(position);

        long size = 0L;

        for (Storage device: combine){
            size += device.getTotalSize();
        }

        return size;
    }

    @Override
    public Long getUsableSize(Integer combineOption) {
        int position = 0;
        if (combineOption != null){
            position = combineOption;
        }

        if (position>= devices.size()){
            return 0L;
        }

        List<Storage> combine = devices.get(position);

        long size = 0L;

        for (Storage device: combine){
            size += device.getUsableSize();
        }

        return size;
    }


}
