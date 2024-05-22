package com.hamsterhub.webdav.resource;

import com.hamsterhub.service.dto.VFileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebFileResource {
    private String href;
    private String downloadHref;
    private String name;
    private Boolean isCollection;
    private Map<String,String> prop;


    public WebFileResource(String url, VFileDTO vFileDTO) throws UnsupportedEncodingException {
        name = vFileDTO.getName();
        isCollection = vFileDTO.isDir();
        String path = url + "/" + vFileDTO.getName() + (vFileDTO.isDir() ? "/" : "");
        // 避免特殊字符的影响需要url编码，同时由于历史原因需要将+ 转为为%20 以保证解码结果正确
        href = encodeUrl(url + "/" + vFileDTO.getName() + (vFileDTO.isDir() ? "/" : ""));
        downloadHref = null;
        prop = new HashMap<>();
        prop.put("creationdate",dateAdaptor(vFileDTO.getCreated().toString()));
        prop.put("getlastmodified",dateAdaptor(vFileDTO.getModified().toString()));
        // 目录不加入尺寸
        if (!vFileDTO.isDir()){
            prop.put("getcontentlength",vFileDTO.getSize().toString());
        }
    }

    public void setDownloadHrefAndEncode(String url) throws UnsupportedEncodingException {
        // 避免特殊字符的影响需要url编码，同时由于历史原因需要将+ 转为为%20 以保证解码结果正确
        downloadHref = encodeUrl(url);
    }

    // url编码
    public String encodeUrl(String url) throws UnsupportedEncodingException {
        // 避免特殊字符的影响需要url编码，同时由于历史原因需要将+ 转为为%20 以保证解码结果正确
        return URLEncoder.encode(url, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
    }

    // 日期转换为webdav的格式
    public String dateAdaptor(String dateValue){
        // 解析 ISO 8601 日期字符串
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateValue, isoFormatter);

        // 转换为 Date 对象
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // 创建 WebDAV 日期格式化器
        SimpleDateFormat webDavDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        webDavDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return webDavDateFormat.format(date);
    }


    public void writerWebDavXml(PrintWriter writer) {
        writer.println("<D:response>");

        // 处理下载的情况和查询的情况
        if(downloadHref!=null){
            writer.println("<D:href>" + downloadHref + "</D:href>");
        }else{
            writer.println("<D:href>/dav" + href + "</D:href>");
        }

        writer.println("<D:propstat>");
        writer.println("<D:prop>");
        writer.println("<D:displayname>" + name + "</D:displayname>");
        if (isCollection) {
            writer.println("<D:resourcetype><D:collection/></D:resourcetype>");
        } else {
            writer.println("<D:resourcetype/>");
        }
        if(prop != null){
            for (Map.Entry<String, String> entry : prop.entrySet()) {
                writer.println("<D:" + entry.getKey() + ">" + entry.getValue() + "</D:" + entry.getKey() + ">");
            }
        }

        writer.println("</D:prop>");
        writer.println("<D:status>HTTP/1.1 200 OK</D:status>");
        writer.println("</D:propstat>");
        writer.println("</D:response>");
    }
}
