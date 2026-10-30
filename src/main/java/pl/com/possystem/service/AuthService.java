package pl.com.possystem.service;

import pl.com.possystem.exception.UserException;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse signup(UserDTO userDto) throws UserException;
    AuthResponse login(UserDTO userDto) throws UserException;
}
