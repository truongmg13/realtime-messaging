package com.truongmg.messaging.service;

import com.truongmg.messaging.dto.UserResponse;
import com.truongmg.messaging.exception.BadRequestException;
import com.truongmg.messaging.model.User;
import com.truongmg.messaging.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    private UserService userService;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        currentUserId = UUID.randomUUID();
    }

    @Test
    void search_matchingQuery_returnsMappedResults() {
        User bob = buildUser("bob", "Bob Smith");
        when(userRepository.search(eq("bob"), eq(currentUserId), any(Pageable.class)))
                .thenReturn(List.of(bob));

        List<UserResponse> results = userService.search(currentUserId, "bob", 20);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().username()).isEqualTo("bob");
        assertThat(results.getFirst().displayName()).isEqualTo("Bob Smith");
    }

    @Test
    void search_blankQuery_throwsBadRequestWithoutQuerying() {
        assertThatThrownBy(() -> userService.search(currentUserId, "  ", 20))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void search_nullQuery_throwsBadRequestWithoutQuerying() {
        assertThatThrownBy(() -> userService.search(currentUserId, null, 20))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void search_limitAboveMax_isClampedToMax() {
        when(userRepository.search(any(), any(), any(Pageable.class))).thenReturn(List.of());

        userService.search(currentUserId, "bob", 500);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).search(eq("bob"), eq(currentUserId), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(50);
    }

    private User buildUser(String username, String displayName) {
        User user = new User();
        user.setUsername(username);
        user.setDisplayName(displayName);
        setId(user, UUID.randomUUID());
        return user;
    }

    private void setId(User user, UUID id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
