package eventmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Các trường được phép sửa. Email, mã số thuế, id, role và mật khẩu không sửa qua đây.
 * gender / dateOfBirth chỉ áp dụng cho Customer, address chỉ áp dụng cho Organizer
 * (service kiểm tra bắt buộc theo từng loại tài khoản).
 */
@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(max = 200, message = "Họ tên tối đa 200 ký tự.")
    private String name;

    @NotBlank(message = "Vui lòng nhập số điện thoại.")
    @Pattern(regexp = "^\\+?[0-9]{9,14}$", message = "Số điện thoại không hợp lệ (9-14 chữ số).")
    private String phoneNumber;

    @Pattern(regexp = "Male|Female|Other", message = "Giới tính chỉ nhận Male, Female hoặc Other.")
    private String gender;

    @Past(message = "Ngày sinh phải ở quá khứ.")
    private LocalDate dateOfBirth;

    @Size(max = 200, message = "Địa chỉ tối đa 200 ký tự.")
    private String address;
}
