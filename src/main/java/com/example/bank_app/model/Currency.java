package com.example.bank_app.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currency")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder

public class Currency {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;
}
