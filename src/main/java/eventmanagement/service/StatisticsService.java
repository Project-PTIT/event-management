package eventmanagement.service;

import eventmanagement.dto.AdminStatisticsResponse;
import eventmanagement.dto.OrganizerStatisticsResponse;
import eventmanagement.entity.Event;
import eventmanagement.exception.ForbiddenException;
import eventmanagement.repository.EventRepository;
import eventmanagement.repository.OrderDetailRepository;
import eventmanagement.repository.OrderRepository;
import eventmanagement.repository.PeopleRepository;
import eventmanagement.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thống kê cho Admin (toàn hệ thống) và cho Organizer (chỉ sự kiện của mình, lấy theo CurrentUser).
 * Doanh thu chỉ tính đơn 'Đã thanh toán'; đơn đã hủy hoặc đã hoàn tiền không được cộng vào.
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final PeopleRepository peopleRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    /** Tổng quan toàn hệ thống cho Admin. */
    @Transactional(readOnly = true)
    public AdminStatisticsResponse adminOverview() {
        if (!CurrentUser.isAdmin()) {
            throw new ForbiddenException("Chỉ quản trị viên mới xem được thống kê hệ thống.");
        }
        long pending = eventRepository.countByStatus("PENDING");
        long approved = eventRepository.countByStatus("APPROVED");
        long rejected = eventRepository.countByStatus("REJECTED");
        return new AdminStatisticsResponse(
                peopleRepository.count(),
                orderRepository.sumPaidRevenue(),
                pending + approved + rejected,
                pending, approved, rejected);
    }

    /** Tổng quan của Organizer đang đăng nhập. */
    @Transactional(readOnly = true)
    public OrganizerStatisticsResponse.Overview organizerOverview() {
        String organizerId = currentOrganizerId();
        List<OrganizerStatisticsResponse.ByEvent> rows = buildByEvent(organizerId);
        BigDecimal revenue = BigDecimal.ZERO;
        long sold = 0;
        for (OrganizerStatisticsResponse.ByEvent row : rows) {
            revenue = revenue.add(row.revenue());
            sold += row.ticketsSold();
        }
        return new OrganizerStatisticsResponse.Overview(revenue, sold, rows.size());
    }

    /** Doanh thu và số vé theo từng sự kiện của Organizer đang đăng nhập (kể cả sự kiện chưa bán được vé). */
    @Transactional(readOnly = true)
    public List<OrganizerStatisticsResponse.ByEvent> organizerByEvent() {
        return buildByEvent(currentOrganizerId());
    }

    // ======================= HÀM PHỤ =======================

    /** Mã Organizer lấy từ session (không nhận từ client). */
    private String currentOrganizerId() {
        if (!CurrentUser.isOrganizer()) {
            throw new ForbiddenException("Chỉ nhà tổ chức mới xem được thống kê bán vé.");
        }
        return CurrentUser.getId();
    }

    /** Ghép danh sách sự kiện của Organizer với số liệu bán hàng; sự kiện chưa bán được vé hiện 0. */
    private List<OrganizerStatisticsResponse.ByEvent> buildByEvent(String organizerId) {
        Map<String, Object[]> sales = new HashMap<>();
        for (Object[] row : orderDetailRepository.sumSalesByEventForOrganizer(organizerId)) {
            sales.put((String) row[0], row);
        }
        return eventRepository.findByOrganizer_Id(organizerId).stream()
                .map((Event e) -> {
                    Object[] row = sales.get(e.getEventId());
                    BigDecimal revenue = row == null ? BigDecimal.ZERO : (BigDecimal) row[1];
                    long sold = row == null ? 0L : ((Number) row[2]).longValue();
                    return new OrganizerStatisticsResponse.ByEvent(e.getEventId(), e.getTitle(), revenue, sold);
                })
                .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                .toList();
    }
}
