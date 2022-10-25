package github.qiao712.bbs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

@Configuration
@EnableOpenApi
public class SwaggerConfig {
    @Bean
    public Docket createRestApi(){
        ApiInfo apiInfo = new ApiInfoBuilder().title("my-bbs").version("0.1.0").build();

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("github.qiao712.bbs.controller"))
                .paths(PathSelectors.any())
                .build();

        //要求一个全局的参数(每个接口都要填写)--登录Token
        RequestParameter requestParameter = new RequestParameterBuilder()
                .name("Token")
                .description("Token")
                .in(ParameterType.HEADER)
                .build();
        docket.globalRequestParameters(Collections.singletonList(requestParameter));
        return docket;
    }
}
