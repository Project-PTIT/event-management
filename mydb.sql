CREATE DATABASE event_tickets;
USE event_tickets;

CREATE TABLE people (
    id 				VARCHAR(20) PRIMARY KEY,
    `name` 			VARCHAR(1000) NOT NULL,
    email 			VARCHAR(200) NOT NULL UNIQUE,
    phone_number 	VARCHAR(15) NOT NULL,
    `password` 		VARCHAR(255) NOT NULL
);

CREATE TABLE `admin` (
    admin_id 		VARCHAR(20) PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES people(id)
);

CREATE TABLE organizer (
    organizer_id 	VARCHAR(20) PRIMARY KEY,
    tax_code 		VARCHAR(100) NOT NULL UNIQUE,
    address 		VARCHAR(200) NOT NULL,
    FOREIGN KEY (organizer_id) REFERENCES people(id)
);

CREATE TABLE customer (
    customer_id 	VARCHAR(20) PRIMARY KEY,
    gender 			VARCHAR(10) NOT NULL,
    date_of_birth 	DATE NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES people(id),
    CHECK (gender IN ('Male', 'Female', 'Other'))
);

CREATE TABLE province (
    province_id 		VARCHAR(20) PRIMARY KEY,
    province_name 	VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE ward (
    ward_id 		VARCHAR(20) PRIMARY KEY,
    ward_name		VARCHAR(100) NOT NULL,
    province_id 		VARCHAR(20) NOT NULL,
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
    event_id 		VARCHAR(20) PRIMARY KEY,
    organizer_id 	VARCHAR(20) NOT NULL,
    title 			VARCHAR(1000) NOT NULL,
    image 			VARCHAR(1000) NOT NULL,
    `desc` 			VARCHAR(1000) NOT NULL,
    create_type 	VARCHAR(100),
    `status` 		VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (organizer_id) REFERENCES organizer(organizer_id),
	CHECK (`status` IN ('PENDING', 'APPROVED', 'REJECTED')),
    CHECK (create_type IN ('WEEKLY', 'MONTHLY', 'YEARLY')));

CREATE TABLE event_schedule (
    schedule_id 	VARCHAR(20) PRIMARY KEY,
    event_id 		VARCHAR(20) NOT NULL,
    venue_id 		VARCHAR(20) NOT NULL,
    start_datetime 	DATETIME NOT NULL,
    end_datetime 	DATETIME NOT NULL,
    `status` 		VARCHAR(50) NOT NULL,
    FOREIGN KEY (event_id) REFERENCES `event`(event_id),
    FOREIGN KEY (venue_id) REFERENCES venue(venue_id),
    CHECK (end_datetime > start_datetime),
    CHECK (`status` IN ('Mở đặt vé sớm', 'Đang mở bán', 'Sắp diễn ra', 'Đang diễn ra', 'Đã kết thúc'))
);

CREATE TABLE artist (
    artist_id 		VARCHAR(20) PRIMARY KEY,
    artist_name 	VARCHAR(200) NOT NULL,
    nationality 	VARCHAR(100) NOT NULL,
    image 			VARCHAR(1000) NOT NULL,
    bio 			VARCHAR(1000)
);

CREATE TABLE schedule_artist (
    artist_id 		VARCHAR(20) NOT NULL,
    schedule_id 	VARCHAR(20) NOT NULL,
    `role` 			VARCHAR(100) NOT NULL,
    PRIMARY KEY (artist_id, schedule_id),
    FOREIGN KEY (artist_id) REFERENCES artist(artist_id),
    FOREIGN KEY (schedule_id) REFERENCES event_schedule(schedule_id)
);

CREATE TABLE ticket (
    ticket_id 			VARCHAR(20) PRIMARY KEY,
    schedule_id 		VARCHAR(20) NOT NULL,
    ticket_name 		VARCHAR(200) NOT NULL,
    price 				DECIMAL(15,0) NOT NULL,
    `desc`				VARCHAR(2000) NOT NULL,
    total_quantity 		INT NOT NULL,
    remaining_quantity 	INT NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES event_schedule(schedule_id),
    CHECK (price >= 0),
    UNIQUE (schedule_id, ticket_name),
    CHECK (total_quantity >= 0),
    CHECK (
        remaining_quantity >= 0
        AND remaining_quantity <= total_quantity
    )
);

CREATE TABLE `order` (
    order_id 		VARCHAR(20) PRIMARY KEY,
    customer_id 	VARCHAR(20) NOT NULL,
    created_at 		DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount 	DECIMAL(15,0) NOT NULL,
    `status` 		VARCHAR(100) NOT NULL,
    payment_method 	VARCHAR(500),
    payment_at 		DATETIME,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CHECK (total_amount > 0),
    CHECK (payment_at IS NULL OR payment_at >= created_at),
    CHECK (`status` IN ('Chờ thanh toán', 'Đã thanh toán', 'Đã hủy', 'Đã hoàn tiền')),
    CHECK (
        (`status` IN ('Đã thanh toán', 'Đã hoàn tiền') AND payment_at IS NOT NULL)
        OR (`status` IN ('Chờ thanh toán', 'Đã hủy') AND payment_at IS NULL)
    )
);

CREATE TABLE order_detail (
    order_id 		VARCHAR(20) NOT NULL,
    ticket_id 		VARCHAR(20) NOT NULL,
    quantity 		INT NOT NULL,
    price 			DECIMAL(15,0) NOT NULL,
    PRIMARY KEY (order_id, ticket_id),
    FOREIGN KEY (order_id) REFERENCES `order`(order_id),
    FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id),
    CHECK (quantity > 0),
    CHECK (price >= 0)
);

INSERT INTO people (id, `name`, email, phone_number, `password`) VALUES
('AD01', 'Bùi Ngọc Hải', 'admin01@event.vn', '0901000001', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK'),
('AD02', 'Phạm Hoàng Hiệp', 'admin02@event.vn', '0901000002', '$2b$12$fHd7KgQAy6YPgvHbmtaIEONbmRAoplerJ33TwvNWkC2QwwcaNhOJK');

INSERT INTO `admin` (admin_id) VALUES
('AD01'),
('AD02');

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
('AR01', 'Sơn Tùng M-TP', 'Việt Nam', '/images/artists/son-tung-mtp.jpg', 'Ca sĩ, nhạc sĩ và nhà sản xuất âm nhạc.'),
('AR02', 'Mỹ Tâm', 'Việt Nam', '/images/artists/my-tam.jpg', 'Ca sĩ và nhạc sĩ nhạc pop Việt Nam.'),
('AR03', 'Hà Anh Tuấn', 'Việt Nam', '/images/artists/ha-anh-tuan.jpg', 'Ca sĩ nổi tiếng với dòng nhạc pop và ballad.'),
('AR04', 'Hòa Minzy', 'Việt Nam', '/images/artists/hoa-minzy.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('AR05', 'Đen Vâu', 'Việt Nam', '/images/artists/den-vau.jpg', 'Rapper và nhạc sĩ Việt Nam.'),
('AR06', 'Soobin', 'Việt Nam', '/images/artists/soobin.jpg', 'Ca sĩ và nghệ sĩ biểu diễn Việt Nam.'),
('AR07', 'Đức Phúc', 'Việt Nam', '/images/artists/duc-phuc.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('AR08', 'Erik', 'Việt Nam', '/images/artists/erik.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('AR09', 'Noo Phước Thịnh', 'Việt Nam', '/images/artists/noo-phuoc-thinh.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('AR10', 'Đông Nhi', 'Việt Nam', '/images/artists/dong-nhi.jpg', 'Ca sĩ và diễn viên Việt Nam.'),
('AR11', 'Bích Phương', 'Việt Nam', '/images/artists/bich-phuong.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('AR12', 'Tóc Tiên', 'Việt Nam', '/images/artists/toc-tien.jpg', 'Ca sĩ nhạc pop và R&B Việt Nam.'),
('AR13', 'Isaac', 'Việt Nam', '/images/artists/isaac.jpg', 'Ca sĩ và diễn viên Việt Nam.'),
('AR14', 'Min', 'Việt Nam', '/images/artists/min.jpg', 'Ca sĩ nhạc pop Việt Nam.'),
('AR15', 'Phan Mạnh Quỳnh', 'Việt Nam', '/images/artists/phan-manh-quynh.jpg', 'Ca sĩ và nhạc sĩ sáng tác.'),
('AR16', 'RHYDER', 'Việt Nam', '/images/artists/rhyder.jpg', 'Ca sĩ, rapper và nhà sản xuất âm nhạc.'),
('AR17', 'Binz', 'Việt Nam', '/images/artists/binz.jpg', 'Rapper và nhạc sĩ Việt Nam.'),
('AR18', 'HIEUTHUHAI', 'Việt Nam', '/images/artists/hieuthuhai.jpg', 'Rapper và ca sĩ Việt Nam.'),
('AR19', 'Phương Mỹ Chi', 'Việt Nam', '/images/artists/phuong-my-chi.jpg', 'Ca sĩ Việt Nam.'),
('AR20', 'Thu Minh', 'Việt Nam', '/images/artists/thu-minh.jpg', 'Ca sĩ nhạc pop Việt Nam.');