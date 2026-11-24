// Phần dùng chung cho các trang Organizer (organizer/*.html).
// Các file organizer-*.js gọi API.get/post/put/delete và Utils.esc/array/pick/date/money,
// đồng thời cần sẵn khung trang #page-content. File này cung cấp đủ các thứ đó.
// Nạp SAU config.js, api.js, auth.js, layout.js và TRƯỚC organizer-*.js.

(function () {
  // ---------- 1. Bổ sung hàm tiện ích vào Utils (định nghĩa trong api.js) ----------
  Object.assign(Utils, {
    // Escape HTML để chèn an toàn vào innerHTML
    esc: Utils.escapeHtml,

    // Đưa mọi dạng danh sách về mảng: mảng thẳng hoặc dạng phân trang { content: [...] }
    array(data) {
      if (Array.isArray(data)) return data;
      if (data && Array.isArray(data.content)) return data.content;
      return [];
    },

    // Lấy giá trị của khóa đầu tiên có dữ liệu; hỗ trợ khóa lồng nhau dạng 'venue.id'
    pick(obj) {
      const keys = Array.prototype.slice.call(arguments, 1);
      for (const key of keys) {
        let value = obj;
        for (const part of String(key).split('.')) {
          value = value === undefined || value === null ? undefined : value[part];
        }
        if (value !== undefined && value !== null && value !== '') return value;
      }
      return undefined;
    },

    // Ngày giờ theo định dạng Việt Nam
    date(value) {
      if (!value) return '—';
      const d = new Date(value);
      return Number.isNaN(d.getTime()) ? String(value) : d.toLocaleString('vi-VN');
    },

    // Tiền VNĐ
    money(value) {
      return Utils.formatCurrency(value);
    }
  });

  // ---------- 2. API.get/post/put/delete: trả về phần data, lỗi ném ApiError(message) ----------
  window.API = {
    get: (path, opts) => Api.get(path, opts),
    post: (path, body, opts) => Api.post(path, body, opts),
    put: (path, body, opts) => Api.put(path, body, opts),
    delete: (path, opts) => Api.delete(path, opts)
  };

  // ---------- 3. Dựng khung trang đồng bộ (để organizer-*.js tìm thấy #page-content ngay) ----------
  document.body.classList.add('ev-body', 'org-page');
  const mount = document.querySelector('[data-layout]');
  if (mount) {
    mount.outerHTML =
      '<header id="ev-header"></header>' +
      '<div class="ev-layout"><aside id="ev-sidebar"></aside>' +
      '<main class="ev-content"><div id="page-content"></div></main></div>' +
      '<footer id="ev-footer"></footer>';
  }

  // ---------- 4. Header/sidebar theo người dùng thật và chặn người không phải Organizer ----------
  // Dùng cơ chế tự khởi tạo của layout.js (kiểm tra role bằng backend, ẩn nội dung tới khi xác nhận).
  document.body.setAttribute('data-ev-page', '');
  document.body.setAttribute('data-ev-roles', 'ROLE_ORGANIZER');
})();
