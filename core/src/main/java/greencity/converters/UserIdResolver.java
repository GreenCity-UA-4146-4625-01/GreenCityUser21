package greencity.converters;

import greencity.annotations.CurrentUserId;
import greencity.dto.user.UserVO;
import greencity.service.UserService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.security.Principal;

@Component
@AllArgsConstructor
public class UserIdResolver implements HandlerMethodArgumentResolver {
    private final UserService userService;

    /**
     * Method checks if parameter is id and is annotated with
     * {@link CurrentUserId}.
     *
     * @param parameter method parameter
     * @return boolean
     */

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUserId.class) != null
               && Long.class.equals(parameter.getParameterType());
    }

    /**
     * Resolves the ID of the currently authenticated user from the security context.
     *
     * @param parameter     method parameter
     * @param mavContainer  the ModelAndViewContainer
     * @param webRequest    the current web request
     * @param binderFactory the factory to create WebDataBinder instances
     * @return the ID of the currently authenticated user or null if unauthenticated
     */

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Principal principal = webRequest.getUserPrincipal();
        if (principal == null) {
            return null;
        }

        UserVO user = userService.findByEmail(principal.getName());
        if (user == null) {
            throw new SecurityException("Authenticated user not found in database");
        }
        return user.getId();
    }
}
