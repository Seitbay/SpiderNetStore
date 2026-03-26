package ru.SeitbayBulat.SpiderNetStore.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.SeitbayBulat.SpiderNetStore.user.Role;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;
import ru.SeitbayBulat.SpiderNetStore.user.dto.AuthResponse;
import ru.SeitbayBulat.SpiderNetStore.user.dto.LoginRequest;
import ru.SeitbayBulat.SpiderNetStore.user.dto.RegisterRequest;
import ru.SeitbayBulat.SpiderNetStore.user.security.JwtUtil;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        // Проверяем email на уникальность
        if (userRepository.existsByEmail(request.getEmail())) {
            // TODO: заменить на кастомное исключение EmailAlreadyExistsException
            throw new RuntimeException("Email уже занят");
        }

        // Проверяем username на уникальность
        if (userRepository.existsByUsername(request.getUsername())) {
            // TODO: заменить на кастомное исключение UsernameAlreadyExistsException
            throw new RuntimeException("Username уже занят");
        }

        // Создаём пользователя
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.BUYER);
        user.setBalance(BigDecimal.ZERO);
        // user.setVerified(false);  // если будет подтверждение по почте

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {

        // TODO: заменить на кастомное исключение InvalidCredentialsException
        //       и сделать одинаковое сообщение для "не найден" и "неверный пароль"
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // TODO: то же исключение, что и выше — не раскрывать, найден ли пользователь
            throw new RuntimeException("Неверный пароль");
        }

        // TODO: добавить проверку статуса аккаунта
        // if (!user.isVerified()) {
        //     throw new AccountNotVerifiedException("Аккаунт не подтверждён. Проверьте почту.");
        // }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}