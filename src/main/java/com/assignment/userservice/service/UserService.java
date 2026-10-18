package com.assignment.userservice.service;
import com.assignment.userservice.exception.UserNotFoundException;
import java.util.List;
import com.assignment.userservice.dto.CreateUserRequest;
import com.assignment.userservice.entity.UserEntity;
import com.assignment.userservice.exception.DuplicateEmailException;
import com.assignment.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;

@Service
public class UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserEntity createUser(CreateUserRequest request) {
        String fullName = request.getFullName().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            logger.warn("User registration rejected: duplicate email");
            throw new DuplicateEmailException(email);
        }

        UserEntity user = new UserEntity();
        user.setFullName(fullName);
        user.setEmail(email);

        try {
            UserEntity savedUser = userRepository.saveAndFlush(user);

            logger.info("User registered successfully with id={}", savedUser.getId());

            return savedUser;
        } catch (DataIntegrityViolationException exception) {
            logger.warn("User registration rejected by a database integrity constraint");
            throw new DuplicateEmailException(email);
        }
    }


    @Transactional(readOnly = true)
    public List<UserEntity> getAllUsers() {
        logger.info("Fetching all users");

        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserEntity getUserById(Long id) {
        logger.info("Fetching user with id={}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

}
