package ru.SeitbayBulat.SpiderNetStore.product.category;

import jakarta.persistence.*;
import lombok.Data;
import ru.SeitbayBulat.SpiderNetStore.product.Product;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @ManyToMany(mappedBy = "categories")
    private List<Product> products = new ArrayList<>();
}