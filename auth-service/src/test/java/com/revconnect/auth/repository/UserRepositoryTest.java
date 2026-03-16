package com.revconnect.auth.repository;

import com.revconnect.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        savedUser = userRepository.save(
                User.builder()
                        .email("alice@example.com")
                        .username("alice")
                        .password("hashed")
                        .firstName("Alice")
                        .lastName("Smith")
                        .accountType("PERSONAL")
                        .build()
        );
    }

    @Test
    @DisplayName("findByEmail - returns user when email exists")
    void findByEmail_exists_returnsUser() {

        Optional<User> result = userRepository.findByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findByEmail - returns empty when email not found")
    void findByEmail_notFound_returnsEmpty() {

        assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail - returns true when email exists")
    void existsByEmail_exists_returnsTrue() {

        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - returns false when email not found")
    void existsByEmail_notFound_returnsFalse() {

        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }

    @Test
    @DisplayName("save - persists user with auto-generated id and timestamps")
    void save_persistsUserWithTimestamps() {

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }
}