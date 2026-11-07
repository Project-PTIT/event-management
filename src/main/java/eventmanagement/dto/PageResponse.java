package eventmanagement.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Khung phân trang dùng chung cho các API có page/size.
 * page tính từ 0 (đúng như Spring Data).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}
