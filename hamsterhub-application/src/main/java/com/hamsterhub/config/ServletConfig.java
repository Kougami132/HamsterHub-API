package com.hamsterhub.config;


import javax.servlet.Servlet;

import com.hamsterhub.service.FileService;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.webdav.FileTool;
import com.hamsterhub.webdav.MyWebDavServlet;
import org.apache.catalina.servlets.DefaultServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfig {
    @Autowired
    private FileTool fileTool;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private FileService fileService;

    @Bean
    public ServletRegistrationBean<Servlet> servletRegistrationBean() {
        ServletRegistrationBean<Servlet> register = new ServletRegistrationBean<>();
        DefaultServlet defaultServlet = new MyWebDavServlet(fileTool,deviceService,fileService);
        register.setServlet(defaultServlet);
        register.addUrlMappings("/dav/*");
        register.setLoadOnStartup(1);
        return register;
    }

}
