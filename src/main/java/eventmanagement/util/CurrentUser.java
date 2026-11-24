package eventmanagement.util;

import eventmanagement.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Lấy người dùng đang đăng nhập từ Spring Security (session).
 * Giữ nguyên getId() / getRole() như bản tạm trước đó nên các BE khác không phải sửa gì.
 * Chưa đăng nhập thì ném AuthenticationCredentialsNotFoundException -> GlobalExceptionHandler trả 401.
 * Service luôn dùng id này để xác định quyền sở hữu, không tin id do client gửi.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    private static CustomUserDetails principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AuthenticationCredentialsNotFoundException("Bạn chưa đăng nhập.");
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails;
    }

    /** Mã người dùng, ví dụ AD01, TC001, KH001. */
    public static String getId() {
        return principal().getId();
    }

    /** ROLE_ADMIN, ROLE_ORGANIZER hoặc ROLE_CUSTOMER. */
    public static String getRole() {
        return principal().getRole();
    }

    public static String getEmail() {
        return principal().getEmail();
    }

    public static boolean isAdmin() {
        return CustomUserDetails.ROLE_ADMIN.equals(getRole());
    }

    public static boolean isOrganizer() {
        return CustomUserDetails.ROLE_ORGANIZER.equals(getRole());
    }

    public static boolean isCustomer() {
        return CustomUserDetails.ROLE_CUSTOMER.equals(getRole());
    }
}
