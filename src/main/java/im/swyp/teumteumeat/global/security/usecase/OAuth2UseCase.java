package im.swyp.teumteumeat.global.security.usecase;

import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.component.OAuthOidcHelper;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.security.dto.LoginResponse;
import im.swyp.teumteumeat.global.security.dto.OidcPayload;
import im.swyp.teumteumeat.global.security.dto.request.SignUpRequest;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.Token;
import im.swyp.teumteumeat.global.security.AppleUtil;
import im.swyp.teumteumeat.global.security.client.apple.AppleAuthClient;
import im.swyp.teumteumeat.global.security.client.google.GoogleAuthClient;
import im.swyp.teumteumeat.global.security.dto.AppleTokenResponse;
import im.swyp.teumteumeat.global.security.dto.GoogleTokenResponse;
import im.swyp.teumteumeat.global.security.properties.apple.AppleOidcProperties;
import im.swyp.teumteumeat.global.security.properties.google.GoogleOidcProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class OAuth2UseCase {

    private final OAuthOidcHelper oauthOidcHelper;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final AppleAuthClient appleAuthClient;
    private final GoogleAuthClient googleAuthClient;
    private final AppleUtil appleUtil;
    private final AppleOidcProperties appleOidcProperties; // Using OidcProperties for client-id/secret
    private final GoogleOidcProperties googleOidcProperties;

    public LoginResponse signUp(SocialProvider provider, SignUpRequest.Oidc request) {
        OidcPayload payload = oauthOidcHelper.getPayload(provider, request.idToken());

        String socialId = payload.sub();
        String email = payload.email();
        String name;

        if (provider.equals(SocialProvider.APPLE)) {
            name = StringUtils.hasText(request.name()) ? request.name() : "Apple User";
        } else {
            name = payload.name();
        }

        UserEntity user = userService.findBySocialProviderAndSocialId(provider, socialId)
                .orElseGet(() -> {
                    if (request.termsAgreed()) {
                        return userService.getOrSaveUser(name, provider, socialId, email);
                    }
                    throw new BaseException(AuthResponseCode.NEED_REGISTER);
                });

        // [NEW] Refresh Token Exchange & Save
        if (StringUtils.hasText(request.authCode())) {
            String refreshToken = null;
            try {
                if (provider == SocialProvider.APPLE) {
                    AppleTokenResponse response = appleAuthClient.getToken(
                            appleUtil.getClientId(),
                            appleUtil.createClientSecret(),
                            request.authCode(),
                            "authorization_code",
                            null // redirect_uri might be needed or can be null for some flows
                    );
                    refreshToken = response.refreshToken();
                } else if (provider == SocialProvider.GOOGLE) {
                    GoogleTokenResponse response = googleAuthClient.getToken(
                            googleOidcProperties.getSecret(), // Confusing mapping: GoogleOidcProperties uses 'secret'
                                                              // for Client ID (aud)
                            googleOidcProperties.getClientSecret(), // Real Client Secret
                            request.authCode(),
                            "authorization_code",
                            "" // Redirect URI is required for Google usually, strictly for 'postmessage' or
                               // configured URI
                    );
                    refreshToken = response.refreshToken();
                }

                if (refreshToken != null) {
                    userService.updateSocialRefreshToken(user, refreshToken);
                }
            } catch (Exception e) {
                log.error("Failed to exchange auth code for refresh token", e);
                // Don't fail login, just log error? Or throw? For now log.
            }
        }

        Token token = jwtProvider.issueToken(user);

        return LoginResponse.builder()
                .accessToken(token.accessToken())
                .refreshToken(token.refreshToken())
                .isOnboardingCompleted(user.isOnboardingCompleted())
                .build();
    }
}
