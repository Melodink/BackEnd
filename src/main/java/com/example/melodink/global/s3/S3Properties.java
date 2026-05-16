package com.example.melodink.global.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloud.aws.s3")
public class S3Properties {

    private String bucket;
    private String region;
    private String cdnBaseUrl;  // CloudFront 도메인 (예: https://cdn.melodink.com)

    // 허용 파일 크기 제한
    private long maxImageSizeBytes = 10 * 1024 * 1024L;   // 10MB
    private long maxAudioSizeBytes = 100 * 1024 * 1024L;  // 100MB
    private long maxVideoSizeBytes = 500 * 1024 * 1024L;  // 500MB
}
