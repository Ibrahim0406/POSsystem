package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.com.possystem.config.JWTProvider;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.UserService;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final JWTProvider jwtProvider;

    @Override
    public User getUserFromJwt(String token) throws UserException {

        String email = jwtProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UserException("User not found");
        }
        return user;
    }

    @Override
    public User getCurrentUser() throws UserException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UserException("User not found");
        }

        return user;
    }

    @Override
    public User getUserByEmail(String email) throws UserException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UserException("User not found");
        }

        return user;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);

    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
