package com.example.bank_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "transaction_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TransactionType {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
