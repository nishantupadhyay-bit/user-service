package com.assignment.userservice.repository;

import com.assignment.userservice.entity.UserEntity;
import com.assignment.userservice.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUser() {
        UserEntity user = createUser("Test User", "test@example.com");

        UserEntity savedUser = userRepository.saveAndFlush(user);

        assertNotNull(savedUser.getId());

        Optional<UserEntity> retrievedUser =
                userRepository.findById(savedUser.getId());

        assertTrue(retrievedUser.isPresent());
        assertEquals("Test User", retrievedUser.get().getFullName());
        assertEquals("test@example.com", retrievedUser.get().getEmail());
    }

    @Test
    void shouldPersistGuestRoleAndAuditTimestamps() {
        UserEntity user = createUser("Guest User", "guest@example.com");

        UserEntity savedUser = userRepository.saveAndFlush(user);

        assertEquals(UserRole.GUEST, savedUser.getRole());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        UserEntity firstUser =
                createUser("First User", "duplicate@example.com");

        UserEntity secondUser =
                createUser("Second User", "duplicate@example.com");

        userRepository.saveAndFlush(firstUser);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(secondUser)
        );
    }

    private UserEntity createUser(String fullName, String email) {
        UserEntity user = new UserEntity();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(UserRole.GUEST);
        return user;
    }
}
