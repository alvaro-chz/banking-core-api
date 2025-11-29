package com.example.bank_app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "login_attempt_seq_gen")
    @SequenceGenerator(name = "login_attempt_seq_gen", sequenceName = "login_attempt_seq")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Builder.Default
    private Integer attempts = 0;

    @UpdateTimestamp
    @Column(name = "last_attempt")
    private LocalDateTime lastAttempt;

    @Builder.Default
    @Column(name = "is_blocked")
    private Boolean isBlocked = false;
}
