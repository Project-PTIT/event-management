// Hàm gọi API dùng chung + tiện ích giao diện nhỏ. Cần js/config.js nạp trước.
// Mọi file JS gọi API qua đây (Api.get/post/put/delete), không tự viết fetch.

const Utils = (() => {
  const ESC = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };

  function escapeHtml(value) {
    return String(value === undefined || value === null ? '' : value).replace(/[&<>"']/g, (c) => ESC[c]);
  }

  function formatNumber(value) {
    const n = Number(value);
    return Number.isFinite(n) ? new Intl.NumberFormat('vi-VN').format(n) : '—';
  }

  function formatCurrency(value) {
    const n = Number(value);
    return Number.isFinite(n)
      ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(n)
      : '—';
  }

  // Chỉ cho phép chuyển hướng tới đường dẫn nội bộ (chống open redirect)
  function isSafeInternalPath(path) {
    return typeof path === 'string' && path.startsWith('/') && !path.startsWith('//') && !path.includes('\\');
  }

  function queryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
  }

  function showAlert(container, type, message) {
    if (!container) return;
    container.innerHTML = '<div class="alert alert-' + type + ' ev-alert" role="alert">' + escapeHtml(message) + '</div>';
  }

  function clearAlert(container) {
    if (container) container.innerHTML = '';
  }

  function setLoading(button, loading, label) {
    if (!button) return;
    if (loading) {
      button.dataset.label = button.innerHTML;
      button.disabled = true;
      button.innerHTML = '<span class="ev-spinner"></span> ' + escapeHtml(label || 'Đang xử lý...');
    } else {
      button.disabled = false;
      if (button.dataset.label) button.innerHTML = button.dataset.label;
    }
  }

  function clearFieldErrors(form) {
    form.querySelectorAll('.is-invalid').forEach((el) => el.classList.remove('is-invalid'));
    form.querySelectorAll('.invalid-feedback').forEach((el) => { el.textContent = ''; });
  }

  // errors: { tênTrường: 'thông báo' } (backend trả trong data khi validation lỗi)
  function showFieldErrors(form, errors) {
    if (!errors || typeof errors !== 'object') return false;
    let shown = false;
    Object.keys(errors).forEach((field) => {
      const input = form.querySelector('[name="' + field + '"]');
      if (!input) return;
      input.classList.add('is-invalid');
      const holder = input.closest('.ev-field') || input.parentElement;
      const feedback = holder && holder.querySelector('.invalid-feedback');
      if (feedback) feedback.textContent = String(errors[field]);
      shown = true;
    });
    return shown;
  }

  function setFieldError(form, field, message) {
    const input = form.querySelector('[name="' + field + '"]');
    if (!input) return;
    input.classList.add('is-invalid');
    const holder = input.closest('.ev-field') || input.parentElement;
    const feedback = holder && holder.querySelector('.invalid-feedback');
    if (feedback) feedback.textContent = message;
  }

  // Nút hiện/ẩn mật khẩu: <button type="button" data-ev-pw="idCủaInput">
  function bindPasswordToggles(root) {
    (root || document).querySelectorAll('[data-ev-pw]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const input = document.getElementById(btn.dataset.evPw);
        if (!input) return;
        const show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        btn.textContent = show ? 'Ẩn' : 'Hiện';
      });
    });
  }

  // Thanh đo độ mạnh mật khẩu (chỉ gợi ý, quy tắc thật do backend kiểm tra):
  // <div class="ev-strength" data-ev-strength="idInput"><i></i><i></i><i></i><i></i></div>
  // <span class="ev-strength-text" data-ev-strength-text="idInput"></span>
  function bindPasswordStrength(root) {
    const labels = ['', 'Yếu', 'Trung bình', 'Khá', 'Mạnh'];
    (root || document).querySelectorAll('[data-ev-strength]').forEach((bar) => {
      const input = document.getElementById(bar.dataset.evStrength);
      const text = (root || document).querySelector('[data-ev-strength-text="' + bar.dataset.evStrength + '"]');
      if (!input) return;
      input.addEventListener('input', () => {
        const v = input.value;
        let level = 0;
        if (v.length >= 8) level++;
        if (/[A-Za-z]/.test(v) && /\d/.test(v)) level++;
        if (v.length >= 12) level++;
        if ((/[a-z]/.test(v) && /[A-Z]/.test(v)) || /[^A-Za-z0-9]/.test(v)) level++;
        if (!v) level = 0;
        bar.dataset.level = String(level);
        if (text) text.textContent = level ? 'Độ mạnh: ' + labels[level] : '';
      });
    });
  }

  return { escapeHtml, formatNumber, formatCurrency, isSafeInternalPath, queryParam, showAlert, clearAlert,
    setLoading, clearFieldErrors, showFieldErrors, setFieldError, bindPasswordToggles, bindPasswordStrength };
})();

const Api = (() => {
  class ApiError extends Error {
    constructor(message, status, data) {
      super(message);
      this.name = 'ApiError';
      this.status = status;
      this.data = data;
    }
  }

  const AUTH_PAGES = ['/login.html', '/register-customer.html', '/register-organizer.html'];
  let redirecting = false;

  function defaultMessage(status) {
    switch (status) {
      case 400: return 'Dữ liệu gửi lên không hợp lệ.';
      case 401: return 'Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.';
      case 403: return 'Bạn không có quyền thực hiện thao tác này.';
      case 404: return 'Không tìm thấy dữ liệu yêu cầu.';
      default: return status >= 500 ? 'Máy chủ gặp lỗi. Vui lòng thử lại sau.' : 'Yêu cầu không thành công.';
    }
  }

  function buildUrl(path, query) {
    let url = API_BASE_URL + path;
    if (query) {
      const params = new URLSearchParams();
      Object.keys(query).forEach((k) => {
        if (query[k] !== undefined && query[k] !== null && query[k] !== '') params.append(k, query[k]);
      });
      const qs = params.toString();
      if (qs) url += (url.includes('?') ? '&' : '?') + qs;
    }
    return url;
  }

  // 401: về trang đăng nhập một lần, không lặp; bỏ qua nếu đang ở trang đăng nhập/đăng ký.
  function handleUnauthorized() {
    if (redirecting || AUTH_PAGES.includes(window.location.pathname)) return;
    redirecting = true;
    try { localStorage.removeItem('eventTicketsCart'); localStorage.removeItem('ev_user_hint'); } catch (e) { /* bỏ qua */ }
    const next = encodeURIComponent(window.location.pathname + window.location.search);
    window.location.href = APP_CONFIG.PAGES.login + '?expired=1&next=' + next;
  }

  /**
   * opts: { query, body, redirectOn401 (mặc định true), withMessage (trả {message, data}) }
   * Thành công: trả về data. Thất bại: ném ApiError(message, status, data).
   */
  async function request(method, path, opts) {
    const options = opts || {};
    const headers = { Accept: 'application/json' };
    const init = { method, headers, credentials: 'same-origin' };
    if (options.body !== undefined && options.body !== null) {
      headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(options.body);
    }

    let response;
    try {
      response = await fetch(buildUrl(path, options.query), init);
    } catch (networkError) {
      throw new ApiError('Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối và thử lại.', 0, null);
    }

    let payload = null;
    const text = await response.text();
    if (text) {
      try { payload = JSON.parse(text); } catch (parseError) { payload = null; } // phản hồi không phải JSON
    }
    const enveloped = payload !== null && typeof payload === 'object' && 'success' in payload;

    if (!response.ok || (enveloped && payload.success === false)) {
      if (response.status === 401 && options.redirectOn401 !== false) handleUnauthorized();
      const message = enveloped && payload.message ? payload.message : defaultMessage(response.status);
      throw new ApiError(message, response.status, enveloped ? payload.data : null);
    }

    const data = enveloped ? payload.data : payload;
    if (options.withMessage) return { message: enveloped ? payload.message : '', data };
    return data;
  }

  return {
    ApiError,
    request,
    get: (path, opts) => request('GET', path, opts),
    post: (path, body, opts) => request('POST', path, Object.assign({}, opts, { body })),
    put: (path, body, opts) => request('PUT', path, Object.assign({}, opts, { body })),
    delete: (path, opts) => request('DELETE', path, opts)
  };
})();
