package eventmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterCustomerRequest {

    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(max = 200, message = "Họ tên tối đa 200 ký tự.")
    private String name;

    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không hợp lệ.")
    @Size(max = 200, message = "Email tối đa 200 ký tự.")
    private String email;

    @NotBlank(message = "Vui lòng nhập số điện thoại.")
    @Pattern(regexp = "^\\+?[0-9]{9,14}$", message = "Số điện thoại không hợp lệ (9-14 chữ số).")
    private String phoneNumber;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự.")
    private String password;

    @NotBlank(message = "Vui lòng chọn giới tính.")
    @Pattern(regexp = "Male|Female|Other", message = "Giới tính chỉ nhận Male, Female hoặc Other.")
    private String gender;

    @NotNull(message = "Vui lòng nhập ngày sinh.")
    @Past(message = "Ngày sinh phải ở quá khứ.")
    private LocalDate dateOfBirth;
}
