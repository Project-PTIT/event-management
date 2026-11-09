package eventmanagement.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bộ sinh mã theo tiền tố và độ rộng.
 * Ví dụ: nextId("SK", 2) -> SK01; nextId("V", 3) -> V001.
 *
 * Bộ đếm trong phiên chạy hiện tại; nếu cần sinh mã dựa trên dữ liệu đã có
 * trong database thì service gọi hàm này cần kiểm tra/tránh trùng mã.
 */
public final class IdGenerator {
    private static final ConcurrentHashMap<String, AtomicLong> COUNTERS =
            new ConcurrentHashMap<>();

    private IdGenerator() {
    }

    public static String nextId(String prefix, int width) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Tiền tố không được để trống.");
        }
        if (width < 1) {
            throw new IllegalArgumentException("Độ rộng mã phải lớn hơn 0.");
        }

        long number = COUNTERS.computeIfAbsent(prefix, key -> new AtomicLong(0))
                .incrementAndGet();

        return prefix + String.format("%0" + width + "d", number);
    }
}
