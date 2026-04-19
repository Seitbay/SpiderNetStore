package ru.SeitbayBulat.SpiderNetStore.order.dispute;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.order.dto.SellerRespondDisputeRequest;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;
    private final DisputeMapper disputeMapper;

    @GetMapping("/{disputeId}")
    public ResponseEntity<?> getDispute(@PathVariable Long disputeId,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Dispute dispute = disputeService.getDisputeForParticipant(disputeId, principal.getId());
        return ResponseEntity.ok(disputeMapper.toDto(dispute));
    }

    @PostMapping("/{disputeId}/respond")
    public ResponseEntity<?> respond(@PathVariable Long disputeId,
                                     @Valid @RequestBody SellerRespondDisputeRequest req,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Dispute dispute = disputeService.respondAsSeller(disputeId, principal.getId(), req.getResponse());
        return ResponseEntity.ok(disputeMapper.toDto(dispute));
    }

    @PostMapping("/{disputeId}/escalate")
    public ResponseEntity<?> escalate(@PathVariable Long disputeId,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Dispute dispute = disputeService.escalate(disputeId, principal.getId());
        return ResponseEntity.ok(disputeMapper.toDto(dispute));
    }

    private static ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Не авторизован"));
    }
}
