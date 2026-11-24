package eventmanagement.dto;

import java.math.BigDecimal;

/**
 * Số liệu tổng quan cho Admin.
 * JSON: { totalUsers, totalRevenue, totalEvents, pendingEvents, approvedEvents, rejectedEvents }.
 */
public record AdminStatisticsResponse(
        long totalUsers,
        BigDecimal totalRevenue,
        long totalEvents,
        long pendingEvents,
        long approvedEvents,
        long rejectedEvents) {
}
