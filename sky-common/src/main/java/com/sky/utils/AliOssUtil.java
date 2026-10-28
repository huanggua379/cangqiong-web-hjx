package com.sky.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.sky.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

/**
 * 阿里云 OSS 文件操作工具。
 *
 * OSS 客户端会在第一次上传时延迟创建，使没有云端凭据的开发环境仍可启动。
 */
@Slf4j
public class AliOssUtil implements AutoCloseable {

    private static final long MAX_SIGNED_URL_EXPIRATION_SECONDS = 7 * 24 * 60 * 60;

    private final String endpoint;
    private final String region;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;
    private final long signedUrlExpirationSeconds;

    private volatile OSS ossClient;

    public AliOssUtil(String endpoint, String region, String accessKeyId, String accessKeySecret,
                      String bucketName, long signedUrlExpirationSeconds) {
        this.endpoint = endpoint;
        this.region = region;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        this.signedUrlExpirationSeconds = signedUrlExpirationSeconds;
    }

    /**
     * 上传对象并返回私有对象的限时访问地址。
     */
    public String upload(byte[] bytes, String objectName, String contentType) {
        OSS client = getClient();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            metadata.setContentType(contentType);
            PutObjectRequest request = new PutObjectRequest(bucketName, objectName, inputStream, metadata);
            client.putObject(request);

            Date expiration = Date.from(Instant.now().plusSeconds(signedUrlExpirationSeconds));
            URL signedUrl = client.generatePresignedUrl(bucketName, objectName, expiration);
            log.info("OSS 文件上传成功，objectName={}", objectName);
            return signedUrl.toString();
        } catch (OSSException ex) {
            log.error("OSS 拒绝上传请求，errorCode={}, requestId={}", ex.getErrorCode(), ex.getRequestId());
            throw new FileUploadException("文件上传失败，请检查 Bucket、地域和 RAM 权限", ex);
        } catch (ClientException ex) {
            log.error("OSS 客户端上传失败，exceptionType={}", ex.getClass().getSimpleName());
            throw new FileUploadException("文件上传失败，请检查 OSS 网络和 Endpoint 配置", ex);
        } catch (Exception ex) {
            log.error("生成 OSS 上传结果失败，exceptionType={}", ex.getClass().getSimpleName());
            throw new FileUploadException("文件上传失败，请稍后重试", ex);
        }
    }

    /**
     * 兼容项目原有调用方式。
     */
    public String upload(byte[] bytes, String objectName) {
        return upload(bytes, objectName, "application/octet-stream");
    }

    private OSS getClient() {
        OSS client = ossClient;
        if (client == null) {
            synchronized (this) {
                client = ossClient;
                if (client == null) {
                    validateConfiguration();
                    ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
                    configuration.setSignatureVersion(SignVersion.V4);
                    client = OSSClientBuilder.create()
                            .endpoint(endpoint)
                            .credentialsProvider(new DefaultCredentialProvider(accessKeyId, accessKeySecret))
                            .clientConfiguration(configuration)
                            .region(region)
                            .build();
                    ossClient = client;
                }
            }
        }
        return client;
    }

    private void validateConfiguration() {
        if (isBlank(endpoint) || isBlank(region) || isBlank(bucketName)
                || isBlank(accessKeyId) || isBlank(accessKeySecret)) {
            throw new FileUploadException("OSS 配置不完整，请设置 Endpoint、Region、Bucket 和访问凭据");
        }
        if (signedUrlExpirationSeconds < 1
                || signedUrlExpirationSeconds > MAX_SIGNED_URL_EXPIRATION_SECONDS) {
            throw new FileUploadException("OSS 签名 URL 有效期必须在 1 秒到 7 天之间");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public void close() {
        OSS client = ossClient;
        if (client != null) {
            client.shutdown();
        }
    }
}
