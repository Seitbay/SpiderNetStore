package ru.SeitbayBulat.SpiderNetStore.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;
import ru.SeitbayBulat.SpiderNetStore.user.dto.*;
import ru.SeitbayBulat.SpiderNetStore.user.service.UserService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMe(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getCurrentUser(email));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMe(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateProfile(email, request));
    }

    // профиль продавца
    @GetMapping("/{id}")
    public ResponseEntity<PublicUserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicUser(id));
    }

    // для получения баланса
    @GetMapping("/me/balance")
    public ResponseEntity<Map<String, BigDecimal>> getMyBalance(Authentication authentication) {
        BigDecimal balance = userService.getBalance(authentication.getName());
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @PatchMapping("/me/password") // TODO: нужно еще логаутиться
    public ResponseEntity<Map<String, String>> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Не авторизован"));
        }

        try {
            userService.changePassword(authentication.getName(), request);
            return ResponseEntity.ok(Map.of("message", "Пароль успешно изменён"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}