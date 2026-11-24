package eventmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderRequest {
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;
}