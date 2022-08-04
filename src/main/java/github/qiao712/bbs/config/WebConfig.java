package github.qiao712.bbs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * 允许跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")     //允许的请求来源
                .allowedMethods("*")            //允许的方法
                .allowCredentials(true)         //允许携带Cookie
                .allowedHeaders("*")            //允许设置Header
                .maxAge(3600);                  //一次OPTION查询的有效时间
    }
}
