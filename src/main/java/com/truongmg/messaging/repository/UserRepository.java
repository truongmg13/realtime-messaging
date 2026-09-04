package com.truongmg.messaging.repository;

import com.truongmg.messaging.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    /**
     * Case-insensitive match on username or display name, excluding the searching user.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.id <> :excludeUserId
          AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY u.username ASC
        """)
    List<User> search(@Param("query") String query, @Param("excludeUserId") UUID excludeUserId, Pageable pageable);
}
