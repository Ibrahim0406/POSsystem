package pl.com.possystem.payload.response;


import lombok.Data;
import pl.com.possystem.payload.dto.UserDto;

@Data
public class AuthResponse {

    private String jwt;

    private String message;

    private UserDto user;
}
