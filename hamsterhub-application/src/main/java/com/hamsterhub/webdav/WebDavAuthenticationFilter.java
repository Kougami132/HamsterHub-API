package com.hamsterhub.webdav;

import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.service.UserService;
import com.hamsterhub.database.service.DeviceService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

// 为webdav提供认证 Filter
public class WebDavAuthenticationFilter implements Filter {
    private UserService userService;

    public WebDavAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    private final String realm = "HamsterHub";
    private final String key = "GMXCVFWEAWT^UMUYN";

    private Boolean CheckPassword(String username, String password, HttpServletRequest request){
        UserDTO userDTO = userService.query(username);
        Boolean res = userDTO.getPassword().equals(MD5Util.getMd5(password));
        if(res){
            request.setAttribute("user", userDTO);
        }
        // 密码错误
        return res;
    }

    private void handleBasicAuth(String authHeader, HttpServletResponse response, FilterChain chain, HttpServletRequest request) throws IOException, ServletException {
        String base64Credentials = authHeader.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] values = credentials.split(":", 2);
        String user = values[0];
        String pass = values[1];

        if (CheckPassword(user, pass, request)) {
            chain.doFilter(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // todo: 使用新的表存储webdav用户以实现该功能
//    private void handleDigestAuth(String authHeader, HttpServletResponse response, FilterChain chain, HttpServletRequest request) throws IOException, ServletException {
//        String[] tokens = authHeader.substring("Digest".length()).trim().split(", ");
//        String nonce = null;
//        String nc = null;
//        String cnonce = null;
//        String qop = null;
//        String responseHash = null;
//        String uri = null;
//
//        for (String token : tokens) {
//            String[] keyValue = token.split("=");
//            String key = keyValue[0].trim();
//            String value = keyValue[1].replaceAll("\"", "").trim();
//
//            switch (key) {
//                case "nonce":
//                    nonce = value;
//                    break;
//                case "nc":
//                    nc = value;
//                    break;
//                case "cnonce":
//                    cnonce = value;
//                    break;
//                case "qop":
//                    qop = value;
//                    break;
//                case "response":
//                    responseHash = value;
//                    break;
//                case "uri":
//                    uri = value;
//                    break;
//            }
//        }
//
//        if (nonce == null || nc == null || cnonce == null || qop == null || responseHash == null || uri == null) {
//            sendChallenge(response);
//            return;
//        }
//
//        String A1 = username + ":" + realm + ":" + password;
//        String A2 = request.getMethod() + ":" + uri;
//        String HA1 = md5Hex(A1);
//        String HA2 = md5Hex(A2);
//        String calculatedResponse = md5Hex(HA1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + HA2);
//
//        if (calculatedResponse.equals(responseHash)) {
//            chain.doFilter(request, response);
//        } else {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//        }
//    }

    private void sendChallenge(HttpServletResponse response) throws IOException {
//        String nonce = generateNonce();
//        String authenticateHeader = "Digest realm=\"" + realm + "\", qop=\"auth\", nonce=\"" + nonce + "\", opaque=\"" + md5Hex(realm) + "\"";
        String authenticateHeader = "Basic realm=\"" + realm + "\"";
                response.setHeader("WWW-Authenticate", authenticateHeader);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private String generateNonce() {
        byte[] nonce = new byte[16];
        new Random().nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }

    private String md5Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            sendChallenge(httpResponse);
            return;
        }

        if (authHeader.startsWith("Basic")) {
            handleBasicAuth(authHeader, httpResponse, chain, httpRequest);
        }
        // todo: Digest
//        else if (authHeader.startsWith("Digest")) {
//            handleDigestAuth(authHeader, httpResponse, chain, httpRequest);
//        }
        else {
            sendChallenge(httpResponse);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
