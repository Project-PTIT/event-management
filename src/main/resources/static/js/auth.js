// Trạng thái đăng nhập ở giao diện. Cần config.js và api.js nạp trước.
// Nguồn sự thật là session phía backend (GET /api/users/me). localStorage chỉ giữ gợi ý hiển thị
// (tên, role), KHÔNG dùng để quyết định quyền: chỉnh localStorage không mở được trang hay dữ liệu nào.

const Auth = (() => {
  const HINT_KEY = 'ev_user_hint';
  let state = { done: false, user: null };
  let pending = null;

  function setHint(user) {
    try { localStorage.setItem(HINT_KEY, JSON.stringify({ name: user.name, role: user.role })); } catch (e) { /* bỏ qua */ }
  }

  function clearHint() {
    try { localStorage.removeItem(HINT_KEY); } catch (e) { /* bỏ qua */ }
  }

  /** Lấy người dùng hiện tại từ backend. Trả null nếu chưa đăng nhập. Kết quả được nhớ trong trang. */
  function fetchCurrentUser(force) {
    if (!force && state.done) return Promise.resolve(state.user);
    if (!force && pending) return pending;
    pending = Api.get('/api/users/me', { redirectOn401: false })
      .then((user) => { state = { done: true, user }; setHint(user); return user; })
      .catch((err) => {
        if (err.status === 401) { state = { done: true, user: null }; clearHint(); return null; }
        throw err;
      })
      .finally(() => { pending = null; });
    return pending;
  }

  async function login(email, password) {
    const user = await Api.post('/api/auth/login', { email, password }, { redirectOn401: false });
    // Giỏ vé của phiên trước không được sang tài khoản mới
    try { localStorage.removeItem('eventTicketsCart'); } catch (e) { /* bỏ qua */ }
    state = { done: true, user };
    setHint(user);
    return user;
  }

  let loggingOut = false;

  /**
   * Đăng xuất: gọi backend hủy session, rồi HỎI LẠI backend (/api/users/me) để chắc chắn phiên đã hết.
   * - Gọi logout lỗi (trừ 401 = phiên đã hết hạn, coi là đã đăng xuất): ném lỗi, KHÔNG giả vờ đã đăng xuất.
   * - Backend vẫn còn phiên sau khi logout: ném lỗi rõ ràng, không chuyển trang (tránh vòng lặp login <-> trang chủ).
   * - Thành công: xóa dữ liệu phía trình duyệt và về trang đăng nhập (replace để nút Back không quay lại trang cũ).
   */
  async function logout() {
    if (loggingOut) return;
    loggingOut = true;
    try {
      try {
        await Api.post('/api/auth/logout', null, { redirectOn401: false });
      } catch (err) {
        if (err.status !== 401) throw err;
      }
      state = { done: false, user: null };
      pending = null;
      const stillLoggedIn = await fetchCurrentUser(true);
      if (stillLoggedIn) {
        throw new Error('Đăng xuất chưa thành công: máy chủ vẫn ghi nhận phiên đăng nhập. Vui lòng thử lại.');
      }
      clearHint();
      try { localStorage.removeItem('eventTicketsCart'); } catch (e) { /* bỏ qua */ }
      try { sessionStorage.clear(); } catch (e) { /* bỏ qua */ }
      window.location.replace(APP_CONFIG.PAGES.login + '?loggedout=1');
    } finally {
      loggingOut = false;
    }
  }

  function hasRole(user, roles) {
    const list = [].concat(roles || []);
    return !!user && (list.length === 0 || list.indexOf(user.role) !== -1);
  }

  /**
   * Chặn trang theo role. roles rỗng = chỉ cần đăng nhập.
   * Chưa đăng nhập -> về login (kèm next). Sai role -> error.html?code=403.
   * Trả user nếu được vào, trả null nếu đã chuyển hướng (trang nên dừng).
   */
  async function requireRole(roles) {
    const user = await fetchCurrentUser();
    if (!user) {
      const next = encodeURIComponent(window.location.pathname + window.location.search);
      window.location.replace(APP_CONFIG.PAGES.login + '?next=' + next);
      return null;
    }
    if (!hasRole(user, roles)) {
      window.location.replace(APP_CONFIG.PAGES.error + '?code=403');
      return null;
    }
    return user;
  }

  async function pageExists(url) {
    try {
      const res = await fetch(url, { method: 'HEAD', credentials: 'same-origin' });
      return res.ok;
    } catch (e) {
      return false;
    }
  }

  /** Sau đăng nhập: ưu tiên ?next= hợp lệ; nếu không thì trang chủ của role nếu trang đó tồn tại; cuối cùng là hồ sơ. */
  async function redirectAfterLogin(user, next) {
    if (Utils.isSafeInternalPath(next) && !/^\/(login|register-customer|register-organizer)\.html/.test(next)) {
      window.location.href = next;
      return;
    }
    const candidates = (APP_CONFIG.ROLE_HOME_CANDIDATES[user.role] || []);
    for (let i = 0; i < candidates.length; i++) {
      if (await pageExists(candidates[i])) {
        window.location.href = candidates[i];
        return;
      }
    }
    window.location.href = APP_CONFIG.FALLBACK_PAGE;
  }

  function roleLabel(role) {
    return APP_CONFIG.ROLE_LABELS[role] || role || '';
  }

  /** Cập nhật bộ nhớ trong trang sau khi sửa hồ sơ (để header hiện tên mới). */
  function setCurrentUser(user) {
    state = { done: true, user };
    setHint(user);
  }

  return { fetchCurrentUser, login, logout, hasRole, requireRole, redirectAfterLogin, roleLabel, setCurrentUser };
})();
