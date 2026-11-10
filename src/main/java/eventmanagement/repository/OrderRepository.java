package eventmanagement.repository;

import eventmanagement.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    // Customer kế thừa People nên thuộc tính khóa là 'id'
    List<Order> findByCustomer_Id(String customerId);

    // --- BE5 thêm các method sắp xếp và lọc phục vụ /api/orders/me ---
    List<Order> findByCustomer_IdOrderByCreatedAtDesc(String customerId);

    List<Order> findByCustomer_IdAndStatusOrderByCreatedAtDesc(String customerId, String status);
}
