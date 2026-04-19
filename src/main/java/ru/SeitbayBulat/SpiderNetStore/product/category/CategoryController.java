package ru.SeitbayBulat.SpiderNetStore.product.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.product.category.dto.CategoryOptionDto;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<CategoryOptionDto> list() {
        return categoryRepository.findAll(Sort.by("id")).stream()
                .map(c -> new CategoryOptionDto(c.getId(), c.getSlug(), c.getName()))
                .toList();
    }
}
