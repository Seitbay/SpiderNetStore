package ru.SeitbayBulat.SpiderNetStore.user.seller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.SeitbayBulat.SpiderNetStore.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Long> {
    boolean existsByUserAndStatus(User user, ApplicationStatus status);
    List<SellerApplication> findByStatus(ApplicationStatus status);
    Optional<SellerApplication> findByUser(User user);
}