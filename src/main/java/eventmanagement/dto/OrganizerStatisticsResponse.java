package eventmanagement.dto;

import java.math.BigDecimal;

/**
 * Thống kê bán vé của một Organizer (chỉ tính đơn đã thanh toán).
 * Tổng quan: { totalRevenue, ticketsSold, eventCount }.
 * Theo sự kiện: [{ eventId, eventName, revenue, ticketsSold }].
 */
public final class OrganizerStatisticsResponse {

    private OrganizerStatisticsResponse() {
    }

    /** Tổng quan toàn bộ sự kiện của Organizer. */
    public record Overview(BigDecimal totalRevenue, long ticketsSold, long eventCount) {
    }

    /** Doanh thu và số vé đã bán của một sự kiện. */
    public record ByEvent(String eventId, String eventName, BigDecimal revenue, long ticketsSold) {
    }
}
