package ru.SeitbayBulat.SpiderNetStore.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank
    private String title;
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;

    private ProductStatus status;
    private List<Long> categoryIds;
    private String fieldSchema;

    /**
     * Каждый внутренний список — содержимое одного текстового файла (одна строка = один сток-айтем).
     */
    private List<List<String>> textLineBatches;

    /**
     * Каждая строка — полное содержимое одного JSON-файла (объект или массив объектов).
     */
    private List<String> jsonFileContents;

    @Valid
    private List<ArchiveBase64Dto> archivesBase64;
}
