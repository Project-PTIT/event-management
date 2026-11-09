package eventmanagement.repository;

import eventmanagement.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {
    // Tìm chi tiết đơn theo mã đơn hàng
    List<OrderDetail> findByOrder_OrderId(String orderId);
}