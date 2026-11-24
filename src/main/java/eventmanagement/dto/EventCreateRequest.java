package eventmanagement.dto;

/**
 * Dữ liệu tạo sự kiện. createType: null, WEEKLY, MONTHLY hoặc YEARLY.
 * Việc kiểm tra dữ liệu làm trong EventService.
 */
public record EventCreateRequest(
        String title,
        String image,
        String desc,
        String createType) {
}
