package eventmanagement.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/** Ghi chú của Admin khi duyệt (tùy chọn) hoặc từ chối (bắt buộc) yêu cầu hoàn tiền. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundNoteRequest {
    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
