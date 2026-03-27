package ru.SeitbayBulat.SpiderNetStore.user.seller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.SeitbayBulat.SpiderNetStore.user.Role;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerApplicationService {

    private final SellerApplicationRepository applicationRepository;
    private final UserRepository userRepository;


    @Transactional
    public void applyForSeller(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (user.getRole() == Role.SELLER || user.getRole() == Role.ADMIN)
            throw new RuntimeException("Вы уже являетесь продавцом");

        if (applicationRepository.existsByUserAndStatus(user, ApplicationStatus.PENDING))
            throw new RuntimeException("Заявка уже подана и рассматривается");

        SellerApplication application = new SellerApplication();
        application.setUser(user);
        applicationRepository.save(application);
    }


    @Transactional
    public void approve(Long applicationId) {
        SellerApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        app.setStatus(ApplicationStatus.APPROVED);
        app.setReviewedAt(LocalDateTime.now());
        app.getUser().setRole(Role.SELLER);

        applicationRepository.save(app);
    }


    @Transactional
    public void reject(Long applicationId, String comment) {
        SellerApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        app.setStatus(ApplicationStatus.REJECTED);
        app.setAdminComment(comment);
        app.setReviewedAt(LocalDateTime.now());

        applicationRepository.save(app);
    }


    @Transactional(readOnly = true)
    public List<SellerApplication> getPendingApplications() {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<SellerApplication> getByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }
}