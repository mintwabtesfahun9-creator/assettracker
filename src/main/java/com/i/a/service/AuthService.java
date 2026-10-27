package com.i.a.service;

import com.i.a.dto.RegisterRequest;
import com.i.a.dto.UserResponse;
import com.i.a.entity.Role;
import com.i.a.entity.User;
import com.i.a.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String username = request.username().trim();

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Username already exists"
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(
            passwordEncoder.encode(request.password())
        );
        user.setRole(Role.ROLE_STAFF);

        User saved = userRepository.save(user);

        return new UserResponse(
            saved.getId(),
            saved.getUsername(),
            saved.getRole()
        );
    }
}
