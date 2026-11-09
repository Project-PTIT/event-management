package eventmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketResponse {
    private String ticketId;
    private String ticketName;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private String eventTitle;
    private LocalDateTime startDatetime;
    private String venueName;

    public TicketResponse(String ticketId, String ticketName, BigDecimal price, 
                          Integer totalQuantity, Integer remainingQuantity, 
                          String eventTitle, LocalDateTime startDatetime, String venueName) {
        this.ticketId = ticketId;
        this.ticketName = ticketName;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = remainingQuantity;
        this.eventTitle = eventTitle;
        this.startDatetime = startDatetime;
        this.venueName = venueName;
    }

    // Getters
    public String getTicketId() { return ticketId; }
    public String getTicketName() { return ticketName; }
    public BigDecimal getPrice() { return price; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public Integer getRemainingQuantity() { return remainingQuantity; }
    public String getEventTitle() { return eventTitle; }
    public LocalDateTime getStartDatetime() { return startDatetime; }
    public String getVenueName() { return venueName; }
}