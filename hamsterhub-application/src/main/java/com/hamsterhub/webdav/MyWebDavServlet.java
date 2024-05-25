package com.hamsterhub.webdav;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.service.FileService;
import com.hamsterhub.service.RedisService;
import com.hamsterhub.service.dto.*;
import com.hamsterhub.service.service.*;
import com.hamsterhub.util.ApplicationContextHelper;
import com.hamsterhub.webdav.resource.WebFileResource;
import org.apache.catalina.WebResource;
import org.apache.catalina.servlets.WebdavServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class MyWebDavServlet extends WebdavServlet {


    private final DeviceService deviceService;
    private final FileService fileService;
    private final FileTool fileTool;

    public MyWebDavServlet(FileTool fileTool,DeviceService deviceService,FileService fileService) {
        this.fileTool = fileTool;
        this.fileService = fileService;
        this.deviceService = deviceService;
    }
    private static final int FIND_BY_PROPERTY = 0;
    private static final int FIND_ALL_PROP = 1;
    private static final int FIND_PROPERTY_NAMES = 2;
    private static final int maxDepth = 1;
    private static final int SC_MULTI_STATUS = 207;

    private AccountDTO getUser(HttpServletRequest req, HttpServletResponse resp){
        AccountDTO user = (AccountDTO) req.getAttribute("user");
        if(user == null){
            throw new BusinessException(CommonErrorCode.E_NO_AUTHORITY);
        }
        return user;
    }
    @Override
    protected void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AccountDTO user = getUser(req,resp);

        String depthStr = req.getHeader("Depth");
        int depth = maxDepth;

        if("0".equals(depthStr)){
            depth = 0;
        }

        Node propNode = null;

        int type = FIND_ALL_PROP;

        if (req.getContentLengthLong() > 0) {
            DocumentBuilder documentBuilder = getDocumentBuilder();

            try {
                Document document = documentBuilder.parse
                        (new InputSource(req.getInputStream()));

                // Get the root element of the document
                Element rootElement = document.getDocumentElement();
                NodeList childList = rootElement.getChildNodes();

                for (int i=0; i < childList.getLength(); i++) {
                    Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            if (currentNode.getNodeName().endsWith("prop")) {
                                type = FIND_BY_PROPERTY;
                                propNode = currentNode;
                            }
                            if (currentNode.getNodeName().endsWith("propname")) {
                                type = FIND_PROPERTY_NAMES;
                            }
                            if (currentNode.getNodeName().endsWith("allprop")) {
                                type = FIND_ALL_PROP;
                            }
                            break;
                    }
                }
            } catch (SAXException e) {
                // Something went wrong - bad request
                resp.sendError(WebdavStatus.SC_BAD_REQUEST);
                return;
            } catch (IOException e) {
                // Something went wrong - bad request
                resp.sendError(WebdavStatus.SC_BAD_REQUEST);
                return;
            }
        }


        String path = getRelativePath(req);
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        resp.setStatus(SC_MULTI_STATUS);
        resp.setContentType("text/xml; charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        writer.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        writer.println("<D:multistatus xmlns:D=\"DAV:\">");

        List<WebFileResource> data = null;

        if ("/".equals(path)) {// 根目录请求

            if (depth == 0) {// 根目录默认值，主要兼容RaiDrive，其余客户端都不会查询根目录是否为目录
                data = WebFileResource.CreateRootFile();
            }else{
                data = fileTool.queryRoot(user);
            }

        } else if (req.getPathInfo().endsWith("/")) {// 子目录请求
            data = fileTool.queryList(path, depth, user);
        } else {// 文件请求
            data = fileTool.queryFile(path, user);
        }

        for (WebFileResource d: data) {
            d.writerWebDavXml(writer);
        }

        writer.println("</D:multistatus>");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AccountDTO user = getUser(req,resp);
        String path = getRelativePath(req);

        if (path == null || path.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file path");
            return;
        }

        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String downloadUrl = fileTool.getFileUrl(path, user);
        StringBuilder baseUrl = new StringBuilder();
        // 将目录回退至/api
        if(!downloadUrl.startsWith("h")){
            int length = req.getPathInfo().split("/").length;
            for (int i = 0; i < length-1; i++) {
                if(i == length-2){
                    baseUrl.append("..");
                }else{
                    baseUrl.append("../");
                }

            }
        }


//        if(!downloadUrl.startsWith("h")){
//            // 如果不是绝对地址
//            try {
//                URI uri = new URI(req.getRequestURL().toString());
//                baseUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + req.getContextPath();
//            } catch (URISyntaxException e) {
//                throw new RuntimeException(e);
//            }
//        }


        resp.sendRedirect(baseUrl.toString() +downloadUrl);


//        RFileDTO rFileDTO = fileTool.getRFileObj(path);
//        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());
//
//        // 本地不存在此文件
//        if (!deviceDTO.getType().equals(0))
//            throw new BusinessException(CommonErrorCode.E_500001);
//
//        String result = fileService.download(rFileDTO);
//
//        // 返回文件 断点续传
//        File file = new File(result);
//        if (!file.exists()) {
//            // 文件不存在
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//        try (OutputStream outputStream = resp.getOutputStream();
//             RandomAccessFile targetFile = new RandomAccessFile(file, "r")) {
//            resp.reset();
//            // 获取请求头中Range的值
//            String rangeString = req.getHeader(HttpHeaders.RANGE);
//
//            long fileLength = targetFile.length();
//            long requestSize = (int) fileLength;
//            // 分段下载视频
//            if (StringUtils.hasText(rangeString)) {
//                // 从Range中提取需要获取数据的开始和结束位置
//                long requestStart = 0, requestEnd = 0;
//                String[] ranges = rangeString.split("=");
//                if (ranges.length > 1) {
//                    String[] rangeDatas = ranges[1].split("-");
//                    requestStart = Integer.parseInt(rangeDatas[0]);
//                    if (rangeDatas.length > 1)
//                        requestEnd = Integer.parseInt(rangeDatas[1]);
//                }
//                if (requestEnd != 0 && requestEnd > requestStart)
//                    requestSize = requestEnd - requestStart + 1;
//                // 根据协议设置请求头
//                resp.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
//                resp.setHeader(HttpHeaders.CONTENT_TYPE, "video/mp4");
//                if (!StringUtils.hasText(rangeString))
//                    resp.setHeader(HttpHeaders.CONTENT_LENGTH, fileLength + "");
//                else {
//                    long length;
//                    if (requestEnd > 0) {
//                        length = requestEnd - requestStart + 1;
//                        resp.setHeader(HttpHeaders.CONTENT_LENGTH, "" + length);
//                        resp.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + requestStart + "-" + requestEnd + "/" + fileLength);
//                    }
//                    else {
//                        length = fileLength - requestStart;
//                        resp.setHeader(HttpHeaders.CONTENT_LENGTH, "" + length);
//                        resp.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + requestStart + "-" + (fileLength - 1) + "/" + fileLength);
//                    }
//                }
//                // 断点传输下载视频返回206
//                resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//                //设置targetFile，从自定义位置开始读取数据
//                targetFile.seek(requestStart);
//            }
//            else {
//                // 如果Range为空则下载整个视频
////                resp.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
//                // 设置文件长度
//                resp.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength));
//            }
//            resp.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//            // 从磁盘读取数据流返回
//            byte[] cache = new byte[4096];
//            try {
//                while (requestSize > 0) {
//                    int len = targetFile.read(cache);
//                    if (requestSize < cache.length)
//                        outputStream.write(cache, 0, (int) requestSize);
//                    else {
//                        outputStream.write(cache, 0, len);
//                        if (len < cache.length)
//                            break;
//                    }
//                    requestSize -= cache.length;
//                }
//            }
//            catch (IOException e) {
//                // tomcat原话。写操作IO异常几乎总是由于客户端主动关闭连接导致，所以直接吃掉异常打日志
//                // 比如使用video播放视频时经常会发送Range为0- 的范围只是为了获取视频大小，之后就中断连接了
//                System.out.println(e.getMessage());
//            }
//            outputStream.flush();
//        }
//        catch (Exception e) {
//            System.out.println("文件传输错误");
//        }
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        AccountDTO user = getUser(req,resp);
        String path = getRelativePath(req);

        if (path == null || path.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file path");
            return;
        }

        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

//        todo
//        if (readOnly) {
//            sendNotAllowed(req, resp);
//            return;
//        }

//        if (isLocked(req)) {
//            resp.sendError(org.apache.catalina.servlets.WebdavStatus.SC_LOCKED);
//            return;
//        }

        if(fileTool.delFileUrl(path, user)){
            resp.setStatus(WebdavStatus.SC_NO_CONTENT);
        }else{
            resp.setStatus(WebdavStatus.SC_FORBIDDEN);
        }

    }

    @Override
    protected void doMkcol(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        AccountDTO user = getUser(req,resp);
        String path = getRelativePath(req);

        if (path == null || path.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file path");
            return;
        }

        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }



//        todo
//        if (readOnly) {
//            resp.sendError(org.apache.catalina.servlets.WebdavStatus.SC_FORBIDDEN);
//            return;
//        }
//
//        if (isLocked(req)) {
//            resp.sendError(org.apache.catalina.servlets.WebdavStatus.SC_LOCKED);
//            return;
//        }


        if (fileTool.mkdirUrl(path, user)) {
            resp.setStatus(WebdavStatus.SC_CREATED);
            // Removing any lock-null resource which would be present
//            lockNullResources.remove(path);
        } else {
            resp.sendError(WebdavStatus.SC_CONFLICT,WebdavStatus.getStatusText(WebdavStatus.SC_CONFLICT));
        }
    }
}



class WebdavStatus {


    // ----------------------------------------------------- Instance Variables


    /**
     * This Hashtable contains the mapping of HTTP and WebDAV
     * status codes to descriptive text.  This is a static
     * variable.
     */
    private static final Hashtable<Integer,String> mapStatusCodes =
            new Hashtable<>();


    // ------------------------------------------------------ HTTP Status Codes


    /**
     * Status code (200) indicating the request succeeded normally.
     */
    public static final int SC_OK = HttpServletResponse.SC_OK;


    /**
     * Status code (201) indicating the request succeeded and created
     * a new resource on the server.
     */
    public static final int SC_CREATED = HttpServletResponse.SC_CREATED;


    /**
     * Status code (202) indicating that a request was accepted for
     * processing, but was not completed.
     */
    public static final int SC_ACCEPTED = HttpServletResponse.SC_ACCEPTED;


    /**
     * Status code (204) indicating that the request succeeded but that
     * there was no new information to return.
     */
    public static final int SC_NO_CONTENT = HttpServletResponse.SC_NO_CONTENT;


    /**
     * Status code (301) indicating that the resource has permanently
     * moved to a new location, and that future references should use a
     * new URI with their requests.
     */
    public static final int SC_MOVED_PERMANENTLY =
            HttpServletResponse.SC_MOVED_PERMANENTLY;


    /**
     * Status code (302) indicating that the resource has temporarily
     * moved to another location, but that future references should
     * still use the original URI to access the resource.
     */
    public static final int SC_MOVED_TEMPORARILY =
            HttpServletResponse.SC_MOVED_TEMPORARILY;


    /**
     * Status code (304) indicating that a conditional GET operation
     * found that the resource was available and not modified.
     */
    public static final int SC_NOT_MODIFIED =
            HttpServletResponse.SC_NOT_MODIFIED;


    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
    public static final int SC_BAD_REQUEST =
            HttpServletResponse.SC_BAD_REQUEST;


    /**
     * Status code (401) indicating that the request requires HTTP
     * authentication.
     */
    public static final int SC_UNAUTHORIZED =
            HttpServletResponse.SC_UNAUTHORIZED;


    /**
     * Status code (403) indicating the server understood the request
     * but refused to fulfill it.
     */
    public static final int SC_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;


    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */
    public static final int SC_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;


    /**
     * Status code (500) indicating an error inside the HTTP service
     * which prevented it from fulfilling the request.
     */
    public static final int SC_INTERNAL_SERVER_ERROR =
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR;


    /**
     * Status code (501) indicating the HTTP service does not support
     * the functionality needed to fulfill the request.
     */
    public static final int SC_NOT_IMPLEMENTED =
            HttpServletResponse.SC_NOT_IMPLEMENTED;


    /**
     * Status code (502) indicating that the HTTP server received an
     * invalid response from a server it consulted when acting as a
     * proxy or gateway.
     */
    public static final int SC_BAD_GATEWAY =
            HttpServletResponse.SC_BAD_GATEWAY;


    /**
     * Status code (503) indicating that the HTTP service is
     * temporarily overloaded, and unable to handle the request.
     */
    public static final int SC_SERVICE_UNAVAILABLE =
            HttpServletResponse.SC_SERVICE_UNAVAILABLE;


    /**
     * Status code (100) indicating the client may continue with
     * its request.  This interim response is used to inform the
     * client that the initial part of the request has been
     * received and has not yet been rejected by the server.
     */
    public static final int SC_CONTINUE = 100;


    /**
     * Status code (405) indicating the method specified is not
     * allowed for the resource.
     */
    public static final int SC_METHOD_NOT_ALLOWED = 405;


    /**
     * Status code (409) indicating that the request could not be
     * completed due to a conflict with the current state of the
     * resource.
     */
    public static final int SC_CONFLICT = 409;


    /**
     * Status code (412) indicating the precondition given in one
     * or more of the request-header fields evaluated to false
     * when it was tested on the server.
     */
    public static final int SC_PRECONDITION_FAILED = 412;


    /**
     * Status code (413) indicating the server is refusing to
     * process a request because the request entity is larger
     * than the server is willing or able to process.
     */
    public static final int SC_REQUEST_TOO_LONG = 413;


    /**
     * Status code (415) indicating the server is refusing to service
     * the request because the entity of the request is in a format
     * not supported by the requested resource for the requested
     * method.
     */
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;


    // -------------------------------------------- Extended WebDav status code


    /**
     * Status code (207) indicating that the response requires
     * providing status for multiple independent operations.
     */
    public static final int SC_MULTI_STATUS = 207;
    // This one collides with HTTP 1.1
    // "207 Partial Update OK"


    /**
     * Status code (418) indicating the entity body submitted with
     * the PATCH method was not understood by the resource.
     */
    public static final int SC_UNPROCESSABLE_ENTITY = 418;
    // This one collides with HTTP 1.1
    // "418 Reauthentication Required"


    /**
     * Status code (419) indicating that the resource does not have
     * sufficient space to record the state of the resource after the
     * execution of this method.
     */
    public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    // This one collides with HTTP 1.1
    // "419 Proxy Reauthentication Required"


    /**
     * Status code (420) indicating the method was not executed on
     * a particular resource within its scope because some part of
     * the method's execution failed causing the entire method to be
     * aborted.
     */
    public static final int SC_METHOD_FAILURE = 420;


    /**
     * Status code (423) indicating the destination resource of a
     * method is locked, and either the request did not contain a
     * valid Lock-Info header, or the Lock-Info header identifies
     * a lock held by another principal.
     */
    public static final int SC_LOCKED = 423;


    // ------------------------------------------------------------ Initializer


    static {
        // HTTP 1.0 status Code
        addStatusCodeMap(SC_OK, "OK");
        addStatusCodeMap(SC_CREATED, "Created");
        addStatusCodeMap(SC_ACCEPTED, "Accepted");
        addStatusCodeMap(SC_NO_CONTENT, "No Content");
        addStatusCodeMap(SC_MOVED_PERMANENTLY, "Moved Permanently");
        addStatusCodeMap(SC_MOVED_TEMPORARILY, "Moved Temporarily");
        addStatusCodeMap(SC_NOT_MODIFIED, "Not Modified");
        addStatusCodeMap(SC_BAD_REQUEST, "Bad Request");
        addStatusCodeMap(SC_UNAUTHORIZED, "Unauthorized");
        addStatusCodeMap(SC_FORBIDDEN, "Forbidden");
        addStatusCodeMap(SC_NOT_FOUND, "Not Found");
        addStatusCodeMap(SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        addStatusCodeMap(SC_NOT_IMPLEMENTED, "Not Implemented");
        addStatusCodeMap(SC_BAD_GATEWAY, "Bad Gateway");
        addStatusCodeMap(SC_SERVICE_UNAVAILABLE, "Service Unavailable");
        addStatusCodeMap(SC_CONTINUE, "Continue");
        addStatusCodeMap(SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
        addStatusCodeMap(SC_CONFLICT, "Conflict");
        addStatusCodeMap(SC_PRECONDITION_FAILED, "Precondition Failed");
        addStatusCodeMap(SC_REQUEST_TOO_LONG, "Request Too Long");
        addStatusCodeMap(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
        // WebDav Status Codes
        addStatusCodeMap(SC_MULTI_STATUS, "Multi-Status");
        addStatusCodeMap(SC_UNPROCESSABLE_ENTITY, "Unprocessable Entity");
        addStatusCodeMap(SC_INSUFFICIENT_SPACE_ON_RESOURCE,
                "Insufficient Space On Resource");
        addStatusCodeMap(SC_METHOD_FAILURE, "Method Failure");
        addStatusCodeMap(SC_LOCKED, "Locked");
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Returns the HTTP status text for the HTTP or WebDav status code
     * specified by looking it up in the static mapping.  This is a
     * static function.
     *
     * @param   nHttpStatusCode [IN] HTTP or WebDAV status code
     * @return  A string with a short descriptive phrase for the
     *                  HTTP status code (e.g., "OK").
     */
    public static String getStatusText(int nHttpStatusCode) {
        Integer intKey = Integer.valueOf(nHttpStatusCode);

        if (!mapStatusCodes.containsKey(intKey)) {
            return "";
        } else {
            return mapStatusCodes.get(intKey);
        }
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Adds a new status code -> status text mapping.  This is a static
     * method because the mapping is a static variable.
     *
     * @param   nKey    [IN] HTTP or WebDAV status code
     * @param   strVal  [IN] HTTP status text
     */
    private static void addStatusCodeMap(int nKey, String strVal) {
        mapStatusCodes.put(Integer.valueOf(nKey), strVal);
    }

}