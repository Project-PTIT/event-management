package eventmanagement.service;

import eventmanagement.repository.OrderRepository;
import eventmanagement.util.AppTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Hủy đơn 'Chờ thanh toán' quá hạn giữ chỗ và hoàn vé.
 * Nằm ở bean riêng để OrderService.cancelExpiredOne đi qua proxy và có transaction
 * (gọi trong cùng class sẽ bỏ qua @Transactional).
 */
@Component
@RequiredArgsConstructor
public class OrderExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderExpiryScheduler.class);

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 60_000)
    public void cancelExpiredOrders() {
        LocalDateTime cutoff = AppTime.now().minusMinutes(OrderService.PENDING_TIMEOUT_MINUTES);
        for (String orderId : orderRepository.findExpiredPendingIds(cutoff)) {
            try {
                orderService.cancelExpiredOne(orderId);
            } catch (RuntimeException ex) {
                log.warn("Không hủy được đơn quá hạn {}: {}", orderId, ex.getMessage());
            }
        }
    }
}
