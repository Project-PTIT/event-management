DROP DATABASE IF EXISTS event_tickets;
CREATE DATABASE event_tickets CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE event_tickets;

CREATE TABLE people (
    id              VARCHAR(20) PRIMARY KEY,
    `name`          VARCHAR(1000) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    phone_number    VARCHAR(15) NOT NULL,
    `password`      VARCHAR(255) NOT NULL
);

CREATE TABLE `admin` (
    admin_id        VARCHAR(20) PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES people(id)
);

CREATE TABLE organizer (
    organizer_id    VARCHAR(20) PRIMARY KEY,
    tax_code        VARCHAR(100) NOT NULL UNIQUE,
    address         VARCHAR(200) NOT NULL,
    FOREIGN KEY (organizer_id) REFERENCES people(id)
);

CREATE TABLE customer (
    customer_id     VARCHAR(20) PRIMARY KEY,
    gender          VARCHAR(10) NOT NULL,
    date_of_birth   DATE NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES people(id),
    CHECK (gender IN ('Male', 'Female', 'Other'))
);

CREATE TABLE province (
    province_id     VARCHAR(20) PRIMARY KEY,
    province_name   VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE ward (
    ward_id         VARCHAR(20) PRIMARY KEY,
    ward_name       VARCHAR(100) NOT NULL,
    province_id     VARCHAR(20) NOT NULL,
    FOREIGN KEY (province_id) REFERENCES province(province_id)
);

CREATE TABLE venue (
    venue_id VARCHAR(20) PRIMARY KEY,
    venue_name VARCHAR(1000) NOT NULL,
    address VARCHAR(200) NOT NULL,
    capacity INT NOT NULL,
    ward_id VARCHAR(20) NOT NULL,
    FOREIGN KEY (ward_id) REFERENCES ward(ward_id),
    CHECK (capacity > 0)
);

CREATE TABLE `event` (
    event_id        VARCHAR(20) PRIMARY KEY,
    organizer_id    VARCHAR(20) NOT NULL,
    title           VARCHAR(1000) NOT NULL,
    image           VARCHAR(1000) NOT NULL,
    `desc`          VARCHAR(1000) NOT NULL,
    create_type     VARCHAR(100),
    `status`        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (organizer_id) REFERENCES organizer(organizer_id),
    CHECK (`status` IN ('PENDING', 'APPROVED', 'REJECTED')),
    CHECK (create_type IN ('WEEKLY', 'MONTHLY', 'YEARLY'))
);

CREATE TABLE event_schedule (
    schedule_id     VARCHAR(20) PRIMARY KEY,
    event_id        VARCHAR(20) NOT NULL,
    venue_id        VARCHAR(20) NOT NULL,
    start_datetime  DATETIME NOT NULL,
    end_datetime    DATETIME NOT NULL,
    `status`        VARCHAR(50) NOT NULL,
    FOREIGN KEY (event_id) REFERENCES `event`(event_id),
    FOREIGN KEY (venue_id) REFERENCES venue(venue_id),
    CHECK (end_datetime > start_datetime),
    CHECK (`status` IN ('Mở đặt vé sớm', 'Đang mở bán', 'Sắp diễn ra', 'Đang diễn ra', 'Đã kết thúc'))
);

CREATE TABLE artist (
    artist_id       VARCHAR(20) PRIMARY KEY,
    artist_name     VARCHAR(200) NOT NULL,
    nationality     VARCHAR(100) NOT NULL,
    image           VARCHAR(1000) NOT NULL,
    bio             VARCHAR(1000)
);

CREATE TABLE schedule_artist (
    artist_id       VARCHAR(20) NOT NULL,
    schedule_id     VARCHAR(20) NOT NULL,
    `role`          VARCHAR(100) NOT NULL,
    PRIMARY KEY (artist_id, schedule_id),
    FOREIGN KEY (artist_id) REFERENCES artist(artist_id),
    FOREIGN KEY (schedule_id) REFERENCES event_schedule(schedule_id)
);

CREATE TABLE ticket (
    ticket_id           VARCHAR(20) PRIMARY KEY,
    schedule_id         VARCHAR(20) NOT NULL,
    ticket_name         VARCHAR(200) NOT NULL,
    price               DECIMAL(15,0) NOT NULL,
    `desc`              VARCHAR(2000) NOT NULL,
    total_quantity      INT NOT NULL,
    remaining_quantity  INT NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES event_schedule(schedule_id),
    CHECK (price >= 0),
    UNIQUE (schedule_id, ticket_name),
    CHECK (total_quantity >= 0),
    CHECK (remaining_quantity >= 0 AND remaining_quantity <= total_quantity)
);

CREATE TABLE `order` (
    order_id        VARCHAR(20) PRIMARY KEY,
    customer_id     VARCHAR(20) NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount    DECIMAL(15,0) NOT NULL,
    `status`        VARCHAR(100) NOT NULL,
    payment_method  VARCHAR(500),
    payment_at      DATETIME,
    refund_reason       VARCHAR(500),
    refund_requested_at DATETIME,
    refund_response     VARCHAR(500),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CHECK (total_amount > 0),
    CHECK (payment_at IS NULL OR payment_at >= created_at),
    CHECK (`status` IN ('Chờ thanh toán', 'Đã thanh toán', 'Đã hủy', 'Đã hoàn tiền', 'Chờ hoàn tiền')),
    CHECK (
        (`status` IN ('Đã thanh toán', 'Đã hoàn tiền', 'Chờ hoàn tiền') AND payment_at IS NOT NULL)
        OR (`status` IN ('Chờ thanh toán', 'Đã hủy') AND payment_at IS NULL)
    )
);

CREATE TABLE order_detail (
    order_id        VARCHAR(20) NOT NULL,
    ticket_id       VARCHAR(20) NOT NULL,
    quantity        INT NOT NULL,
    price           DECIMAL(15,0) NOT NULL,
    PRIMARY KEY (order_id, ticket_id),
    FOREIGN KEY (order_id) REFERENCES `order`(order_id),
    FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id),
    CHECK (quantity > 0),
    CHECK (price >= 0)
);

INSERT INTO people (id, `name`, email, phone_number, `password`) VALUES
('AD01', 'Bùi Ngọc Hải', 'admin01@event.vn', '0901000001', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('AD02', 'Phạm Hoàng Hiệp', 'admin02@event.vn', '0901000002', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('TC001', 'Công ty Giải trí M-TP Entertainment', 'contact@mtpentertainment.vn', '0902000001', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('TC002', 'Viet Vision Entertainment', 'info@vietvision.vn', '0902000002', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('TC003', 'SpaceSpeakers Group', 'booking@spacespeakers.vn', '0902000003', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('TC004', 'Nhà hát Nghệ thuật Đương đại', 'sukien@nhahatduongdai.vn', '0902000004', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('KH001', 'Nguyễn Minh Anh', 'minhanh.nguyen@gmail.com', '0911000001', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('KH002', 'Trần Quốc Bảo', 'quocbao.tran@gmail.com', '0911000002', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('KH003', 'Lê Thu Hà', 'thuha.le@gmail.com', '0911000003', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('KH004', 'Phạm Đức Duy', 'ducduy.pham@gmail.com', '0911000004', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('KH005', 'Hoàng Lan Chi', 'lanchi.hoang@gmail.com', '0911000005', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('KH006', 'Vũ Tuấn Kiệt', 'tuankiet.vu@gmail.com', '0911000006', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('KH007', 'Đặng Ngọc Linh', 'ngoclinh.dang@gmail.com', '0911000007', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('KH008', 'Bùi Hải Đăng', 'haidang.bui@gmail.com', '0911000008', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK');

INSERT INTO `admin` (admin_id) VALUES
('AD01'),
('AD02');

INSERT INTO organizer (organizer_id, tax_code, address) VALUES
('TC001', '0314159265', 'Tầng 5, Tòa nhà Bitexco, Quận 1, TP. Hồ Chí Minh'),
('TC002', '0309876543', 'Số 45 Lý Tự Trọng, Quận 1, TP. Hồ Chí Minh'),
('TC003', '0316543210', 'Số 12 Đường Thảo Điền, TP. Thủ Đức, TP. Hồ Chí Minh'),
('TC004', '0102345678', 'Số 16 Lê Thái Tổ, Hoàn Kiếm, Hà Nội');

INSERT INTO customer (customer_id, gender, date_of_birth) VALUES
('KH001', 'Female', '2001-05-14'),
('KH002', 'Male', '1998-11-22'),
('KH003', 'Female', '2003-02-09'),
('KH004', 'Male', '1995-08-30'),
('KH005', 'Female', '2000-12-19'),
('KH006', 'Male', '1999-04-03'),
('KH007', 'Other', '2002-07-25'),
('KH008', 'Male', '1997-09-11');

INSERT INTO province (province_id, province_name) VALUES
('29', 'Thành phố Hà Nội'),
('11', 'Tỉnh Cao Bằng'),
('22', 'Tỉnh Tuyên Quang'),
('27', 'Tỉnh Điện Biên'),
('25', 'Tỉnh Lai Châu'),
('26', 'Tỉnh Sơn La'),
('24', 'Tỉnh Lào Cai'),
('20', 'Tỉnh Thái Nguyên'),
('12', 'Tỉnh Lạng Sơn'),
('14', 'Tỉnh Quảng Ninh'),
('99', 'Tỉnh Bắc Ninh'),
('19', 'Tỉnh Phú Thọ'),
('15', 'Thành phố Hải Phòng'),
('89', 'Tỉnh Hưng Yên'),
('35', 'Tỉnh Ninh Bình'),
('36', 'Tỉnh Thanh Hóa'),
('37', 'Tỉnh Nghệ An'),
('38', 'Tỉnh Hà Tĩnh'),
('74', 'Tỉnh Quảng Trị'),
('75', 'Thành phố Huế'),
('43', 'Thành phố Đà Nẵng'),
('76', 'Tỉnh Quảng Ngãi'),
('81', 'Tỉnh Gia Lai'),
('79', 'Tỉnh Khánh Hòa'),
('47', 'Tỉnh Đắk Lắk'),
('49', 'Tỉnh Lâm Đồng'),
('60', 'Tỉnh Đồng Nai'),
('59', 'Thành phố Hồ Chí Minh'),
('70', 'Tỉnh Tây Ninh'),
('66', 'Tỉnh Đồng Tháp'),
('64', 'Tỉnh Vĩnh Long'),
('68', 'Tỉnh An Giang'),
('65', 'Thành phố Cần Thơ'),
('69', 'Tỉnh Cà Mau');

INSERT INTO ward (ward_id, ward_name, province_id) VALUES
('P001', 'Hoàn Kiếm', '29'),
('P002', 'Cửa Nam', '29'),
('P003', 'Ba Đình', '29'),
('P004', 'Ngọc Hà', '29'),
('P005', 'Giảng Võ', '29'),
('P006', 'Hai Bà Trưng', '29'),
('P007', 'Vĩnh Tuy', '29'),
('P008', 'Bạch Mai', '29'),
('P009', 'Đống Đa', '29'),
('P010', 'Kim Liên', '29'),
('P011', 'Văn Miếu - Quốc Tử Giám', '29'),
('P012', 'Láng', '29'),
('P013', 'Ô Chợ Dừa', '29'),
('P014', 'Hồng Hà', '29'),
('P015', 'Lĩnh Nam', '29'),
('P016', 'Hoàng Mai', '29'),
('P017', 'Vĩnh Hưng', '29'),
('P018', 'Tương Mai', '29'),
('P019', 'Định Công', '29'),
('P020', 'Hoàng Liệt', '29'),
('P021', 'Yên Sở', '29'),
('P022', 'Thanh Xuân', '29'),
('P023', 'Khương Đình', '29'),
('P024', 'Phương Liệt', '29'),
('P025', 'Cầu Giấy', '29'),
('P026', 'Nghĩa Đô', '29'),
('P027', 'Yên Hòa', '29'),
('P028', 'Tây Hồ', '29'),
('P029', 'Phú Thượng', '29'),
('P030', 'Tây Tựu', '29'),
('P031', 'Phú Diễn', '29'),
('P032', 'Xuân Đỉnh', '29'),
('P033', 'Đông Ngạc', '29'),
('P034', 'Thượng Cát', '29'),
('P035', 'Từ Liêm', '29'),
('P036', 'Xuân Phương', '29'),
('P037', 'Tây Mỗ', '29'),
('P038', 'Đại Mỗ', '29'),
('P039', 'Long Biên', '29'),
('P040', 'Bồ Đề', '29'),
('P041', 'Việt Hưng', '29'),
('P042', 'Phúc Lợi', '29'),
('P043', 'Hà Đông', '29'),
('P044', 'Dương Nội', '29'),
('P045', 'Yên Nghĩa', '29'),
('P046', 'Phú Lương', '29'),
('P047', 'Kiến Hưng', '29'),
('P048', 'Thanh Liệt', '29'),
('P049', 'Chương Mỹ', '29'),
('P050', 'Sơn Tây', '29'),
('P051', 'Tùng Thiện', '29');

INSERT INTO venue (venue_id, venue_name, address, capacity, ward_id) VALUES
('DD01', 'Nhà hát Lớn Hà Nội', 'Số 1 Tràng Tiền, Hà Nội', 600, 'P001'),
('DD02', 'Cung Văn hóa Hữu nghị Việt Xô', 'Số 91 Trần Hưng Đạo, Hà Nội', 1200, 'P002'),
('DD03', 'Sân vận động Quốc gia Mỹ Đình', 'Số 1 Lê Đức Thọ, Hà Nội', 40000, 'P035'),
('DD04', 'Trung tâm Hội nghị Quốc gia', 'Số 57 Phạm Hùng, Hà Nội', 3800, 'P035'),
('DD05', 'JW Marriott Hanoi', 'Số 8 Đỗ Đức Dục, Hà Nội', 1000, 'P035'),
('DD06', 'Bảo tàng Hà Nội', 'Đường Phạm Hùng, Hà Nội', 2000, 'P035'),
('DD07', 'Hà Nội Creative City', 'Số 1 Lương Yên, Hà Nội', 3000, 'P007'),
('DD08', 'Cung Thể thao Quần Ngựa', 'Đường Văn Cao, Hà Nội', 5000, 'P003'),
('DD09', 'Hoàng thành Thăng Long', 'Số 19C Hoàng Diệu, Hà Nội', 3000, 'P003'),
('DD10', 'Trống Đồng Palace', 'Hà Nội', 1500, 'P001');

INSERT INTO artist (artist_id, artist_name, nationality, image, bio) VALUES
('NS01', 'Sơn Tùng M-TP', 'Việt Nam', '/images/artists/son-tung-mtp.jpg', 'Ca sĩ, nhạc sĩ và nhà sản xuất âm nhạc.'),
('NS02', 'Mỹ Tâm', 'Việt Nam', '/images/artists/my-tam.jpg', 'Ca sĩ và nhạc sĩ nhạc pop Việt Nam.'),
('NS03', 'Hà Anh Tuấn', 'Việt Nam', '/images/artists/ha-anh-tuan.jpg', 'Ca sĩ nổi tiếng với dòng nhạc pop và ballad.'),
('NS04', 'Hòa Minzy', 'Việt Nam', '/images/artists/hoa-minzy.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('NS05', 'Đen Vâu', 'Việt Nam', '/images/artists/den-vau.jpg', 'Rapper và nhạc sĩ Việt Nam.'),
('NS06', 'Soobin', 'Việt Nam', '/images/artists/soobin.jpg', 'Ca sĩ và nghệ sĩ biểu diễn Việt Nam.'),
('NS07', 'Đức Phúc', 'Việt Nam', '/images/artists/duc-phuc.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('NS08', 'Erik', 'Việt Nam', '/images/artists/erik.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('NS09', 'Noo Phước Thịnh', 'Việt Nam', '/images/artists/noo-phuoc-thinh.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('NS10', 'Đông Nhi', 'Việt Nam', '/images/artists/dong-nhi.jpg', 'Ca sĩ và diễn viên Việt Nam.'),
('NS11', 'Bích Phương', 'Việt Nam', '/images/artists/bich-phuong.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('NS12', 'Tóc Tiên', 'Việt Nam', '/images/artists/toc-tien.jpg', 'Ca sĩ nhạc pop và R&B Việt Nam.'),
('NS13', 'Isaac', 'Việt Nam', '/images/artists/isaac.jpg', 'Ca sĩ và diễn viên Việt Nam.'),
('NS14', 'Min', 'Việt Nam', '/images/artists/min.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('NS15', 'Phan Mạnh Quỳnh', 'Việt Nam', '/images/artists/phan-manh-quynh.jpg', 'Ca sĩ và nhạc sĩ sáng tác.'),
('NS16', 'RHYDER', 'Việt Nam', '/images/artists/rhyder.jpg', 'Ca sĩ, rapper và nhà sản xuất âm nhạc.'),
('NS17', 'Binz', 'Việt Nam', '/images/artists/binz.jpg', 'Rapper và nhạc sĩ Việt Nam.'),
('NS18', 'HIEUTHUHAI', 'Việt Nam', '/images/artists/hieuthuhai.jpg', 'Rapper và ca sĩ Việt Nam.'),
('NS19', 'Phương Mỹ Chi', 'Việt Nam', '/images/artists/phuong-my-chi.jpg', 'Ca sĩ Việt Nam.'),
('NS20', 'Thu Minh', 'Việt Nam', '/images/artists/thu-minh.jpg', 'Ca sĩ nhạc pop Việt Nam.');

INSERT INTO `event` (event_id, organizer_id, title, image, `desc`, create_type, `status`) VALUES
('SK01', 'TC001', 'SKY TOUR HÀ NỘI 2026', 'https://dingtea.vn/images/skytour1.png', 'Đại nhạc hội âm nhạc và ánh sáng đỉnh cao của Sơn Tùng M-TP cùng dàn khách mời đặc biệt.', 'YEARLY', 'APPROVED'),
('SK02', 'TC002', 'STORII CONCERT: NGƯỜI GIỮ MÙA XUÂN', '/images/events/storii-concert.svg', 'Đêm nhạc trữ tình sâu lắng cùng Hà Anh Tuấn và các giọng ca hàng đầu Việt Nam.', 'YEARLY', 'APPROVED'),
('SK03', 'TC003', 'KOSMIK LIVE CONCERT', '/images/events/kosmik.svg', 'Bữa tiệc Hip-hop & R&B bùng nổ kỷ niệm chặng đường âm nhạc của SpaceSpeakers.', 'YEARLY', 'APPROVED'),
('SK04', 'TC004', 'ĐÊM NHẠC PHÒNG TRÀ: KHÚC TÌNH THU', '/images/events/khuc-tinh-thu.svg', 'Không gian âm nhạc acoustic ấm cúng tại Nhà hát Lớn Hà Nội.', 'MONTHLY', 'APPROVED'),
('SK05', 'TC002', 'GENFEST: CỔNG ÂM NHẠC ĐA GIÁC QUAN', '/images/events/genfest.svg', 'Lễ hội âm nhạc dành cho thế hệ trẻ với sự góp mặt của các nghệ sĩ GenZ đình đám.', 'YEARLY', 'PENDING'),
('SK06', 'TC004', 'GALA HÀI KỊCH CUỐI TUẦN', '/images/events/gala-hai.svg', 'Chương trình biểu diễn nghệ thuật tạp kỹ hàng tuần.', 'WEEKLY', 'REJECTED');

INSERT INTO event_schedule (schedule_id, event_id, venue_id, start_datetime, end_datetime, `status`) VALUES
('SCH001', 'SK04', 'DD01', '2026-10-15 20:00:00', '2026-10-15 22:30:00', 'Đã kết thúc'),
('SCH002', 'SK01', 'DD03', '2026-12-20 19:30:00', '2026-12-20 23:00:00', 'Đang mở bán'),
('SCH003', 'SK02', 'DD04', '2026-12-12 20:00:00', '2026-12-12 23:00:00', 'Đang mở bán'),
('SCH004', 'SK02', 'DD04', '2026-12-13 20:00:00', '2026-12-13 23:00:00', 'Đang mở bán'),
('SCH005', 'SK03', 'DD08', '2027-01-16 19:00:00', '2027-01-16 23:30:00', 'Mở đặt vé sớm'),
('SCH006', 'SK04', 'DD02', '2026-11-25 20:00:00', '2026-11-25 22:30:00', 'Sắp diễn ra');

INSERT INTO schedule_artist (artist_id, schedule_id, `role`) VALUES
('NS03', 'SCH001', 'Ca sĩ chính'),
('NS15', 'SCH001', 'Khách mời'),
('NS01', 'SCH002', 'Nghệ sĩ chính (Headliner)'),
('NS16', 'SCH002', 'Khách mời đặc biệt'),
('NS14', 'SCH002', 'Khách mời'),
('NS03', 'SCH003', 'Nghệ sĩ chính'),
('NS02', 'SCH003', 'Khách mời đặc biệt'),
('NS04', 'SCH003', 'Khách mời'),
('NS03', 'SCH004', 'Nghệ sĩ chính'),
('NS20', 'SCH004', 'Khách mời đặc biệt'),
('NS11', 'SCH004', 'Khách mời'),
('NS06', 'SCH005', 'Nghệ sĩ chính'),
('NS17', 'SCH005', 'Nghệ sĩ chính'),
('NS12', 'SCH005', 'Khách mời đặc biệt'),
('NS18', 'SCH005', 'Khách mời'),
('NS07', 'SCH006', 'Ca sĩ chính'),
('NS08', 'SCH006', 'Ca sĩ chính'),
('NS19', 'SCH006', 'Khách mời');

INSERT INTO ticket (ticket_id, schedule_id, ticket_name, price, `desc`, total_quantity, remaining_quantity) VALUES
('TK001', 'SCH001', 'Hạng Phổ Thông', 800000, 'Ghế ngồi tầng 2 Nhà hát Lớn.', 300, 120),
('TK002', 'SCH001', 'Hạng VIP', 1500000, 'Ghế ngồi tầng 1 trung tâm, tặng kèm nước uống.', 200, 45),
('TK003', 'SCH002', 'GA - Khu đứng phổ thông', 900000, 'Khu vực đứng tự do phía sau, bao gồm lightstick tiêu chuẩn.', 15000, 14995),
('TK004', 'SCH002', 'Fanzone - Sát sân khấu', 2200000, 'Khu vực đứng gần sân khấu chính, ưu tiên check-in sớm.', 5000, 4997),
('TK005', 'SCH002', 'SVIP - Khán đài trung tâm', 4500000, 'Ghế ngồi tầm nhìn đẹp nhất, tặng bộ quà tặng độc quyền + Soundcheck.', 1000, 998),
('TK006', 'SCH003', 'Standard - Hạng Tiêu Chuẩn', 1200000, 'Ghế ngồi khu vực cánh trái/phải.', 1500, 1495),
('TK007', 'SCH003', 'VIP - Hạng Vàng', 2800000, 'Ghế ngồi khu vực trung tâm.', 1000, 998),
('TK008', 'SCH003', 'VVIP - Hạng Kim Cương', 4200000, 'Hàng ghế đầu tiên, tặng ấn phẩm lưu niệm có chữ ký.', 300, 300),
('TK009', 'SCH004', 'Standard - Hạng Tiêu Chuẩn', 1200000, 'Ghế ngồi khu vực cánh trái/phải.', 1500, 1500),
('TK010', 'SCH004', 'VIP - Hạng Vàng', 2800000, 'Ghế ngồi khu vực trung tâm.', 1000, 999),
('TK011', 'SCH005', 'Early Bird GA', 750000, 'Vé mở bán sớm số lượng giới hạn.', 1000, 996),
('TK012', 'SCH005', 'Early Bird VIP', 1800000, 'Vé VIP mở bán sớm kèm áo thun sự kiện.', 500, 500),
('TK013', 'SCH006', 'Vé Đồng Hạng', 1000000, 'Ghế ngồi tự do theo khu vực.', 1000, 0);

INSERT INTO `order` (order_id, customer_id, created_at, total_amount, `status`, payment_method, payment_at) VALUES
('DH001', 'KH001', '2026-11-10 10:15:00', 4000000, 'Đã thanh toán', 'VNPay', '2026-11-10 10:18:30'),
('DH002', 'KH001', '2026-11-11 14:20:00', 9000000, 'Đã thanh toán', 'Chuyển khoản', '2026-11-11 14:22:10'),
('DH003', 'KH001', '2026-11-12 09:00:00', 8000000, 'Đã thanh toán', 'Ví điện tử', '2026-11-12 09:01:45'),
('DH004', 'KH001', '2026-11-23 21:10:00', 7100000, 'Chờ thanh toán', NULL, NULL),
('DH005', 'KH001', '2026-11-15 16:00:00', 2800000, 'Đã hủy', NULL, NULL),
('DH006', 'KH001', '2026-10-01 08:30:00', 3000000, 'Đã hoàn tiền', 'Thẻ', '2026-10-01 08:31:15'),
('DH007', 'KH001', '2026-11-20 19:45:00', 3000000, 'Đã thanh toán', 'Ví điện tử', '2026-11-20 19:46:05'),
('DH008', 'KH001', '2026-11-21 11:30:00', 5200000, 'Đã thanh toán', 'Chuyển khoản', '2026-11-21 11:33:00');

INSERT INTO order_detail (order_id, ticket_id, quantity, price) VALUES
('DH001', 'TK003', 2, 900000),
('DH001', 'TK004', 1, 2200000),
('DH002', 'TK005', 2, 4500000),
('DH003', 'TK006', 2, 1200000),
('DH003', 'TK007', 2, 2800000),
('DH004', 'TK003', 3, 900000),
('DH004', 'TK004', 2, 2200000),
('DH005', 'TK007', 1, 2800000),
('DH006', 'TK002', 2, 1500000),
('DH007', 'TK011', 4, 750000),
('DH008', 'TK006', 2, 1200000),
('DH008', 'TK010', 1, 2800000);

-- Đơn mẫu đang chờ Admin duyệt hoàn tiền (khách KH001 đã gửi yêu cầu)
INSERT INTO `order` (order_id, customer_id, created_at, total_amount, `status`, payment_method, payment_at, refund_reason, refund_requested_at) VALUES
('DH009', 'KH001', '2026-11-22 10:00:00', 1200000, 'Chờ hoàn tiền', 'Ví điện tử', '2026-11-22 10:02:00', 'Tôi có việc đột xuất nên không đi được.', '2026-11-24 09:00:00');

INSERT INTO order_detail (order_id, ticket_id, quantity, price) VALUES
('DH009', 'TK006', 1, 1200000);
