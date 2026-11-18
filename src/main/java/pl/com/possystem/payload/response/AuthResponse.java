package pl.com.possystem.payload.response;


import lombok.Data;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.payload.dto.UserDTO;

@Data
public class AuthResponse {

    private String jwt;

    private String message;

    private UserDTO user;

    private StoreStatus storeStatus;
}
