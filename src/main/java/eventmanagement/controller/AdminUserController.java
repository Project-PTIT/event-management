package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.UserProfileResponse;
import eventmanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Phần làm thêm của BE1. Quyền ROLE_ADMIN được kiểm tra ở SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> listUsers(
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công.",
                userService.listUsers(role)));
    }
}
