package pl.com.possystem.service;

import pl.com.possystem.exception.UserException;
import pl.com.possystem.payload.dto.UserDto;
import pl.com.possystem.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse signup(UserDto userDto) throws UserException;
    AuthResponse login(UserDto userDto) throws UserException;
}
