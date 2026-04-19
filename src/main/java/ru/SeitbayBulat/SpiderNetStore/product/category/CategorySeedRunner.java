package ru.SeitbayBulat.SpiderNetStore.product.category;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class CategorySeedRunner implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }
        seed("accounts", "Аккаунты");
        seed("software", "Софт");
        seed("keys", "Ключи");
        seed("services", "Услуги");
        seed("other", "Другое");
    }

    private void seed(String slug, String name) {
        Category c = new Category();
        c.setSlug(slug);
        c.setName(name);
        categoryRepository.save(c);
    }
}
