package ru.SeitbayBulat.SpiderNetStore.user.seller.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.SeitbayBulat.SpiderNetStore.user.seller.SellerApplication;
import ru.SeitbayBulat.SpiderNetStore.user.seller.SellerApplicationService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/seller-applications")
@RequiredArgsConstructor
public class AdminSellerApplicationController {

    private final SellerApplicationService sellerApplicationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<SellerApplication>> getPending() {
        return ResponseEntity.ok(sellerApplicationService.getPendingApplications());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        sellerApplicationService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestParam String comment) {
        sellerApplicationService.reject(id, comment);
        return ResponseEntity.ok().build();
    }
}