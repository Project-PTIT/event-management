package eventmanagement.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {
    private String ticketId;
    private String ticketName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}