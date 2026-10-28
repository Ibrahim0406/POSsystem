package pl.com.possystem.mapper;


import pl.com.possystem.entity.User;
import pl.com.possystem.payload.dto.UserDto;

public class UserMapper {


    public static UserDto toDTO(User savedUser) {

        UserDto userDto = new UserDto();
        userDto.setId(savedUser.getId());
        userDto.setFullName(savedUser.getFullName());
        userDto.setEmail(savedUser.getEmail());
        userDto.setRole(savedUser.getRole());
        userDto.setPhone(savedUser.getPhone());
        userDto.setCreatedAt(savedUser.getCreatedAt());
        userDto.setUpdatedAt(savedUser.getUpdatedAt());
        userDto.setLastLogin(savedUser.getLastLogin());
        userDto.setBranchId(savedUser.getBranch() != null ? savedUser.getBranch().getId() : null);
        userDto.setStoreId(savedUser.getStore() != null ? savedUser.getStore().getId() : null);

        return userDto;
    }

    public static User toEntity(UserDto userDto) {
        User user = new User();
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setPhone(userDto.getPhone());
        user.setCreatedAt(userDto.getCreatedAt());
        user.setUpdatedAt(userDto.getUpdatedAt());
        user.setLastLogin(userDto.getLastLogin());
        user.setPassword(userDto.getPassword());
        return user;
    }
}
