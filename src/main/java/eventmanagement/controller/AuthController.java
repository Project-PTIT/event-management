package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.LoginRequest;
import eventmanagement.dto.RegisterCustomerRequest;
import eventmanagement.dto.RegisterOrganizerRequest;
import eventmanagement.dto.UserProfileResponse;
import eventmanagement.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<UserProfileResponse>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản khách hàng thành công.",
                        authService.registerCustomer(request)));
    }

    @PostMapping("/register/organizer")
    public ResponseEntity<ApiResponse<UserProfileResponse>> registerOrganizer(
            @Valid @RequestBody RegisterOrganizerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản nhà tổ chức thành công.",
                        authService.registerOrganizer(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserProfileResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công.",
                authService.login(request, httpRequest, httpResponse)));
    }

    /** Yêu cầu đã đăng nhập (SecurityConfig trả 401 nếu chưa). Chỉ hủy session của chính người gọi. */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Hủy session phía server (invalidate) và xóa SecurityContext của request hiện tại
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        SecurityContextHolder.clearContext();
        // Không cho trình duyệt/proxy lưu cache phản hồi đăng xuất
        response.setHeader("Cache-Control", "no-store");

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công.", null));
    }
}
