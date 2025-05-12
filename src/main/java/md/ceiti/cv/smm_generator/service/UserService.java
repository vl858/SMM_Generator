package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.dto.UserDto;
import md.ceiti.cv.smm_generator.entity.User;

import java.util.List;

public interface UserService {

    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();
}
