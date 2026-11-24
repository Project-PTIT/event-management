package eventmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String orderId;
    private String customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentAt;

    private String refundReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundRequestedAt;

    private String refundResponse;

    /** Với đơn 'Chờ thanh toán': thời điểm hệ thống tự hủy nếu chưa thanh toán. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    /** Với đơn 'Đã thanh toán': hạn chót khách được gửi yêu cầu hoàn tiền. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundableUntil;

    private List<OrderDetailResponse> details;
}