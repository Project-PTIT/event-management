package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "`order`")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    @Column(name = "order_id", length = 20)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 100)
    private String status;

    @Column(name = "payment_method", length = 500)
    private String paymentMethod;

    @Column(name = "payment_at")
    private LocalDateTime paymentAt;

    /** Lý do khách yêu cầu hoàn tiền. */
    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "refund_requested_at")
    private LocalDateTime refundRequestedAt;

    /** Ghi chú phản hồi của Admin khi duyệt hoặc từ chối hoàn tiền. */
    @Column(name = "refund_response", length = 500)
    private String refundResponse;

    @JsonIgnore
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details = new ArrayList<>();

    public void addDetail(OrderDetail detail) {
        if (detail == null) return;
        details.add(detail);
        detail.setOrder(this);
    }

    public void removeDetail(OrderDetail detail) {
        if (detail == null) return;
        details.remove(detail);
        detail.setOrder(null);
    }
}
