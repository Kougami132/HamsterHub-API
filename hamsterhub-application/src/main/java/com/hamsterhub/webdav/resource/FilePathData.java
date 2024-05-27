package com.hamsterhub.webdav.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FilePathData {
    private String root;
    private String fileUrl;
    private String parentUrl;
    private String name;
}
