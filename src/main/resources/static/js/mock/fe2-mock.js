// FE2 Mock Data & Mock API Dispatcher
// Chứa dữ liệu mẫu chuẩn theo Entity backend và hỗ trợ chạy độc lập không phụ thuộc backend

const FE2_MOCK_DATA = {
  wards: [
    { wardId: 'P001', wardName: 'Hoàn Kiếm', provinceName: 'Hà Nội' },
    { wardId: 'P002', wardName: 'Cửa Nam', provinceName: 'Hà Nội' },
    { wardId: 'P003', wardName: 'Ba Đình', provinceName: 'Hà Nội' },
    { wardId: 'P007', wardName: 'Vĩnh Tuy', provinceName: 'Hà Nội' },
    { wardId: 'P025', wardName: 'Cầu Giấy', provinceName: 'Hà Nội' },
    { wardId: 'P035', wardName: 'Từ Liêm', provinceName: 'Hà Nội' }
  ],

  venues: [
    { venueId: 'DD01', venueName: 'Nhà hát Lớn Hà Nội', address: 'Số 1 Tràng Tiền, Hoàn Kiếm, Hà Nội', capacity: 600, wardId: 'P001' },
    { venueId: 'DD02', venueName: 'Cung Văn hóa Hữu nghị Việt Xô', address: 'Số 91 Trần Hưng Đạo, Hà Nội', capacity: 1200, wardId: 'P002' },
    { venueId: 'DD03', venueName: 'Sân vận động Quốc gia Mỹ Đình', address: 'Số 1 Lê Đức Thọ, Từ Liêm, Hà Nội', capacity: 40000, wardId: 'P035' },
    { venueId: 'DD06', venueName: 'Bảo tàng Hà Nội', address: 'Đường Phạm Hùng, Hà Nội', capacity: 2000, wardId: 'P035' },
    { venueId: 'DD07', venueName: 'Hà Nội Creative City', address: 'Số 1 Lương Yên, Vĩnh Tuy, Hà Nội', capacity: 3000, wardId: 'P007' },
    { venueId: 'DD08', venueName: 'Cung Thể thao Quần Ngựa', address: 'Đường Văn Cao, Ba Đình, Hà Nội', capacity: 5000, wardId: 'P003' },
    { venueId: 'DD09', venueName: 'Hoàng thành Thăng Long', address: 'Số 19C Hoàng Diệu, Ba Đình, Hà Nội', capacity: 3000, wardId: 'P003' }
  ],

  artists: [
    {
      artistId: 'AR01',
      artistName: 'Sơn Tùng M-TP',
      nationality: 'Việt Nam',
      image: '/images/artists/son-tung-mtp.jpg',
      bio: 'Ca sĩ, nhạc sĩ kiêm nhà sản xuất âm nhạc hàng đầu Việt Nam với loạt bản hit quốc tế.'
    },
    {
      artistId: 'AR02',
      artistName: 'Mỹ Tâm',
      nationality: 'Việt Nam',
      image: '/images/artists/my-tam.jpg',
      bio: 'Biểu tượng nhạc pop Việt Nam với sự nghiệp âm nhạc rực rỡ và các liveshow quy mô sân vận động.'
    },
    {
      artistId: 'AR03',
      artistName: 'Hà Anh Tuấn',
      nationality: 'Việt Nam',
      image: '/images/artists/ha-anh-tuan.jpg',
      bio: 'Nam ca sĩ truyền cảm hứng với chuỗi concert See Sing Share cháy vé trong vài phút.'
    },
    {
      artistId: 'AR04',
      artistName: 'Hòa Minzy',
      nationality: 'Việt Nam',
      image: '/images/artists/hoa-minzy.jpg',
      bio: 'Giọng ca nội lực, giàu cảm xúc với nhiều bản hit ballad và phong cách biểu diễn cuốn hút.'
    },
    {
      artistId: 'AR05',
      artistName: 'Đen Vâu',
      nationality: 'Việt Nam',
      image: '/images/artists/den-vau.jpg',
      bio: 'Rapper và nhạc sĩ truyền cảm hứng mộc mạc, ý nghĩa, sở hữu lượng fan đông đảo nhất nhì V-Pop.'
    },
    {
      artistId: 'AR06',
      artistName: 'Soobin',
      nationality: 'Việt Nam',
      image: '/images/artists/soobin.jpg',
      bio: 'Nghệ sĩ đa tài có khả năng sáng tác, chơi nhiều nhạc cụ và trình diễn R&B, Pop xuất sắc.'
    },
    {
      artistId: 'AR07',
      artistName: 'Đức Phúc',
      nationality: 'Việt Nam',
      image: '/images/artists/duc-phuc.jpg',
      bio: 'Hoàng tử nhạc Valentine với giọng hát ấm áp và những bản tình ca lãng mạn.'
    },
    {
      artistId: 'AR08',
      artistName: 'Erik',
      nationality: 'Việt Nam',
      image: '/images/artists/erik.jpg',
      bio: 'Ca sĩ nhạc pop với chất giọng tenor đặc trưng và vũ đạo điêu luyện.'
    },
    {
      artistId: 'AR09',
      artistName: 'Noo Phước Thịnh',
      nationality: 'Việt Nam',
      image: '/images/artists/noo-phuoc-thinh.jpg',
      bio: 'Ngôi sao giải trí đa năng, phong cách lịch lãm cùng giọng hát truyền cảm.'
    },
    {
      artistId: 'AR10',
      artistName: 'Đông Nhi',
      nationality: 'Việt Nam',
      image: '/images/artists/dong-nhi.jpg',
      bio: 'Nữ ca sĩ tiên phong của dòng nhạc pop hiện đại với các đêm nhạc hoành tráng.'
    },
    {
      artistId: 'AR11',
      artistName: 'Bích Phương',
      nationality: 'Việt Nam',
      image: '/images/artists/bich-phuong.jpg',
      bio: 'Nữ ca sĩ biến hóa phong cách đa dạng từ ballad u sầu đến nhạc điện tử dân gian lôi cuốn.'
    },
    {
      artistId: 'AR12',
      artistName: 'Tóc Tiên',
      nationality: 'Việt Nam',
      image: '/images/artists/toc-tien.jpg',
      bio: 'Nghệ sĩ biểu diễn bùng nổ, hiện đại với phong cách thời trang và âm nhạc cá tính.'
    },
    {
      artistId: 'AR13',
      artistName: 'Isaac',
      nationality: 'Việt Nam',
      image: '/images/artists/isaac.jpg',
      bio: 'Nam ca sĩ, diễn viên tài năng với ngoại hình điển trai và vũ đạo đỉnh cao.'
    },
    {
      artistId: 'AR14',
      artistName: 'Min',
      nationality: 'Việt Nam',
      image: '/images/artists/min.jpg',
      bio: 'Nữ ca sĩ với gu âm nhạc văn minh, hiện đại và hàng loạt MV triệu view.'
    },
    {
      artistId: 'AR15',
      artistName: 'Phan Mạnh Quỳnh',
      nationality: 'Việt Nam',
      image: '/images/artists/phan-manh-quynh.jpg',
      bio: 'Nhạc sĩ, ca sĩ với tư duy âm nhạc tự sự sâu sắc, giai điệu da diết chạm đến trái tim.'
    },
    {
      artistId: 'AR16',
      artistName: 'RHYDER',
      nationality: 'Việt Nam',
      image: '/images/artists/rhyder.jpg',
      bio: 'Nghệ sĩ trẻ đa năng với khả năng sáng tác, hát, rap và sản xuất âm nhạc đột phá.'
    },
    {
      artistId: 'AR17',
      artistName: 'Binz',
      nationality: 'Việt Nam',
      image: '/images/artists/binz.jpg',
      bio: 'Nhà thơ của rap Việt với flow đặc trưng và phong cách biểu diễn phóng khoáng.'
    },
    {
      artistId: 'AR18',
      artistName: 'HIEUTHUHAI',
      nationality: 'Việt Nam',
      image: '/images/artists/hieuthuhai.jpg',
      bio: 'Rapper thế hệ mới sở hữu visual ấn tượng và các bản hit trendy càn quét bảng xếp hạng.'
    },
    {
      artistId: 'AR19',
      artistName: 'Phương Mỹ Chi',
      nationality: 'Việt Nam',
      image: '/images/artists/phuong-my-chi.jpg',
      bio: 'Giọng ca trẻ nổi bật với sự kết hợp tuyệt vời giữa âm hưởng dân ca và nhạc pop thời thượng.'
    },
    {
      artistId: 'AR20',
      artistName: 'Thu Minh',
      nationality: 'Việt Nam',
      image: '/images/artists/thu-minh.jpg',
      bio: 'Diva của làng nhạc Việt với giọng hát khủng và các bản nhạc dance bùng nổ năng lượng.'
    }
  ],

  events: [
    {
      eventId: 'SK01',
      title: 'Đại nhạc hội EDM 2026',
      image: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80',
      desc: 'Đêm nhạc điện tử quy mô lớn nhất năm quy tụ dàn DJ đình đám quốc tế cùng các nghệ sĩ top đầu Việt Nam. Hệ thống âm thanh, ánh sáng chuẩn quốc tế.',
      createType: 'YEARLY',
      status: 'APPROVED',
      organizer: { organizerId: 'TC002', name: 'Empire Entertainment', address: 'Quận 1, TP. HCM' },
      ward: { wardId: 'P003', wardName: 'Ba Đình', province: { provinceId: '29', name: 'Hà Nội' } },
      minPrice: 800000,
      featured: true
    },
    {
      eventId: 'SK04',
      title: 'Concert Những Bài Ca Không Quên',
      image: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&auto=format&fit=crop&q=80',
      desc: 'Đêm nhạc trữ tình hoài niệm đưa khán giả trở về với những giai điệu bất hủ qua sự thể hiện của các ca sĩ gạo cội và những giọng ca hàng đầu V-Pop.',
      createType: 'YEARLY',
      status: 'APPROVED',
      organizer: { organizerId: 'TC003', name: 'V-Music Production', address: 'Hoàn Kiếm, Hà Nội' },
      ward: { wardId: 'P003', wardName: 'Ba Đình', province: { provinceId: '29', name: 'Hà Nội' } },
      minPrice: 400000,
      featured: true
    },
    {
      eventId: 'SK07',
      title: 'Hanoi Youth Music Festival',
      image: 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=800&auto=format&fit=crop&q=80',
      desc: 'Lễ hội âm nhạc dành riêng cho giới trẻ với sự góp mặt của dàn nghệ sĩ Gen Z và các thần tượng hàng đầu: Sơn Tùng M-TP, Đen Vâu, Binz.',
      createType: 'MONTHLY',
      status: 'APPROVED',
      organizer: { organizerId: 'TC006', name: 'Youth Media Group', address: 'Cầu Giấy, Hà Nội' },
      ward: { wardId: 'P003', wardName: 'Ba Đình', province: { provinceId: '29', name: 'Hà Nội' } },
      minPrice: 600000,
      featured: true
    },
    {
      eventId: 'SK02',
      title: 'Triển lãm Mỹ thuật Đương đại 2026',
      image: 'https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?w=800&auto=format&fit=crop&q=80',
      desc: 'Không gian trưng bày nghệ thuật thị giác đương đại với hơn 100 tác phẩm tranh, tượng điêu khắc và nghệ thuật sắp đặt độc bản.',
      createType: 'YEARLY',
      status: 'APPROVED',
      organizer: { organizerId: 'TC001', name: 'Art Space Vietnam', address: 'Tây Hồ, Hà Nội' },
      ward: { wardId: 'P002', wardName: 'Cửa Nam', province: { provinceId: '29', name: 'Hà Nội' } },
      minPrice: 300000,
      featured: false
    },
    {
      eventId: 'SK06',
      title: 'Vietnam Startup Summit 2026',
      image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop&q=80',
      desc: 'Diễn đàn công nghệ và khởi nghiệp quy mô 40.000 người kết nối các quỹ đầu tư mạo hiểm, kỳ lân công nghệ và các nhà sáng lập tiềm năng.',
      createType: null,
      status: 'APPROVED',
      organizer: { organizerId: 'TC005', name: 'Innovate Vietnam Corp', address: 'Nam Từ Liêm, Hà Nội' },
      ward: { wardId: 'P035', wardName: 'Từ Liêm', province: { provinceId: '29', name: 'Hà Nội' } },
      minPrice: 700000,
      featured: false
    },
    {
      eventId: 'SK10',
      title: 'Live Show Giai Điệu Mùa Thu',
      image: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80',
      desc: 'Đêm nhạc acoustic lãng mạn giữa lòng Hà Nội với sự tham gia của Mỹ Tâm và Phan Mạnh Quỳnh trong không gian nghệ thuật ngoài trời.',
      createType: 'YEARLY',
      status: 'APPROVED',
      organizer: { organizerId: 'TC003', name: 'V-Music Production', address: 'Hoàn Kiếm, Hà Nội' },
      ward: { wardId: 'P007', wardName: 'Vĩnh Tuy', province: { provinceId: '29', name: 'Hà Nội' } },
      minPrice: 500000,
      featured: false
    }
  ],

  schedules: [
    {
      scheduleId: 'SCH01',
      eventId: 'SK01',
      venue: { venueId: 'DD08', venueName: 'Cung Thể thao Quần Ngựa', address: 'Đường Văn Cao, Ba Đình, Hà Nội' },
      startDatetime: '2026-11-15T19:00:00',
      endDatetime: '2026-11-15T23:30:00',
      status: 'Sắp diễn ra',
      artists: [
        { artistId: 'AR17', artistName: 'Binz', role: 'Rapper biểu diễn chính', image: '/images/artists/binz.jpg' },
        { artistId: 'AR18', artistName: 'HIEUTHUHAI', role: 'Khách mời đặc biệt', image: '/images/artists/hieuthuhai.jpg' },
        { artistId: 'AR16', artistName: 'RHYDER', role: 'DJ & Performer', image: '/images/artists/rhyder.jpg' }
      ]
    },
    {
      scheduleId: 'SCH04',
      eventId: 'SK04',
      venue: { venueId: 'DD09', venueName: 'Hoàng thành Thăng Long', address: 'Số 19C Hoàng Diệu, Ba Đình, Hà Nội' },
      startDatetime: '2026-12-20T20:00:00',
      endDatetime: '2026-12-20T22:30:00',
      status: 'Đang mở bán',
      artists: [
        { artistId: 'AR02', artistName: 'Mỹ Tâm', role: 'Ca sĩ chính', image: '/images/artists/my-tam.jpg' },
        { artistId: 'AR03', artistName: 'Hà Anh Tuấn', role: 'Ca sĩ khách mời', image: '/images/artists/ha-anh-tuan.jpg' },
        { artistId: 'AR15', artistName: 'Phan Mạnh Quỳnh', role: 'Nhạc sĩ / Ca sĩ', image: '/images/artists/phan-manh-quynh.jpg' }
      ]
    },
    {
      scheduleId: 'SCH09',
      eventId: 'SK07',
      venue: { venueId: 'DD08', venueName: 'Cung Thể thao Quần Ngựa', address: 'Đường Văn Cao, Ba Đình, Hà Nội' },
      startDatetime: '2026-11-25T16:00:00',
      endDatetime: '2026-11-25T23:00:00',
      status: 'Đang mở bán',
      artists: [
        { artistId: 'AR01', artistName: 'Sơn Tùng M-TP', role: 'Headliner', image: '/images/artists/son-tung-mtp.jpg' },
        { artistId: 'AR05', artistName: 'Đen Vâu', role: 'Special Guest', image: '/images/artists/den-vau.jpg' },
        { artistId: 'AR06', artistName: 'Soobin', role: 'Main Artist', image: '/images/artists/soobin.jpg' }
      ]
    },
    {
      scheduleId: 'SCH02',
      eventId: 'SK02',
      venue: { venueId: 'DD02', venueName: 'Cung Văn hóa Hữu nghị Việt Xô', address: 'Số 91 Trần Hưng Đạo, Hà Nội' },
      startDatetime: '2026-11-10T09:00:00',
      endDatetime: '2026-11-10T20:00:00',
      status: 'Sắp diễn ra',
      artists: [
        { artistId: 'AR04', artistName: 'Hòa Minzy', role: 'Nghệ sĩ đại diện', image: '/images/artists/hoa-minzy.jpg' }
      ]
    },
    {
      scheduleId: 'SCH07',
      eventId: 'SK06',
      venue: { venueId: 'DD03', venueName: 'Sân vận động Quốc gia Mỹ Đình', address: 'Số 1 Lê Đức Thọ, Từ Liêm, Hà Nội' },
      startDatetime: '2026-11-05T08:00:00',
      endDatetime: '2026-11-05T18:00:00',
      status: 'Sắp diễn ra',
      artists: [
        { artistId: 'AR01', artistName: 'Sơn Tùng M-TP', role: 'Diễn giả khách mời', image: '/images/artists/son-tung-mtp.jpg' }
      ]
    },
    {
      scheduleId: 'SCH12',
      eventId: 'SK10',
      venue: { venueId: 'DD07', venueName: 'Hà Nội Creative City', address: 'Số 1 Lương Yên, Vĩnh Tuy, Hà Nội' },
      startDatetime: '2026-11-28T20:00:00',
      endDatetime: '2026-11-28T22:30:00',
      status: 'Sắp diễn ra',
      artists: [
        { artistId: 'AR02', artistName: 'Mỹ Tâm', role: 'Ca sĩ chính', image: '/images/artists/my-tam.jpg' },
        { artistId: 'AR15', artistName: 'Phan Mạnh Quỳnh', role: 'Khách mời', image: '/images/artists/phan-manh-quynh.jpg' }
      ]
    }
  ],

  tickets: {
    SCH01: [
      { ticketId: 'V001', ticketName: 'VVIP Standing', price: 5000000, desc: 'Khu vực sát sân khấu, tặng áo đấu và photocard độc quyền', totalQuantity: 500, remainingQuantity: 280 },
      { ticketId: 'V002', ticketName: 'VIP Zone A', price: 2500000, desc: 'Khu vực ưu tiên tầm nhìn trung tâm cực đẹp', totalQuantity: 1500, remainingQuantity: 950 },
      { ticketId: 'V003', ticketName: 'GA - Phổ Thông', price: 800000, desc: 'Khu vực khán giả tự do, hòa mình vào âm nhạc sôi động', totalQuantity: 5000, remainingQuantity: 3120 }
    ],
    SCH04: [
      { ticketId: 'V008', ticketName: 'Hạng Kim Cương', price: 3500000, desc: 'Hàng ghế đầu chính diện sân khấu Hoàng thành, tiệc trà nhẹ', totalQuantity: 100, remainingQuantity: 35 },
      { ticketId: 'V009', ticketName: 'Hạng Vàng', price: 1800000, desc: 'Khu vực khán đài trung tâm, âm thanh chân thực', totalQuantity: 1500, remainingQuantity: 620 },
      { ticketId: 'V010', ticketName: 'Hạng Tiêu Chuẩn', price: 400000, desc: 'Khu vực khán đài phía sau', totalQuantity: 3000, remainingQuantity: 1540 }
    ],
    SCH09: [
      { ticketId: 'V020', ticketName: 'VIP Fanzone', price: 1800000, desc: 'Khu vực cận sân khấu giao lưu thần tượng', totalQuantity: 1000, remainingQuantity: 450 },
      { ticketId: 'V021', ticketName: 'Standard GA', price: 600000, desc: 'Vé tham gia không gian lễ hội âm nhạc', totalQuantity: 5000, remainingQuantity: 2800 }
    ],
    SCH02: [
      { ticketId: 'V004', ticketName: 'VIP All Access', price: 1500000, desc: 'Tham quan toàn bộ khu vực triển lãm và catalogue nghệ thuật', totalQuantity: 500, remainingQuantity: 320 },
      { ticketId: 'V005', ticketName: 'Standard', price: 300000, desc: 'Vé tham quan tiêu chuẩn', totalQuantity: 700, remainingQuantity: 410 }
    ],
    SCH07: [
      { ticketId: 'V016', ticketName: 'VIP Delegate', price: 2500000, desc: 'Tham dự trọn gói hội nghị và tiệc kết nối doanh nghiệp', totalQuantity: 500, remainingQuantity: 210 },
      { ticketId: 'V017', ticketName: 'Standard Pass', price: 700000, desc: 'Vé tham dự các phiên hội thảo', totalQuantity: 2000, remainingQuantity: 1350 }
    ],
    SCH12: [
      { ticketId: 'V022', ticketName: 'VIP Giai Điệu', price: 1500000, desc: 'Ghế ngồi gần sân khấu, tặng nước uống', totalQuantity: 300, remainingQuantity: 120 },
      { ticketId: 'V023', ticketName: 'Standard', price: 500000, desc: 'Khu vực tự do thưởng thức âm nhạc', totalQuantity: 1000, remainingQuantity: 450 }
    ]
  }
};

// FE2 Mock API Handler
const FE2_Mock = {
  getEvents(filters = {}) {
    let list = FE2_MOCK_DATA.events.filter(e => e.status === 'APPROVED');

    if (filters.keyword) {
      const kw = filters.keyword.toLowerCase().trim();
      list = list.filter(e => e.title.toLowerCase().includes(kw) || e.desc.toLowerCase().includes(kw));
    }
    if (filters.createType) {
      if (filters.createType === 'NONE') {
        list = list.filter(e => e.createType === null || e.createType === '');
      } else {
        list = list.filter(e => e.createType === filters.createType);
      }
    }
    if (filters.wardId) {
      list = list.filter(e => e.ward?.wardId === filters.wardId);
    }
    if (filters.featured) {
      list = list.filter(e => e.featured === true);
    }

    const page = parseInt(filters.page || 0, 10);
    const size = parseInt(filters.size || 9, 10);
    const start = page * size;
    const paginated = list.slice(start, start + size);

    return {
      content: paginated,
      totalElements: list.length,
      totalPages: Math.ceil(list.length / size),
      page: page,
      size: size
    };
  },

  getEventById(id) {
    const event = FE2_MOCK_DATA.events.find(e => e.eventId === id);
    if (!event) throw new Error('Không tìm thấy sự kiện');
    return event;
  },

  getEventSchedules(eventId) {
    return FE2_MOCK_DATA.schedules.filter(s => s.eventId === eventId);
  },

  getScheduleTickets(scheduleId) {
    return FE2_MOCK_DATA.tickets[scheduleId] || [];
  },

  getArtists() {
    return FE2_MOCK_DATA.artists;
  },

  getWards() {
    return FE2_MOCK_DATA.wards;
  }
};

// Đăng ký toàn cục để các script FE2 sử dụng
window.FE2_MOCK_DATA = FE2_MOCK_DATA;
window.FE2_Mock = FE2_Mock;
