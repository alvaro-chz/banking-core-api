package com.example.bank_app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder

public class Role {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}