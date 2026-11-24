# Các thay đổi đã vá
- Ảnh sự kiện: upload từ máy (POST /api/organizer/uploads/event-image, lưu ./uploads/events, phục vụ qua /uploads/**); placeholder local thay ảnh Unsplash cố định.
- Giỏ vé: phải đăng nhập (ROLE_CUSTOMER) mới thêm được; ẩn nút giỏ với khách; xóa giỏ khi login/logout/hết phiên; cart.html chỉ Customer.
- Sửa sự kiện đã APPROVED -> quay về PENDING chờ duyệt lại.
- GET /api/events/{id}: dùng AccessRules.canView (khách xem sự kiện chưa duyệt nhận 404, không còn 401).
- Đơn "Chờ thanh toán" quá 15 phút tự hủy và hoàn vé (@Scheduled, mỗi phút).
- Không cho đặt vé suất đã bắt đầu/kết thúc; frontend vô hiệu hóa nút mua cho suất đó.
- Trang chủ: "Sắp diễn ra" không còn trùng "Nổi bật"; nhãn "Đang mở bán" dựa vào minPrice thật.
Chưa sửa: thống kê doanh thu theo tháng chưa tách năm; thanh toán vẫn là giả lập; chưa chạy build Maven (môi trường không có Maven/JDK) nên hãy chạy `mvnw clean package` để kiểm tra.

## Vòng rà soát 3
- Sửa sự kiện đã duyệt: chỉ quay về PENDING khi đổi tên hoặc ảnh; sửa mô tả/chu kỳ giữ nguyên APPROVED.
- Admin duyệt sự kiện: nút "Xem" mở cửa sổ chi tiết (ảnh, mô tả, nhà tổ chức, suất diễn, nghệ sĩ).
- Thống kê doanh thu theo tháng: tách theo năm (trả [năm, tháng, doanh thu]) và tính theo ngày thanh toán.
- Trang checkout ghi rõ thanh toán đang giả lập.

## Vòng rà soát 2
- Sửa lỗi job hủy đơn quá hạn không chạy (gọi @Transactional trong cùng class): tách thành OrderExpiryScheduler.
- Trang chủ: "Sắp diễn ra" thật sự khác "Nổi bật".
- Admin duyệt sự kiện: cột tên sự kiện không còn trống (dùng title).
- Upload ảnh quá 5MB trả 400 rõ ràng thay vì 500.
- Giá "Từ ..." chỉ tính suất chưa bắt đầu (khớp với chặn đặt vé suất đã bắt đầu).
- Thêm lại thư mục static/images/events/*.svg cho dữ liệu mẫu.

## Vòng 4: thanh toán lại và yêu cầu hoàn tiền
- Đơn "Chờ thanh toán": trang chi tiết đơn có nút Thanh toán ngay (chọn phương thức) và Hủy đơn, hiện giờ hết hạn; danh sách đơn có nút Thanh toán.
- Hoàn tiền hai chiều: khách gửi yêu cầu (kèm lý do, trước giờ diễn ≥ 24h), có thể rút lại; Admin duyệt hoặc từ chối (bắt buộc ghi lý do). Admin vẫn hoàn tiền trực tiếp được.
- Trạng thái mới 'Chờ hoàn tiền'; thêm 3 cột refund_reason, refund_requested_at, refund_response vào bảng order (chạy lại mydb.sql). Doanh thu vẫn tính đơn 'Chờ hoàn tiền' cho tới khi được duyệt hoàn.
- Dữ liệu mẫu có thêm DH009 (KH001) đang 'Chờ hoàn tiền' để test.
