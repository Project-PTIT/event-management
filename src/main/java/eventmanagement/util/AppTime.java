package eventmanagement.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Giờ hiện tại theo múi giờ của hệ thống (Asia/Ho_Chi_Minh), cùng múi giờ với chuỗi kết nối JDBC
 * (serverTimezone=Asia/Ho_Chi_Minh) nên so sánh với các cột DATETIME luôn nhất quán.
 */
public final class AppTime {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private AppTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }
}
