package eventmanagement.util;

/**
 * Bản dùng tạm theo yêu cầu BE1: trả về người dùng cố định để các BE khác
 * có thể tích hợp trước khi phần đăng nhập/session hoàn tất.
 *
 * Sau khi BE1 hoàn thiện đăng nhập, chỉ thay phần triển khai trong class này
 * để lấy id và role từ session/Spring Security.
 */
public final class CurrentUser {
    // Mặc định dùng tài khoản Customer mẫu.
    private static final String USER_ID = "KH001";
    private static final String ROLE = "ROLE_CUSTOMER";

    private CurrentUser() {
    }

    public static String getId() {
        return USER_ID;
    }

    public static String getRole() {
        return ROLE;
    }
}
