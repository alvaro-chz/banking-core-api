package com.example.bank_app.repository;

import com.example.bank_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDocumentId(String documentId);
}
