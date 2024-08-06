package com.hamsterhub.service.schedule;

import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.downloader.DownloadState;
import com.hamsterhub.service.dto.*;
import com.hamsterhub.service.service.*;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.web.client.RestTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RSSCheckService {
    @Autowired
    private RSSService rssService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DownloadTaskListService downloadTaskListService;

    @Autowired
    private AccountService accountService;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(cron = "0 30 * * * ?") // 每小时查询一次RSS订阅
    public void RSSCheck() throws Exception {

        log.info("RSS定时任务开始执行");
        List<RSSListDTO> rssListDTOS = rssService.fetchAllRSSList();
        for (RSSListDTO rssListDTO : rssListDTOS) {
            String rssContent = fetchRssContent(rssListDTO.getUrl());
            String md5 = MD5Util.getMd5(rssContent);

            if (md5 == null || md5.equals(rssListDTO.getLastHash())) {
                continue;
            }

            AccountDTO accountDTO = accountService.query(rssListDTO.getUserId());

            try {
                List<RSSTaskDTO> rssTaskDTOS = parseRSSXml(rssContent, rssListDTO.getId(),rssListDTO.getUserId(),
                        rssListDTO.getReplaceHost(),rssListDTO.getMirrorHost());

                rssService.createRSSTasks(rssTaskDTOS);
                // 创建对应下载任务
                for (RSSTaskDTO rssTaskDTO : rssTaskDTOS) {
//                    String savePath = "temp/torrents/";
//                    this.downloadTorrent(rssTaskDTO.getUrl(),savePath);
                    if (rssTaskDTO.getId()==null){
                        // 没id说明已经创建过下载任务了，跳过
                        continue;
                    }

                    String parentIndex = getParentIndex(rssListDTO, rssTaskDTO, accountDTO);
                    if (parentIndex == null) {
                        // 为null说明目录冲突
                        rssTaskDTO.setState(DownloadState.ERROR.ordinal());
                        rssService.updateRSSTask(rssTaskDTO);// 更改状态为异常
                        continue;
                    }
                    DownloadTaskListDTO downloadTaskListDTO =
                            DownloadTaskListDTO.createTask(rssListDTO,rssTaskDTO,parentIndex);

                    downloadTaskListService.create(downloadTaskListDTO);
                }

            } catch (Exception e){
                log.info("RSS订阅出现未知异常: {}", e.getMessage());
                continue;
            }

            rssListDTO.setLastHash(md5);
            rssService.updateRSSList(rssListDTO);
        }
    }

    private String fetchRssContent(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return response.body().string();
        }
    }


    private List<RSSTaskDTO> parseRSSXml(String xml, Long rssListId, Long userId, String replaceHost, String mirrorHost)
            throws Exception {

        List<RSSTaskDTO> rssListDTOS = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));

        doc.getDocumentElement().normalize();
        NodeList itemList = doc.getElementsByTagName("item");


        for (int i = 0; i < itemList.getLength(); i++) {
            RSSTaskDTO temp = new RSSTaskDTO();

            temp.setUserId(userId);
            temp.setRssListId(rssListId);
            temp.setState(0);

            Element item = (Element) itemList.item(i);

            Element e = getElementByTagName("title",item);
            temp.setTitle(e.getTextContent());

            e = getElementByTagName("torrent",item);
            e = getElementByTagName("pubDate",e);
            LocalDateTime localDateTime = LocalDateTime.parse(e.getTextContent(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            temp.setPubDate(localDateTime);

            e = getElementByTagName("enclosure",item);
            String url = e.getAttribute("url");
            // 替换域名，用于处理镜像站的场景
            if (StringUtil.isNotEmpty(url)) {
                if (StringUtil.isNotEmpty(replaceHost)) {
                    url = StringUtil.replaceDomain(url, replaceHost);
                }

                if (StringUtil.isNotEmpty(mirrorHost)) {
                    url = mirrorHost + url;
                }
            }

            temp.setUrl(url);
            temp.setSize(Long.parseLong(e.getAttribute("length")));

            rssListDTOS.add(temp);
        }

        return rssListDTOS;
    }

    private Element getElementByTagName(String tagName, Element item){
        NodeList nodeList = item.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return (Element) nodeList.item(0);
        }
        return null;
    }

    public String getParentIndex(RSSListDTO rssListDTO,RSSTaskDTO rssTaskDTO,AccountDTO accountDTO){
        String res = null;
        String title = rssTaskDTO.getTitle();
        String[] split = title.split("/");

        if(split.length <= 1){
            return rssListDTO.getParentIndex();
        }

        String queryUrl = fileStorageService
                .getQueryUrl(rssListDTO.getRoot(), rssListDTO.getParentIndex(), accountDTO);

        String curParentIndex = rssListDTO.getParentIndex();
        // 提前创建好前面部分的目录
        for (int i = 0; i < split.length - 1; i++) {
            if (!queryUrl.endsWith("/")){
                queryUrl += "/";
            }
            queryUrl += split[i].trim();
            List<VFileDTO> vFileDTOS = null;
            VFileDTO dir;
            try {
                vFileDTOS = fileStorageService.queryFile(rssListDTO.getRoot(), queryUrl, accountDTO);
            }catch (Exception ignored){} // 报错说明不存在


            if(vFileDTOS ==null || vFileDTOS.isEmpty()){
                // 没有则创建
                dir = fileStorageService.makeDirectory(rssListDTO.getRoot(),curParentIndex,split[i],accountDTO);
            }else if (!vFileDTOS.get(0).isDir()){
                // 目录冲突
                return res;
            }else {
                dir = vFileDTOS.get(0);
            }
            curParentIndex = dir.getId();
        }
        res = curParentIndex;
        return res;
    }

//    public boolean downloadTorrent(String url, String savePath) {
//        Request request = new Request.Builder()
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
//                .url(url)
//                .build();
//
//        try (Response response = httpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                throw new IOException("Unexpected code " + response);
//            }
//
//            String md5 = MD5Util.getMd5(response.body().toString());
//            File file = new File(savePath+md5);
//
//            try (InputStream inputStream = response.body().byteStream();
//                 FileOutputStream outputStream = new FileOutputStream(file)) {
//
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, bytesRead);
//                }
//            }
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }





}
