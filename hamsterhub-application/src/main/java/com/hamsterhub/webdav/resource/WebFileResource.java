package com.hamsterhub.webdav.resource;

import com.hamsterhub.database.dto.VFileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@AllArgsConstructor
public class WebFileResource {
    private String href;
    private String downloadHref;
    private String name;
    private Boolean isCollection;
    private Map<String,String> prop;

    public static List<WebFileResource> CreateRootFile() throws UnsupportedEncodingException {
        List<WebFileResource> data = new ArrayList<>();
        WebFileResource temp = new WebFileResource();
        // 根目录属性默认值
        temp.setName("dav");
        temp.setHref("/");
        temp.setIsCollection(true);
        data.add(temp);
        return data;
    }

    public WebFileResource() throws UnsupportedEncodingException {
        prop = new HashMap<>();
        // 没有时间则返回当前时间
        Date date = new Date();
        SimpleDateFormat webDavDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        webDavDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String data = webDavDateFormat.format(date);
        prop.put("creationdate",data);
        prop.put("getlastmodified",data);
    }

    public WebFileResource(String url, VFileDTO vFileDTO) throws UnsupportedEncodingException {
        name = encodeUrl(vFileDTO.getName());
        isCollection = vFileDTO.isDir();
        // 避免特殊字符的影响需要url编码，同时由于历史原因需要将+ 转为为%20 以保证解码结果正确
        href = encodeUrl(url).replace("%2F","/")+ "/" + encodeUrl(vFileDTO.getName()) + (vFileDTO.isDir() ? "/" : "");
        downloadHref = null;
        prop = new HashMap<>();
        prop.put("creationdate",dateAdaptor(vFileDTO.getCreated()));
        prop.put("getlastmodified",dateAdaptor(vFileDTO.getModified()));
        // 目录不加入尺寸
        if (!vFileDTO.isDir()){
            prop.put("getcontentlength",vFileDTO.getSize().toString());
        }
    }

    public WebFileResource(String url, VFileDTO vFileDTO, boolean isParent) throws UnsupportedEncodingException {
        name = encodeUrl(vFileDTO.getName());
        isCollection = vFileDTO.isDir();
        // 避免特殊字符的影响需要url编码，同时由于历史原因需要将+ 转为为%20 以保证解码结果正确
        href = encodeUrl(url).replace("%2F","/");
        if (!isParent){
            href += "/" + encodeUrl(vFileDTO.getName()) + (vFileDTO.isDir() ? "/" : "");
        }

        downloadHref = null;
        prop = new HashMap<>();
        prop.put("creationdate",dateAdaptor(vFileDTO.getCreated()));
        prop.put("getlastmodified",dateAdaptor(vFileDTO.getModified()));
        // 目录不加入尺寸
        if (!vFileDTO.isDir()){
            prop.put("getcontentlength",vFileDTO.getSize().toString());
        }
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
        webDavDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return webDavDateFormat.format(date);
    }

    // Unix时间戳(毫秒)转WebDav日期
    public String dateAdaptor(Long dateValue){
        Instant instant = Instant.ofEpochMilli(dateValue);

        // 转换为 LocalDateTime 对象（使用 UTC 时区）
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return dateTime.format(formatter);
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
