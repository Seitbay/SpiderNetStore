package ru.SeitbayBulat.SpiderNetStore.user.seller.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.user.seller.SellerApplicationService;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class SellerApplicationController {

    private final SellerApplicationService sellerApplicationService;

    @PostMapping("/seller-application")
    public ResponseEntity<Void> apply(Authentication authentication) {
        sellerApplicationService.applyForSeller(authentication.getName());
        return ResponseEntity.ok().build();
    }
}