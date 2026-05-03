package im.swyp.teumteumeat.global.security.resolver;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static im.swyp.teumteumeat.global.security.constant.AuthResponseCode.UNAUTHORIZED;

@Slf4j
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isLoginUserAnnotation = parameter.getParameterAnnotation(LoginUser.class) != null;
        boolean isCustomUserDetailsClass = CustomUserDetails.class.equals(parameter.getParameterType());

        return isLoginUserAnnotation && isCustomUserDetailsClass;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BaseException(UNAUTHORIZED);
        }

        if (authentication.getPrincipal().equals("anonymousUser")) {
            log.error("principal is anonymous.");
            throw new BaseException(UNAUTHORIZED);
        }

        return authentication.getPrincipal();
    }
}
