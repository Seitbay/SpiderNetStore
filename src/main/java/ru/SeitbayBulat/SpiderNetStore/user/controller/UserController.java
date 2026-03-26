package ru.SeitbayBulat.SpiderNetStore.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.user.dto.UserDto;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;
import ru.SeitbayBulat.SpiderNetStore.user.User;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(Authentication authentication) {
        // Authentication приходит автоматически из SecurityContext
        // JwtFilter уже положил туда юзера
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        UserDto dto = new UserDto();
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setBalance(user.getBalance());
        dto.setRole(user.getRole().name());

        return ResponseEntity.ok(dto);
    }
}