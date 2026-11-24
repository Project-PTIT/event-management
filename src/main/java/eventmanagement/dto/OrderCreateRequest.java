package eventmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    @NotEmpty(message = "Danh sách vé không được để trống")
    @Valid
    private List<OrderItemRequest> items;
}