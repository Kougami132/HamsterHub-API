package com.hamsterhub.config;

import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.webdav.WebDavAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Autowired
    private AccountService accountService;

    @Bean
    public FilterRegistrationBean<WebDavAuthenticationFilter> customFilter() {
        // 为webdav提供认证 Filter
        FilterRegistrationBean<WebDavAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new WebDavAuthenticationFilter(accountService));
        registrationBean.addUrlPatterns("/dav/*");

        return registrationBean;
    }
}
