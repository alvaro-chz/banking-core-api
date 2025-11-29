package com.example.bank_app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beneficiary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficiary_seq_gen")
    @SequenceGenerator(name = "beneficiary_seq_gen", sequenceName = "beneficiary_seq")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String alias;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Builder.Default
    @Column(name = "bank_name", length = 100)
    private String bankName = "BankDemo";

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
