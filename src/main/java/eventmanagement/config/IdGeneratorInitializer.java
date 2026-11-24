package eventmanagement.config;

import eventmanagement.util.IdGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Nối IdGenerator với database: tra số lớn nhất của từng tiền tố mã trong bảng tương ứng.
 * Tiền tố chưa khai báo ở đây vẫn dùng được, chỉ dựa vào bộ đếm trong bộ nhớ.
 */
@Component
public class IdGeneratorInitializer {

    private static final Pattern SAFE_PREFIX = Pattern.compile("[A-Za-z]+");

    // tiền tố -> {bảng, cột khóa}. Đối chiếu với mydb.sql.
    private static final Map<String, String[]> SOURCES = Map.of(
            "AD", new String[]{"people", "id"},
            "TC", new String[]{"people", "id"},
            "KH", new String[]{"people", "id"},
            "SK", new String[]{"`event`", "event_id"},
            "SCH", new String[]{"event_schedule", "schedule_id"},
            "DD", new String[]{"venue", "venue_id"},
            "P", new String[]{"ward", "ward_id"},
            "NS", new String[]{"artist", "artist_id"},
            "DH", new String[]{"`order`", "order_id"},
            "TK", new String[]{"ticket", "ticket_id"}
    );

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    void register() {
        IdGenerator.setExistingMaxLookup(this::maxNumberOf);
    }

    private long maxNumberOf(String prefix) {
        String[] source = SOURCES.get(prefix);
        if (source == null || !SAFE_PREFIX.matcher(prefix).matches()) {
            return 0;
        }
        String table = source[0];
        String column = source[1];
        String sql = "SELECT MAX(CAST(SUBSTRING(" + column + ", " + (prefix.length() + 1) + ") AS UNSIGNED)) "
                + "FROM " + table + " WHERE " + column + " REGEXP ?1";
        Object result = entityManager.createNativeQuery(sql)
                .setParameter(1, "^" + prefix + "[0-9]+$")
                .getSingleResult();
        return result == null ? 0 : ((Number) result).longValue();
    }
}
