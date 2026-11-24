package eventmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Dữ liệu tạo/sửa hạng vé. JSON: { ticketName, desc, price, totalQuantity, remainingQuantity? }.
 * Mã vé và mã suất diễn KHÔNG nhận từ client: mã vé do server sinh, suất diễn lấy từ đường dẫn.
 * remainingQuantity là tùy chọn và chỉ được phép bằng đúng giá trị server tính (tổng vé - vé đã bán),
 * để không ai ghi đè số vé còn lại làm sai dữ liệu bán vé.
 */
@Getter
@Setter
public class TicketRequestDTO {
    private String ticketName;
    private String desc;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer remainingQuantity;
}
