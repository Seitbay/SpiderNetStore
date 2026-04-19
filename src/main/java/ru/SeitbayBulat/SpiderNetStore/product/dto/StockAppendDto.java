package ru.SeitbayBulat.SpiderNetStore.product.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class StockAppendDto {
    private List<List<String>> textLineBatches;
    private List<String> jsonFileContents;

    @Valid
    private List<ArchiveBase64Dto> archivesBase64;
}
