package ru.SeitbayBulat.SpiderNetStore.order.dispute;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.order.dto.DisputeDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ResolveDisputeRequest;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
public class AdminDisputeController {

    private final DisputeService disputeService;
    private final DisputeMapper disputeMapper;

    @PostMapping("/{disputeId}/resolve")
    public ResponseEntity<?> resolve(@PathVariable Long disputeId,
                                     @Valid @RequestBody ResolveDisputeRequest req,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Не авторизован"));
        }
        Dispute dispute = disputeService.resolveByModerator(
                disputeId, principal.getId(), req.getOutcome(), req.getResolution());
        return ResponseEntity.ok(disputeMapper.toDto(dispute));
    }
}
