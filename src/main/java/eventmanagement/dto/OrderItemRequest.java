package eventmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotBlank(message = "ticketId không được để trống")
    private String ticketId;

    @Min(value = 1, message = "Số lượng mua tối thiểu là 1")
    private int quantity;
}