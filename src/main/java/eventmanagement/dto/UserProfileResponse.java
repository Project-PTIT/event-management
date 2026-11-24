package eventmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Hồ sơ người dùng (cũng là kết quả đăng nhập, kèm role). Không bao giờ chứa mật khẩu. */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;

    // Customer
    private String gender;
    private LocalDate dateOfBirth;

    // Organizer
    private String taxCode;
    private String address;
}
