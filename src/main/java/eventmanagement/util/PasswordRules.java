package eventmanagement.util;

import eventmanagement.exception.BadRequestException;

import java.nio.charset.StandardCharsets;

/** Quy tắc mật khẩu dùng chung cho đăng ký và đổi mật khẩu. */
public final class PasswordRules {
    private PasswordRules() {
    }

    public static void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        // BCrypt chỉ xử lý tối đa 72 byte
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("Mật khẩu quá dài (tối đa 72 byte).");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BadRequestException("Mật khẩu phải có cả chữ cái và chữ số.");
        }
    }
}
