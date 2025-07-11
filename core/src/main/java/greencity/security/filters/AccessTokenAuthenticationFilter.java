package greencity.security.filters;

import greencity.dto.user.UserVO;
import greencity.security.jwt.JwtTool;
import greencity.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Class that provide Authentication object based on JWT.
 *
 * @author Yurii Koval.
 * @version 1.0
 */
@Slf4j
public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTool jwtTool;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    /**
     * Constructor.
     */
    public AccessTokenAuthenticationFilter(JwtTool jwtTool, AuthenticationManager authenticationManager,
        UserService userService) {
        this.jwtTool = jwtTool;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    private String getTokenFromCookies(Cookie[] cookies) {
        return Arrays.stream(cookies)
            .filter(c -> c.getName().equals("accessToken"))
            .findFirst()
            .map(Cookie::getValue).orElse(null);
    }

    private String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String uri = request.getRequestURI();
        if (cookies != null && uri.startsWith("/management")) {
            return getTokenFromCookies(cookies);
        }

        return jwtTool.getTokenFromHttpServletRequest(request);
    }

    /**
     * Checks if request has token in header, if this token still valid, and set
     * authentication for spring.
     *
     * @param request  this is servlet that take request
     * @param response this is response servlet
     * @param chain    this is filter of chain
     */
    @Override
    public void doFilterInternal(@SuppressWarnings("NullableProblems") HttpServletRequest request,
        @SuppressWarnings("NullableProblems") HttpServletResponse response,
        @SuppressWarnings("NullableProblems") FilterChain chain)
        throws IOException, ServletException {
        String token = extractToken(request);
        log.info("token {}", token);

        if (token != null) {
            try {
                String email = jwtTool.getEmailOutOfAccessToken(token);
                Optional<UserVO> userOptional = userService.findNotDeactivatedByEmail(email);
                if (userOptional.isPresent()) {
                    UserVO user = userOptional.get();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority(user.getRole().toString()))
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("User successfully authenticated - {}, {}", user.getEmail(), user.getRole());
                } else {
                    log.info("User with email {} not found or deactivated", email);
                }
            } catch (ExpiredJwtException e) {
                log.info("Token has expired: " + token);
            } catch (Exception e) {
                log.info("Access denied with token: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}