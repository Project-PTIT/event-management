package eventmanagement.controller;

import eventmanagement.dto.*;
import eventmanagement.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Tạo đơn đặt vé mới (status mặc định: 'Chờ thanh toán')
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đơn hàng thành công", response));
    }

    /**
     * GET /api/orders/me
     * Lịch sử đơn hàng của khách hàng hiện tại (có thể lọc theo status)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @RequestParam(name = "status", required = false) String status) {
        List<OrderResponse> responses = orderService.getMyOrders(status);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", responses));
    }

    /**
     * GET /api/orders/{orderId}
     * Xem chi tiết một đơn hàng
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(@PathVariable("orderId") String orderId) {
        OrderResponse response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", response));
    }

    /**
     * POST /api/orders/{orderId}/pay
     * Thanh toán đơn hàng: chuyển sang 'Đã thanh toán' và ghi nhận payment_at
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> payOrder(
            @PathVariable("orderId") String orderId,
            @Valid @RequestBody PayOrderRequest request) {
        OrderResponse response = orderService.payOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Thanh toán đơn hàng thành công", response));
    }

    /**
     * PUT /api/orders/{orderId}/cancel
     * Hủy đơn hàng đang 'Chờ thanh toán', hoàn lại số lượng vé
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable("orderId") String orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", response));
    }
}