package com.hamsterhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



//@Configuration
//@EnableSwagger2
//public class SwaggerConfiguration {
//    @Bean
//    public Docket buildDocket() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(buildApiInfo())
//                .select()
//// 要扫描的API(Controller)基础包
//                .apis(RequestHandlerSelectors.basePackage("com.hamsterhub.controller"))
//                .paths(PathSelectors.any())
//                .build();
//    }
//    /**
//     * @param
//     * @return springfox.documentation.service.ApiInfo
//     * @Title: 构建API基本信息
//     * @methodName: buildApiInfo
//     */
//    private ApiInfo buildApiInfo() {
//        Contact contact = new Contact("开发者","","");
//        return new ApiInfoBuilder()
//                .title("HamsterHub-云盘API文档")
//                .description("")
//                .contact(contact)
//                .version("1.0.0").build();
//    }
//}


/**
 * SpringDoc API 文档相关配置
 */
@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HamsterHub-云盘API文档")
                        .description("HamsterHub 云盘API接口文档")
                        .version("1.0.0")
//                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
//                .externalDocs(new ExternalDocumentation()
//                        .description("替代 Springfox 的 SpringDOC 入门 文档")
//                        .url("https://www.cnblogs.com/jddreams/p/15922674.html")
                        );
    }

}