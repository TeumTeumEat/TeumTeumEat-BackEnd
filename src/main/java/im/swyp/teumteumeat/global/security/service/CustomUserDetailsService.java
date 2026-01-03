package im.swyp.teumteumeat.global.security.service;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static im.swyp.teumteumeat.global.common.Constants.DELIMITER;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String uniqueId) throws UsernameNotFoundException {
        String[] parts = uniqueId.split(DELIMITER);
        SocialProvider socialProvider = SocialProvider.valueOf(parts[0]);
        String socialId = parts[1];

        UserEntity user = userRepository.findBySocialProviderAndSocialId(socialProvider, socialId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with socialProvider:social id: " + socialProvider + DELIMITER + socialId));

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getKey())
        );

        return new CustomUserDetails(user.getId(), authorities, null);
    }
}