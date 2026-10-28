package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置。
 */
@Configuration
@Slf4j
public class OssConfiguration {

    @Bean(destroyMethod = "close")
    public AliOssUtil aliOssUtil(AliOssProperties properties) {
        log.info("初始化阿里云 OSS 工具（客户端将在首次上传时创建）");
        return new AliOssUtil(
                properties.getEndpoint(),
                properties.getRegion(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getBucketName(),
                properties.getSignedUrlExpirationSeconds()
        );
    }
}
