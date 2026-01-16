package com.example.bank_app.repository;

import com.example.bank_app.dto.admin.UserAdminResponse;
import com.example.bank_app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDocumentId(String documentId);
    Long countByRole_Name(String roleName);

    @Query("""
        SELECT new com.example.bank_app.dto.admin.UserAdminResponse(
             u.id,
             CONCAT(u.name, ' ', u.lastName1),
             u.documentId,
             u.email,
             u.phoneNumber,
             COALESCE(la.isBlocked, false),
             u.createdAt
         )
         FROM User u
         LEFT JOIN LoginAttempt la ON la.user = u
         WHERE (:term IS NULL OR (
             LOWER(u.name) LIKE :term OR
             LOWER(u.lastName1) LIKE :term OR
             LOWER(u.email) LIKE :term OR
             u.documentId LIKE :term
         ))
         AND (:isActive IS NULL OR u.isActive = :isActive)
         AND (:isBlocked IS NULL OR COALESCE(la.isBlocked, false) = :isBlocked)
    """)
    Page<UserAdminResponse> findUsersByFilter(
            @Param("term") String term,
            @Param("isActive") Boolean isActive,
            @Param("isBlocked") Boolean isBlocked,
            Pageable pageable
    );
}
