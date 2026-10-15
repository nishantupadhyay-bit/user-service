package com.assignment.userservice.service;

import com.assignment.userservice.dto.CreateUserRequest;
import com.assignment.userservice.entity.UserEntity;
import com.assignment.userservice.enums.UserRole;
import com.assignment.userservice.exception.DuplicateEmailException;
import com.assignment.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest();
        request.setFullName("Test User");
        request.setEmail("test@example.com");
    }

    @Test
    void shouldCreateUserWithGuestRole() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(false);

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        UserEntity result = userService.createUser(request);

        assertEquals("Test User", result.getFullName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(UserRole.GUEST, result.getRole());

        verify(userRepository).saveAndFlush(any(UserEntity.class));
    }

    @Test
    void shouldNormalizeEmailBeforeSaving() {
        request.setEmail("  TEST@EXAMPLE.COM  ");

        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(false);

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.createUser(request);

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).saveAndFlush(any(UserEntity.class));
    }

    @Test
    void shouldTranslateDatabaseIntegrityViolation() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(false);

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException(
                        "Unique constraint violation"));

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser(request)
        );
    }
}