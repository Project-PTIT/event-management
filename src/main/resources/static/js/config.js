// Cấu hình dùng chung cho toàn bộ frontend.
// Frontend và backend cùng domain (Spring Boot phục vụ static) nên dùng đường dẫn tương đối.
const API_BASE_URL = '';

const APP_CONFIG = Object.freeze({
  API_BASE_URL: API_BASE_URL,
  PAGES: Object.freeze({
    login: '/login.html',
    profile: '/profile.html',
    error: '/error.html',
    home: '/index.html'
  }),
  // Trang đích sau đăng nhập theo role. Auth.redirectAfterLogin thử lần lượt và chỉ đi tới trang THỰC SỰ tồn tại,
  // nếu chưa có trang nào thì về FALLBACK_PAGE.
  ROLE_HOME_CANDIDATES: Object.freeze({
    ROLE_ADMIN: ['/admin/statistics.html'],
    ROLE_ORGANIZER: ['/organizer/statistics.html', '/organizer/events.html'],
    ROLE_CUSTOMER: ['/index.html', '/events.html']
  }),
  FALLBACK_PAGE: '/profile.html',
  ROLE_LABELS: Object.freeze({
    ROLE_ADMIN: 'Quản trị viên',
    ROLE_ORGANIZER: 'Nhà tổ chức',
    ROLE_CUSTOMER: 'Khách hàng'
  })
});
