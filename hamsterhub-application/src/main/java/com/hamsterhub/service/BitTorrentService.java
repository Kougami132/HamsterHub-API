package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.entity.Torrent;

import java.util.List;

public interface BitTorrentService {
    Boolean connect() throws BusinessException;
    List<Torrent> getTorrents() throws BusinessException;
    Boolean addTorrent(String tag, String magnet, String path) throws BusinessException;
    Boolean deleteTorrent(String tag) throws BusinessException;
    Torrent getTorrent(String tag) throws BusinessException;
}
