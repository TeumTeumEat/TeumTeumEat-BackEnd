package im.swyp.teumteumeat.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infra.aws.s3")
public record S3Properties(
        String bucket,
        String region,
        String baseUrl
) {}
