package pl.com.possystem.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.payload.response.AuthResponse;
import pl.com.possystem.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;



    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupHandler(@RequestBody UserDTO userDto) throws UserException {
            return ResponseEntity.ok(authService.signup(userDto));
    }


    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> loginHandler(@RequestBody UserDTO userDto) throws UserException {
        return ResponseEntity.ok(authService.login(userDto));
    }
}
