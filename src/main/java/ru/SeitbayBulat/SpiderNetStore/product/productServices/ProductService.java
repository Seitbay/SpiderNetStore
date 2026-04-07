package ru.SeitbayBulat.SpiderNetStore.product.productServices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewDto;
import ru.SeitbayBulat.SpiderNetStore.order.review.Review;
import ru.SeitbayBulat.SpiderNetStore.order.review.ReviewRepository;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.ProductRepository;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;
import ru.SeitbayBulat.SpiderNetStore.product.category.Category;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductDetailDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductListDto;

@Service
@RequiredArgsConstructor
public class ProductService {


}