package eventmanagement.dto;

import lombok.*;

import java.math.BigDecimal;

/** Một dòng vé trong đơn hàng. price là giá tại thời điểm đặt (lấy từ order_detail). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {
    private String ticketId;
    private String ticketName;
    private String eventName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}
