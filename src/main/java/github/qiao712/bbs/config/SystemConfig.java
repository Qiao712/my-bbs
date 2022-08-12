package github.qiao712.bbs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 系统配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "sys")
public class SystemConfig {
    /**
     * token有时间 (ms)
     */
    private Integer tokenValidTime;

    /**
     * 选择RememberMe时，token有时间 (ms)
     */
    private Integer rememberMeTokenValidTime;

    /**
     * 用户注册的默认角色id
     */
    private Long defaultRoleId;

    /**
     * 头像图片最大size (byte)
     */
    private Long maxAvatarSize;

    /**
     * 贴子中图片最大size (byte)
     */
    private Long maxPostPictureSize;

    /**
     * 贴子中图片最大数量 (byte)
     */
    private Long maxPostPictureNum;

    /**
     * 板块logo图片最大size (byte)
     */
    private Long maxLogoImageSize;

    /**
     * 阿里云OSS配置
     */
    @Data
    public static class AliOSS{
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
    }

    private AliOSS aliOSS;
}
