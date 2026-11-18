package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.possystem.config.JWTProvider;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.StoreContact;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.mapper.UserMapper;
import pl.com.possystem.payload.dto.TenantRegistrationRequest;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.payload.response.AuthResponse;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.AuthService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JWTProvider jwtProvider;

    private final CustomUserImplementation customUserImplementation;
    private final StoreRepository storeRepository;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse signup(TenantRegistrationRequest registration) throws UserException {
        if (registration == null) {
            throw new UserException("Registration details are required");
        }
        String fullName = normalize(registration.getOwnerFullName());
        String email = registration.getOwnerEmail() == null
                ? null : registration.getOwnerEmail().trim().toLowerCase(Locale.ROOT);
        String password = registration.getOwnerPassword();
        String brand = normalize(registration.getStoreBrand());
        if (fullName == null || fullName.isBlank() || fullName.length() > 255) {
            throw new UserException("Owner name is required and must be at most 255 characters");
        }
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.length() > 255) {
            throw new UserException("A valid owner email address is required");
        }
        if (password == null || password.isBlank()) {
            throw new UserException("Owner password is required");
        }
        if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new UserException("Owner password must be at least 8 characters and no more than 72 UTF-8 bytes");
        }
        if (brand == null || brand.isBlank() || brand.length() > 255) {
            throw new UserException("Store brand is required and must be at most 255 characters");
        }
        if (registration.getStoreDescription() != null && registration.getStoreDescription().length() > 255) {
            throw new UserException("Store description must be at most 255 characters");
        }
        if (registration.getStoreType() != null && registration.getStoreType().length() > 255) {
            throw new UserException("Store type must be at most 255 characters");
        }
        StoreContact requestedContact = registration.getContact();
        if (requestedContact != null && Stream.of(
                requestedContact.getAddress(), requestedContact.getPhone(), requestedContact.getEmail())
                .anyMatch(value -> value != null && value.length() > 255)) {
            throw new UserException("Store contact fields must be at most 255 characters");
        }
        if (registration.getOwnerPhone() != null && registration.getOwnerPhone().length() > 255) {
            throw new UserException("Owner phone number must be at most 255 characters");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists");
        }
        if (userRepository.existsByFullNameIgnoreCase(fullName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this full name already exists");
        }
        if (storeRepository.existsByBrandIgnoreCase(brand)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A store with this brand already exists");
        }

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(UserRole.ROLE_STORE_ADMIN);
        newUser.setStatus(AccountStatus.ACTIVE);
        newUser.setPhone(registration.getOwnerPhone() == null ? null : registration.getOwnerPhone().trim());
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);
        Store store = new Store();
        store.setBrand(brand);
        store.setDescription(registration.getStoreDescription());
        store.setStoreType(registration.getStoreType());
        store.setContact(requestedContact == null ? new StoreContact() : requestedContact);
        store.setStatus(StoreStatus.PENDING);
        store.setStoreAdmin(savedUser);
        Store savedStore = storeRepository.save(store);
        savedUser.setStore(savedStore);
        savedUser = userRepository.save(savedUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(), password,
                List.of(new SimpleGrantedAuthority(savedUser.getRole().name())));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Registration successful. Your store is pending platform approval.");
        authResponse.setUser(UserMapper.toDTO(savedUser));
        authResponse.setStoreStatus(savedStore.getStatus());

        return authResponse;
    }

    @Override
    @Transactional
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
        if (user.getStore() != null) {
            authResponse.setStoreStatus(user.getStore().getStatus());
        }

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

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
