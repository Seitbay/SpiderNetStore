package ru.SeitbayBulat.SpiderNetStore.user;

import jakarta.persistence.*;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import ru.SeitbayBulat.SpiderNetStore.product.Product;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Id;


@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.BUYER;

    @Column(precision = 13, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "seller")
    private List<Product> products = new ArrayList<>();

    public boolean isSeller() {
        return this.role == Role.SELLER || this.role == Role.ADMIN;
    }

    public boolean canSell() {
        return isSeller();
    }
}