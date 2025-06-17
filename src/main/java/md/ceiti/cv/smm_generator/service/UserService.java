package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.dto.UserDto;
import md.ceiti.cv.smm_generator.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void saveUser(UserDto userDto);

    Optional<User> findUserByEmail(String email);

    List<UserDto> findAllUsers();

    void updatePassword(String email, String newPassword);

    void deleteUser(String email);

    boolean verifyPassword(String rawPassword, String encodedPassword);

    Optional<User> getCurrentUser();
}
