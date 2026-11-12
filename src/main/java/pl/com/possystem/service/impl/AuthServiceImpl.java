package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.com.possystem.config.JWTProvider;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.mapper.UserMapper;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.payload.response.AuthResponse;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.AuthService;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JWTProvider jwtProvider;

    private final CustomUserImplementation customUserImplementation;



    @Override
    public AuthResponse signup(UserDTO userDto) throws UserException {
        User user = userRepository.findByEmail(userDto.getEmail());
        if (user != null) {
            throw new UserException("User with this email already exists");
        }
        User newUser = new User();
        newUser.setFullName(userDto.getFullName());
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newUser.setRole(UserRole.ROLE_BRANCH_CASHIER);
        newUser.setStatus(AccountStatus.ACTIVE);
        newUser.setPhone(userDto.getPhone());
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("User registered successfully");
        authResponse.setUser(UserMapper.toDTO(savedUser));

        return authResponse;
    }

    @Override
    public AuthResponse login(UserDTO userDto) throws UserException {
        String email = userDto.getEmail();
        String password = userDto.getPassword();
        
        Authentication authentication = authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String role = authorities.iterator().next().getAuthority();

        String jwt = jwtProvider.generateToken(authentication);

        User user = userRepository.findByEmail(email);

        user.setLastLogin(LocalDateTime.now());

        userRepository.save(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("User logged in successfully");
        authResponse.setUser(UserMapper.toDTO(user));

        return authResponse;
    }

    private Authentication authenticate(String email, String password) throws UserException {

        UserDetails userDetails = customUserImplementation.loadUserByUsername(email);

        if (userDetails == null) {

            throw new UserException("User not found with email: " + email);
        }

        if (!userDetails.isEnabled()) {
            throw new UserException("This account is disabled");
        }

        if( !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new UserException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
    }
}
