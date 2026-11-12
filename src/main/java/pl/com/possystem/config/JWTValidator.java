package pl.com.possystem.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.entity.User;
import pl.com.possystem.repository.UserRepository;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collections;

@Component
public class JWTValidator extends OncePerRequestFilter {

    private final SecretKey key;
    private final UserRepository userRepository;

    public JWTValidator(@Value("${jwt.secret}") String jwtSecret, UserRepository userRepository) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        if (jwt != null) {
            try {
                jwt = jwt.substring(7);
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

                String email = String.valueOf(claims.get("email"));
                User user = userRepository.findByEmail(email);
                if (user == null || user.getStatus() == AccountStatus.DISABLED) {
                    throw new BadCredentialsException("Account is unavailable");
                }
                GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        email, null, Collections.singletonList(authority));

                SecurityContextHolder.getContext().setAuthentication(auth);

            }catch(Exception e){
                throw new BadCredentialsException("Invalid token");
            }
        }

        filterChain.doFilter(request, response);
    }
}