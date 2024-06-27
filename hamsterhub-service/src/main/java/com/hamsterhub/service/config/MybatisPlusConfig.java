package com.hamsterhub.service.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.hamsterhub.**.mapper")
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        //乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        //分页插件(根据实际情况指定数据库类型，具体查看DbType枚举)
        // interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        //逻辑删除插件不需要再单独配置

        //防止全表更新
        //interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        //paginationInnerInterceptor.setOverflow(false);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        //paginationInnerInterceptor.setMaxLimit(500L);

        return interceptor;
    }
}