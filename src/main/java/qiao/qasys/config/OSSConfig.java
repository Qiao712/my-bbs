package qiao.qasys.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OSSConfig {
    @Autowired
    private SystemConfig systemConfig;

    @Bean
    public OSS ossClient(){
        //yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = systemConfig.getAliOSS().getEndpoint();
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = systemConfig.getAliOSS().getAccessKeyId();
        String accessKeySecret = systemConfig.getAliOSS().getAccessKeySecret();
        // 创建OSSClient实例.
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
