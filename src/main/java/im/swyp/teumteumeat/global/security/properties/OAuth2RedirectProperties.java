package im.swyp.teumteumeat.global.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "oauth2")
public record OAuth2RedirectProperties(
        List<String> allowedRedirectUris
) {}
