package pl.com.possystem.service;

import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;

import java.util.List;

public interface UserService {

    User getUserFromJwt(String token) throws UserException;
    User getCurrentUser() throws UserException;
    User getUserByEmail(String email) throws UserException;
    User getUserById(Long id);
    List<User> getAllUsers();
    }
