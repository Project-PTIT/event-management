package eventmanagement.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToLongFunction;

/**
 * Bộ sinh mã theo tiền tố và độ rộng.
 * Ví dụ: nextId("SK", 2) -> SK01; nextId("DH", 3) -> DH001.
 *
 * Mỗi lần sinh mã lấy max(số lớn nhất đang có trong database, bộ đếm trong bộ nhớ) + 1.
 * - Số lớn nhất trong database do {@code IdGeneratorInitializer} cung cấp, nên khởi động lại
 *   ứng dụng không bị sinh trùng mã đã có.
 * - Bộ đếm trong bộ nhớ giúp nhiều request đồng thời nhận mã khác nhau dù bản ghi chưa kịp commit.
 * Chữ ký nextId(prefix, width) giữ nguyên để các thành viên khác dùng tiếp.
 */
public final class IdGenerator {
    private static final ConcurrentHashMap<String, AtomicLong> COUNTERS = new ConcurrentHashMap<>();
    private static volatile ToLongFunction<String> existingMaxLookup;

    private IdGenerator() {
    }

    /** Đăng ký hàm tra số lớn nhất hiện có theo tiền tố (trả 0 nếu chưa có / không tra được). */
    public static void setExistingMaxLookup(ToLongFunction<String> lookup) {
        existingMaxLookup = lookup;
    }

    public static synchronized String nextId(String prefix, int width) {
    public static String nextId(String prefix, int width) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Tiền tố không được để trống.");
        }
        if (width < 1) {
            throw new IllegalArgumentException("Độ rộng mã phải lớn hơn 0.");
        }

        ToLongFunction<String> lookup = existingMaxLookup;
        long inDatabase = lookup == null ? 0 : lookup.applyAsLong(prefix);
        AtomicLong counter = COUNTERS.computeIfAbsent(prefix, key -> new AtomicLong(0));
        long number = Math.max(counter.get(), inDatabase) + 1;
        counter.set(number);

        return prefix + String.format("%0" + width + "d", number);
    }
}
