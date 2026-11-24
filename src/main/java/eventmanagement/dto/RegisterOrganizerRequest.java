package eventmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterOrganizerRequest {

    @NotBlank(message = "Vui lòng nhập tên nhà tổ chức.")
    @Size(max = 200, message = "Tên tối đa 200 ký tự.")
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

    @NotBlank(message = "Vui lòng nhập mã số thuế.")
    @Pattern(regexp = "^[A-Za-z0-9-]{5,50}$", message = "Mã số thuế gồm 5-50 ký tự chữ, số hoặc dấu gạch ngang.")
    private String taxCode;

    @NotBlank(message = "Vui lòng nhập địa chỉ.")
    @Size(max = 200, message = "Địa chỉ tối đa 200 ký tự.")
    private String address;
}
