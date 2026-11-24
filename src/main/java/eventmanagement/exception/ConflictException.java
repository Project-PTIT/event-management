package eventmanagement.exception;

/** Xung đột/không nhất quán dữ liệu phát hiện trong nghiệp vụ; GlobalExceptionHandler trả HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
