package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.dto.UserDto;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();
}
