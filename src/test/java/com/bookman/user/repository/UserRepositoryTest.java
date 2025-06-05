package com.bookman.user.repository;

import com.bookman.common.TestDataFactory;
import com.bookman.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("사용자 레포지토리 테스트")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("사용자명으로 조회 테스트")
    class FindByUsernameTest {

        @Test
        @DisplayName("존재하는 사용자명으로 조회시 사용자를 반환해야 한다")
        void findByUsername_WithExistingUsername_ShouldReturnUser() {
            // Given
            User user = TestDataFactory.createTestUser();
            entityManager.persistAndFlush(user);

            // When
            Optional<User> found = userRepository.findByUsername(user.getUsername());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo(user.getUsername());
            assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        }

        @Test
        @DisplayName("존재하지 않는 사용자명으로 조회시 빈 Optional을 반환해야 한다")
        void findByUsername_WithNonExistingUsername_ShouldReturnEmpty() {
            // When
            Optional<User> found = userRepository.findByUsername("nonexisting");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("이메일로 조회 테스트")
    class FindByEmailTest {

        @Test
        @DisplayName("존재하는 이메일로 조회시 사용자를 반환해야 한다")
        void findByEmail_WithExistingEmail_ShouldReturnUser() {
            // Given
            User user = TestDataFactory.createTestUser();
            entityManager.persistAndFlush(user);

            // When
            Optional<User> found = userRepository.findByEmail(user.getEmail());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
            assertThat(found.get().getUsername()).isEqualTo(user.getUsername());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회시 빈 Optional을 반환해야 한다")
        void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
            // When
            Optional<User> found = userRepository.findByEmail("nonexisting@test.com");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자명 존재 여부 테스트")
    class ExistsByUsernameTest {

        @Test
        @DisplayName("존재하는 사용자명이면 true를 반환해야 한다")
        void existsByUsername_WithExistingUsername_ShouldReturnTrue() {
            // Given
            User user = TestDataFactory.createTestUser();
            entityManager.persistAndFlush(user);

            // When
            Boolean exists = userRepository.existsByUsername(user.getUsername());

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 사용자명이면 false를 반환해야 한다")
        void existsByUsername_WithNonExistingUsername_ShouldReturnFalse() {
            // When
            Boolean exists = userRepository.existsByUsername("nonexisting");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("이메일 존재 여부 테스트")
    class ExistsByEmailTest {

        @Test
        @DisplayName("존재하는 이메일이면 true를 반환해야 한다")
        void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
            // Given
            User user = TestDataFactory.createTestUser();
            entityManager.persistAndFlush(user);

            // When
            Boolean exists = userRepository.existsByEmail(user.getEmail());

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 false를 반환해야 한다")
        void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
            // When
            Boolean exists = userRepository.existsByEmail("nonexisting@test.com");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Soft Delete 테스트")
    class SoftDeleteTest {

        @Test
        @DisplayName("삭제된 사용자는 조회되지 않아야 한다")
        void softDelete_ShouldNotReturnDeletedUser() {
            // Given
            User user = TestDataFactory.createTestUser();
            entityManager.persistAndFlush(user);

            // When - Soft delete 시뮬레이션
            entityManager.getEntityManager()
                    .createQuery("UPDATE User u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.userId = :id")
                    .setParameter("id", user.getUserId())
                    .executeUpdate();
            entityManager.clear();

            // Then
            Optional<User> found = userRepository.findByUsername(user.getUsername());
            assertThat(found).isEmpty();

            Boolean exists = userRepository.existsByUsername(user.getUsername());
            assertThat(exists).isFalse();
        }
    }
}
