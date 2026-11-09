package eventmanagement.controller;

import eventmanagement.dto.TicketRequestDTO;
import eventmanagement.dto.TicketResponse;
import eventmanagement.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // 1. Tạo vé mới (POST)
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody TicketRequestDTO dto) {
        try {
            TicketResponse response = ticketService.createTicket(dto);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Tạo vé thành công!");
            res.put("data", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 2. Lấy tất cả vé (GET)
    @GetMapping
    public ResponseEntity<?> getAllTickets() {
        try {
            List<TicketResponse> tickets = ticketService.getAllTickets();
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", tickets);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 3. Đường dẫn tĩnh: Đếm tổng số vé
    @GetMapping("/stats/count")
    public ResponseEntity<?> getTicketCount() {
        try {
            long total = ticketService.getTicketCount();
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", total);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 4. Đường dẫn tĩnh: Danh sách vé chi tiết
    @GetMapping("/details")
    public ResponseEntity<?> getAllTicketsWithDetails() {
        try {
            List<Object[]> details = ticketService.getAllTicketsWithDetails();
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", details);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 5. Đường dẫn tĩnh: Thống kê sự kiện hot
    @GetMapping("/stats/top-events")
    public ResponseEntity<?> getTopEvents() {
        try {
            List<Object[]> topEvents = ticketService.getTopEvents();
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", topEvents);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 6. Đường dẫn tĩnh: Thống kê doanh thu
    @GetMapping("/stats/revenue")
    public ResponseEntity<?> getRevenueByMonth() {
        try {
            List<Object[]> revenue = ticketService.getRevenueByMonth();
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", revenue);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 7. Lấy 1 vé theo ID (ĐẶT DƯỚI CÁC ĐƯỜNG DẪN TĨNH ĐỂ TRÁNH XUNG ĐỘT)
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable String id) {
        try {
            TicketResponse ticket = ticketService.getTicketById(id);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", ticket);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
    }

    // 8. Cập nhật vé (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable String id, @RequestBody TicketRequestDTO dto) {
        try {
            TicketResponse updatedTicket = ticketService.updateTicket(id, dto);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Cập nhật vé thành công!");
            res.put("data", updatedTicket);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    // 9. Xóa vé (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable String id) {
        try {
            ticketService.deleteTicket(id);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Xóa vé thành công!");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }
}