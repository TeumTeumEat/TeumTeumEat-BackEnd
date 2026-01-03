package im.swyp.teumteumeat.global.security.dto;

import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

import static im.swyp.teumteumeat.global.common.Constants.DELIMITER;

public record CustomUserDetails(
        Long userId,
        Role role,
        Collection<? extends GrantedAuthority> authorities,
        OAuth2Attributes oAuth2Attributes) implements UserDetails, OAuth2User {

    public CustomUserDetails(Long userId, Role role, Collection<? extends GrantedAuthority> authorities) {
        this(userId, role, authorities, null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    @Override
    public String getName() {
        return oAuth2Attributes.getProvider() + DELIMITER + oAuth2Attributes.getProviderId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2Attributes.getAttributes();
    }

    public Long getUserId() {
        return userId;
    }
}