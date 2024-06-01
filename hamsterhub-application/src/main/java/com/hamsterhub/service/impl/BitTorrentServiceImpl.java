package com.hamsterhub.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.BitTorrentService;
import com.hamsterhub.service.entity.Torrent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BitTorrentServiceImpl implements BitTorrentService {

    @Value("${bit-torrent.address}")
    private String address;
    @Value("${bit-torrent.username}")
    private String username;
    @Value("${bit-torrent.password}")
    private String password;

    @Autowired
    private RestTemplate restTemplate;

    private String cookie;

    @Override
    public Boolean connect() throws BusinessException {
        String url = this.address + "/api/v2/auth/login";

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // form
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("password", password);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful())
                return false;

            this.cookie = response.getHeaders().getFirst("set-cookie");
            return true;
        }
        catch (Exception e) {
            log.error("qbittorrent连接错误，连接地址：{}", url);
            return false;
        }
    }

    @Override
    public List<Torrent> getTorrents() throws BusinessException {
        String url = this.address + "/api/v2/torrents/info";

        HttpHeaders headers = new HttpHeaders();
        headers.set("cookie", this.cookie);

        ParameterizedTypeReference<List<Torrent>> typeRef = new ParameterizedTypeReference<List<Torrent>>() {};
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<List<Torrent>> response = restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
        return response.getBody();
    }

    @Override
    public Boolean addTorrent(String tag, String magnet, String path) throws BusinessException {
        String url = this.address + "/api/v2/torrents/add";

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("cookie", this.cookie);

        // form
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("tags", tag);
        form.add("urls", magnet);
        form.add("savepath", path);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful())
                return false;
            return response.getBody().equals("Ok.");
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean deleteTorrent(String tag) throws BusinessException {
        Torrent torrent = getTorrent(tag);
        if (torrent == null) return false;
        String hash = torrent.getHash();

        String url = this.address + "/api/v2/torrents/delete";

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("cookie", this.cookie);

        // form
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("hashes", hash);
        form.add("deleteFiles", "true");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful())
                return false;
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Torrent getTorrent(String tag) throws BusinessException {
        List<Torrent> torrents = getTorrents();
        for (Torrent i: torrents)
            if (i.getTags().equals(tag))
                return i;
        return null;
    }
}
