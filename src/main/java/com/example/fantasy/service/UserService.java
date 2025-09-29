package com.example.fantasy.service;

import com.example.fantasy.domain.User;
import com.example.fantasy.domain.enums.UserRole;
import com.example.fantasy.dto.UserDtos;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.exception.ValidationException;
import com.example.fantasy.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User register(UserDtos.UserRegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ValidationException("Username already taken");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ValidationException("Email already used");
        }
        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(UserRole.USER);
        u.setActive(true);
        if (req.favouriteTeamId() != null) {
            // lazy inject via repository to avoid circular beans; simple new reference with id is enough for persist
            com.example.fantasy.domain.BasketballTeam fav = new com.example.fantasy.domain.BasketballTeam();
            fav.setId(req.favouriteTeamId());
            u.setFavouriteTeam(fav);
        }
        u.setNationality(req.nationality());
        return userRepository.save(u);
    }

    public User login(UserDtos.LoginRequest req) {
        User u = userRepository.findByUsername(req.usernameOrEmail())
                .or(() -> userRepository.findByEmail(req.usernameOrEmail()))
                .orElseThrow(() -> new ValidationException("Invalid credentials"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new ValidationException("Invalid credentials");
        }
        return u;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean hasFantasyTeam(Long userId) {
        User user = getUser(userId);
        return user.isHasFantasyTeam();
    }
}
