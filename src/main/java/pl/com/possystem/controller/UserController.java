package pl.com.possystem.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.mapper.UserMapper;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.service.UserService;
import pl.com.possystem.service.TenantAccess;

import java.util.Objects;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TenantAccess tenantAccess;


    @GetMapping("profile")
    public ResponseEntity<UserDTO> getUserProfile(@RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.getUserFromJwt(jwt);
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@RequestHeader("Authorization") String jwt, @PathVariable Long id) throws UserException {

        User actor = userService.getUserFromJwt(jwt);
        User user = userService.getUserById(id);
        if (user == null) {
            throw new UserException("User not found");
        }
        if (!Objects.equals(actor.getId(), user.getId())) {
            tenantAccess.requireUserAccess(actor, user);
        }
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }


}
