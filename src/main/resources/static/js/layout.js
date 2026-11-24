// Header, footer, menu theo role, thanh bên cho Organizer/Admin.
// Cần config.js, api.js, auth.js nạp trước. Trang chỉ cần:
//   <header id="ev-header"></header> ... <footer id="ev-footer"></footer>
//   (tùy chọn) <aside id="ev-sidebar" class="ev-sidebar"></aside>
//   Layout.init({ active: '/admin/statistics.html' });
// Ẩn menu chỉ phục vụ giao diện; quyền thật do backend kiểm tra.
//
// Cách dùng ngắn gọn cho mọi trang (không cần viết script riêng):
//   <body data-ev-page>                          -> tự dựng header/footer theo người dùng thật
//   <body data-ev-page data-ev-roles="ROLE_ADMIN">  -> thêm chặn truy cập theo role (rỗng = chỉ cần đăng nhập)
// Trang được chặn sẽ ẩn nội dung cho tới khi backend xác nhận quyền, và tự kiểm tra lại khi quay về bằng nút Back.

const Layout = (() => {
  const esc = Utils.escapeHtml;
  const BOOTSTRAP_CSS = 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css';

  const PUBLIC_LINKS = [
    { href: '/index.html', label: 'Trang chủ' },
    { href: '/events.html', label: 'Sự kiện' },
    { href: '/artists.html', label: 'Nghệ sĩ' }
  ];

  const MENUS = {
    ROLE_CUSTOMER: PUBLIC_LINKS.concat([
      { href: '/my-orders.html', label: 'Đơn của tôi' }
    ]),
    ROLE_ORGANIZER: PUBLIC_LINKS.concat([
      { href: '/organizer/events.html', label: 'Quản lý sự kiện' },
      { href: '/organizer/statistics.html', label: 'Thống kê' }
    ]),
    ROLE_ADMIN: [
      { href: '/admin/statistics.html', label: 'Thống kê' },
      { href: '/admin/events-approval.html', label: 'Duyệt sự kiện' },
      { href: '/admin/orders.html', label: 'Đơn hàng' },
      { href: '/admin/users.html', label: 'Người dùng' },
      { href: '/index.html', label: 'Xem website' }
    ]
  };

  const SIDEBARS = {
    ROLE_ADMIN: [
      { href: '/admin/statistics.html', label: 'Thống kê hệ thống' },
      { href: '/admin/events-approval.html', label: 'Duyệt sự kiện' },
      { href: '/admin/orders.html', label: 'Đơn hàng' },
      { href: '/admin/users.html', label: 'Người dùng' },
      { href: '/profile.html', label: 'Hồ sơ của tôi' }
    ],
    ROLE_ORGANIZER: [
      { href: '/organizer/statistics.html', label: 'Doanh thu & vé' },
      { href: '/organizer/events.html', label: 'Sự kiện của tôi' },
      { href: '/profile.html', label: 'Hồ sơ của tôi' }
    ]
  };

  let lastOptions = {};

  function ensureAssets() {
    if (!document.querySelector('link[href*="bootstrap"]')) {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = BOOTSTRAP_CSS;
      document.head.prepend(link);
    }
    if (!document.querySelector('link[href$="css/style.css"]')) {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = '/css/style.css';
      document.head.appendChild(link);
    }
    document.body.classList.add('ev-body');
  }

  function isActive(href, active) {
    return (active || window.location.pathname) === href;
  }

  function linksHtml(items, active) {
    return items.map((i) => '<a href="' + esc(i.href) + '"' + (isActive(i.href, active) ? ' class="active"' : '') + '>' + esc(i.label) + '</a>').join('');
  }

  function initials(name) {
    const parts = String(name || '?').trim().split(/\s+/);
    return esc((parts[parts.length - 1] || '?').charAt(0).toUpperCase());
  }

  // Giỏ vé lưu ở localStorage (key trùng cart.js). Chỉ khách và Customer có giỏ vé.
  const CART_KEY = 'eventTicketsCart';
  // Xóa giỏ vé khi đăng xuất / hết phiên (giỏ lưu localStorage, tránh lộ sang tài khoản khác)

  function cartLinkHtml() {
    return '<a class="ev-cart" href="/cart.html" aria-label="Giỏ vé">Giỏ vé'
      + '<span id="navbarCartBadge" class="ev-cart-badge d-none">0</span></a>';
  }

  /** Cập nhật số vé trên biểu tượng giỏ vé. */
  function updateCartBadge() {
    const badge = document.getElementById('navbarCartBadge');
    if (!badge) return;
    let total = 0;
    try {
      total = (JSON.parse(localStorage.getItem(CART_KEY)) || []).reduce((sum, i) => sum + (Number(i.quantity) || 1), 0);
    } catch (e) { total = 0; }
    badge.textContent = total;
    badge.classList.toggle('d-none', total === 0);
  }

  function headerHtml(user, active) {
    const menu = user ? (MENUS[user.role] || PUBLIC_LINKS) : PUBLIC_LINKS;
    let right;
    if (user) {
      right = '<div class="ev-user">'
        + (user.role === 'ROLE_CUSTOMER' ? cartLinkHtml() : '')
        + '<button type="button" class="ev-user-btn" data-ev-toggle="ev-user-menu"><span class="ev-avatar">' + initials(user.name) + '</span>'
        + '<span>' + esc(user.name) + '</span></button>'
        + '<div class="ev-menu" id="ev-user-menu">'
        + '<div class="ev-menu-head">' + esc(Auth.roleLabel(user.role)) + '<br>' + esc(user.email) + '</div>'
        + '<a href="/profile.html">Hồ sơ của tôi</a>'
        + '<button type="button" data-ev-logout>Đăng xuất</button></div></div>';
    } else {
      right = '<div class="ev-auth-links">'
        + '<a class="ev-btn ev-btn-outline ev-btn-sm" href="/login.html">Đăng nhập</a>'
        + '<button type="button" class="ev-btn ev-btn-primary ev-btn-sm" data-ev-toggle="ev-register-menu">Đăng ký</button>'
        + '<div class="ev-menu" id="ev-register-menu">'
        + '<a href="/register-customer.html">Tôi là khách hàng</a>'
        + '<a href="/register-organizer.html">Tôi là nhà tổ chức</a></div></div>';
    }
    return '<div class="ev-header-inner">'
      + '<a class="ev-brand" href="/index.html">Event<span>Tickets</span></a>'
      + '<button type="button" class="ev-nav-toggle" data-ev-toggle="ev-nav" aria-label="Mở menu">☰</button>'
      + '<nav class="ev-nav" id="ev-nav">' + linksHtml(menu, active) + '</nav>'
      + right + '</div>';
  }

  function footerHtml() {
    return '<div class="ev-footer-inner"><span>© ' + new Date().getFullYear() + ' EventTickets – Hệ thống bán vé sự kiện</span></div>'; 
  }

  function render(user, options) {
    const active = options.active;
    const header = document.getElementById('ev-header');
    if (header) { header.className = 'ev-header'; header.innerHTML = headerHtml(user, active); updateCartBadge(); }
    const footer = document.getElementById('ev-footer');
    if (footer) { footer.className = 'ev-footer'; footer.innerHTML = footerHtml(); }
    const sidebar = document.getElementById('ev-sidebar');
    if (sidebar) {
      const items = user && SIDEBARS[user.role];
      if (items) {
        sidebar.className = 'ev-sidebar';
        sidebar.innerHTML = '<div class="ev-sidebar-title">' + esc(Auth.roleLabel(user.role)) + '</div>' + linksHtml(items, active);
      } else {
        sidebar.style.display = 'none';
      }
    }
  }

  function bindEvents() {
    document.addEventListener('click', (event) => {
      const toggle = event.target.closest('[data-ev-toggle]');
      const open = document.querySelectorAll('.ev-menu.show');
      if (toggle) {
        const target = document.getElementById(toggle.dataset.evToggle);
        if (target) {
          const wasOpen = target.classList.contains('show');
          open.forEach((m) => m.classList.remove('show'));
          target.classList.toggle('show', !wasOpen);
        }
        return;
      }
      if (event.target.closest('[data-ev-logout]')) {
        Auth.logout().catch((err) => window.alert(err.message));
        return;
      }
      if (!event.target.closest('.ev-menu')) open.forEach((m) => m.classList.remove('show'));
    });
  }

  let bound = false;

  /** Dựng layout theo người dùng thật từ backend. Trả về user (hoặc null nếu là khách). */
  async function init(options) {
    lastOptions = options || {};
    ensureAssets();
    if (!bound) { bindEvents(); bound = true; }
    let user = null;
    try {
      user = await Auth.fetchCurrentUser();
    } catch (err) {
      user = null; // lỗi mạng/máy chủ: hiển thị như khách, trang tự báo lỗi riêng
    }
    render(user, lastOptions);
    return user;
  }

  /** Vẽ lại header sau khi dữ liệu người dùng đổi (ví dụ vừa sửa hồ sơ). */
  async function refresh() {
    const user = await Auth.fetchCurrentUser();
    render(user, lastOptions);
  }

  // ---------- Tự khởi tạo theo thuộc tính của <body> ----------
  window.addEventListener('storage', updateCartBadge);

  document.addEventListener('DOMContentLoaded', async () => {
    const body = document.body;
    if (!body || !body.hasAttribute('data-ev-page')) return; // trang tự gọi Layout.init thì bỏ qua
    const guarded = body.hasAttribute('data-ev-roles');
    if (guarded) body.style.visibility = 'hidden'; // chưa xác nhận quyền thì chưa cho thấy nội dung
    try {
      await init({ active: window.location.pathname });
      if (guarded) {
        const roles = (body.getAttribute('data-ev-roles') || '').split(',').map((r) => r.trim()).filter(Boolean);
        const allowed = await Auth.requireRole(roles);
        if (!allowed) return; // requireRole đã chuyển hướng, giữ nội dung ẩn
      }
    } catch (err) {
      // Lỗi mạng/máy chủ: không chuyển hướng bừa; API của từng trang vẫn tự kiểm tra quyền
      console.warn('Không xác thực được người dùng:', err);
    }
    body.style.visibility = '';
  });

  // Quay lại bằng nút Back/Forward (bfcache) có thể hiện lại trang cũ: tải lại để kiểm tra lại phiên đăng nhập.
  window.addEventListener('pageshow', (event) => {
    if (event.persisted && document.body && document.body.hasAttribute('data-ev-roles')) {
      window.location.reload();
    }
  });

  return { init, refresh, updateCartBadge };
})();
