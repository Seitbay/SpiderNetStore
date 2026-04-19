package ru.SeitbayBulat.SpiderNetStore.product.dto;

import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.category.Category;
import ru.SeitbayBulat.SpiderNetStore.product.productServices.ProductStockImportService;
import ru.SeitbayBulat.SpiderNetStore.product.stock.StockItem;

@Component
public class ProductManageMapper {

    public ProductManageDto toManageDto(Product p) {
        ProductManageDto dto = new ProductManageDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setRating(p.getRating());
        dto.setStockCount(p.getStockCount());
        dto.setStatus(p.getStatus());
        dto.setFieldSchema(p.getFieldSchema());
        dto.setImageUrl(p.getImageUrl());
        if (p.getCategories() != null) {
            dto.setCategoryNames(p.getCategories().stream().map(Category::getName).toList());
            dto.setCategoryIds(p.getCategories().stream().map(Category::getId).toList());
        }
        if (p.getStockItems() != null) {
            dto.setStockItems(p.getStockItems().stream().map(this::toStockSellerDto).toList());
        }
        return dto;
    }

    public StockItemSellerDto toStockSellerDto(StockItem s) {
        StockItemSellerDto dto = new StockItemSellerDto();
        dto.setId(s.getId());
        dto.setPayloadType(s.getPayloadType().name());
        dto.setStatus(s.getStatus().name());
        dto.setOriginalFilename(s.getOriginalFilename());
        dto.setHasBinary(s.getBinaryPayload() != null && s.getBinaryPayload().length > 0);
        dto.setDataPreview(ProductStockImportService.previewData(s.getData()));
        if (s.getCreatedAt() != null) {
            dto.setCreatedAt(s.getCreatedAt().toString());
        }
        return dto;
    }
}
