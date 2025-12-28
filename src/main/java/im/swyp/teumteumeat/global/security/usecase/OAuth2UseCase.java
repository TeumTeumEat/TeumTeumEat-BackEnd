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
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class OAuth2UseCase {

    private final OAuthOidcHelper oauthOidcHelper;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public LoginResponse signUp(SocialProvider provider, SignUpRequest.Oidc request) {
        OidcPayload payload = oauthOidcHelper.getPayload(provider, request.idToken());

        String socialId = payload.sub();
        String email = payload.email();
        String name = "User";

        UserEntity user = userService.findBySocialProviderAndSocialId(provider, socialId)
                .orElseGet(() -> {
                    if (request.termsAgreed()) {
                        return userService.getOrSaveUser(name, provider, socialId, email);
                    }
                    throw new BaseException(AuthResponseCode.NEED_REGISTER);
                });

        Token token = jwtProvider.issueToken(user);

        return LoginResponse.builder()
                .accessToken(token.accessToken())
                .refreshToken(token.refreshToken())
                .isOnboardingCompleted(user.isOnboardingCompleted())
                .build();
    }
}
