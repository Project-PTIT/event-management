package eventmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundReasonRequest {
    @NotBlank(message = "Vui lòng nhập lý do hoàn tiền")
    @Size(min = 5, max = 500, message = "Lý do hoàn tiền phải từ 5 đến 500 ký tự")
    private String reason;
}
