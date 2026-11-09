package eventmanagement.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TicketRequestDTO {
    private String ticketId;
    private String scheduleId;
    private String ticketName;
    private BigDecimal price;
    private String desc;
    private Integer totalQuantity;
    private Integer remainingQuantity;
}