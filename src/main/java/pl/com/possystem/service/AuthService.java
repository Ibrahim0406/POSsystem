package pl.com.possystem.service;

import pl.com.possystem.exception.UserException;
import pl.com.possystem.payload.dto.TenantRegistrationRequest;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse signup(TenantRegistrationRequest registration) throws UserException;
    AuthResponse login(UserDTO userDto) throws UserException;
}
