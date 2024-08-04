package com.hamsterhub.service.schedule;

import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.dto.RSSListDTO;
import com.hamsterhub.service.dto.RSSTaskDTO;
import com.hamsterhub.service.service.RSSService;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RSSCheckService {
    @Autowired
    private RSSService rssService;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Transactional
    @Scheduled(cron = "0 0 * * * ?") // 每小时查询一次RSS订阅
    public void RSSCheck() throws Exception {

        log.info("RSS定时任务开始执行");
        List<RSSListDTO> rssListDTOS = rssService.fetchAllRSSList();
        for (RSSListDTO rssListDTO : rssListDTOS) {
            String rssContent = fetchRssContent(rssListDTO.getUrl());
            String md5 = MD5Util.getMd5(rssContent);

            if (md5 == null || md5.equals(rssListDTO.getLastHash())) {
                continue;
            }

            try {
                List<RSSTaskDTO> rssTaskDTOS = parseRSSXml(rssContent, rssListDTO.getId(),rssListDTO.getUserId());
                log.info(rssTaskDTOS.toString());
                rssService.createRSSTasks(rssTaskDTOS);
            } catch (Exception e){
                log.info("RSS订阅出现未知异常: {}", rssListDTO.toString());
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


    private List<RSSTaskDTO> parseRSSXml(String xml, Long rssListId, Long userId) throws Exception {
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
            temp.setUrl(e.getAttribute("url"));
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

}
