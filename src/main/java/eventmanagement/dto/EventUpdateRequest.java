package eventmanagement.dto;

/** Dữ liệu sửa sự kiện (PUT = gửi đủ các trường). */
public record EventUpdateRequest(
        String title,
        String image,
        String desc,
        String createType) {
}
