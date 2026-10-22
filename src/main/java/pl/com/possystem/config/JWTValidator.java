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
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Component
public class JWTValidator extends OncePerRequestFilter {

    private final SecretKey key;

    public JWTValidator(@Value("${jwt.secret}") String jwtSecret) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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
                String authorities = String.valueOf(claims.get("authorities"));


                List<GrantedAuthority> auths = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
                Authentication auth = new UsernamePasswordAuthenticationToken(email, null, auths);

                SecurityContextHolder.getContext().setAuthentication(auth);

            }catch(Exception e){
                throw new BadCredentialsException("Invalid token");
            }
        }

        filterChain.doFilter(request, response);
    }
}