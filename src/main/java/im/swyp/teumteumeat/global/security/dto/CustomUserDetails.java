package im.swyp.teumteumeat.global.security.dto;

import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static im.swyp.teumteumeat.global.common.Constants.DELIMITER;

public record CustomUserDetails(
        UserEntity user,
        OAuth2Attributes oAuth2Attributes) implements UserDetails, OAuth2User {

    public CustomUserDetails(UserEntity user) {
        this(user, null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getKey()));
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(user.getId());
    }

    @Override
    public String getName() {
        return oAuth2Attributes.getProvider() + DELIMITER + oAuth2Attributes.getProviderId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2Attributes.getAttributes();
    }
}