package ru.SeitbayBulat.SpiderNetStore.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;
import ru.SeitbayBulat.SpiderNetStore.user.dto.PublicUserDto;
import ru.SeitbayBulat.SpiderNetStore.user.dto.UpdateProfileRequest;
import ru.SeitbayBulat.SpiderNetStore.user.dto.UserProfileDto;
// import ru.SeitbayBulat.SpiderNetStore.user.exception.UserNotFoundException; TODO: сделать эксепшены

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setBalance(user.getBalance());
        dto.setRole(user.getRole().name());
        dto.setVerified(user.isVerified());
        return dto;
    }

    @Transactional(readOnly = true)
    public PublicUserDto getPublicUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        PublicUserDto dto = new PublicUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username уже занят");
            }
            user.setUsername(request.getUsername());
        }

        user = userRepository.save(user);

        return getCurrentUser(email);
    }

    // TODO: реализовать после пэймента
    // @Transactional
    // public void addBalance(Long userId, BigDecimal amount) { ... }
}