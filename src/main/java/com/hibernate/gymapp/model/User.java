package com.hibernate.gymapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = true)
    private Trainee trainee;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = true)
    private Trainer trainer;
}

