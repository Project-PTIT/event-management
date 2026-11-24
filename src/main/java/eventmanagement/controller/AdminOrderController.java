package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.OrderResponse;
import eventmanagement.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API đơn hàng dành cho Admin (SecurityConfig chỉ cho ROLE_ADMIN vào /api/admin/**). */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /** GET /api/admin/orders?status=Đã thanh toán - danh sách đơn hàng. */
    @GetMapping
    public ApiResponse<List<OrderResponse>> listOrders(@RequestParam(required = false) String status) {
        return ApiResponse.success("Lấy danh sách đơn hàng thành công", adminOrderService.listOrders(status));
    }

    /** PUT /api/admin/orders/{orderId}/refund - hoàn tiền đơn đã thanh toán và hoàn vé. */
    @PutMapping("/{orderId}/refund")
    public ApiResponse<OrderResponse> refund(@PathVariable String orderId,
            @jakarta.validation.Valid @RequestBody(required = false) eventmanagement.dto.RefundNoteRequest body) {
        return ApiResponse.success("Hoàn tiền đơn hàng thành công",
                adminOrderService.refundOrder(orderId, body == null ? null : body.getNote()));
    }

    /** PUT /api/admin/orders/{orderId}/refund-reject - từ chối yêu cầu hoàn tiền (bắt buộc có lý do). */
    @PutMapping("/{orderId}/refund-reject")
    public ApiResponse<OrderResponse> rejectRefund(@PathVariable String orderId,
            @jakarta.validation.Valid @RequestBody eventmanagement.dto.RefundNoteRequest body) {
        return ApiResponse.success("Đã từ chối yêu cầu hoàn tiền",
                adminOrderService.rejectRefund(orderId, body.getNote()));
    }
}
