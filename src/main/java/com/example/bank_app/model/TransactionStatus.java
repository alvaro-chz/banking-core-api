package com.example.bank_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "transaction_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TransactionStatus {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;
}