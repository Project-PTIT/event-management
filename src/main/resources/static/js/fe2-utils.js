// FE2 Utilities & API Client
// Chế độ MOCK: true để chạy giả lập độc lập, false khi gọi API thật từ Spring Boot backend
const FE2_USE_MOCK = true;

const FE2_API = {
  async fetch(url, options = {}) {
    // Nếu bật mock, điều hướng sang FE2_Mock
    if (FE2_USE_MOCK && window.FE2_Mock) {
      await new Promise(r => setTimeout(r, 150)); // Giả lập độ trễ mạng nhẹ

      // Phân tích URL
      const [path, queryString] = url.split('?');
      const params = Object.fromEntries(new URLSearchParams(queryString || ''));

      if (path === '/api/events') {
        return window.FE2_Mock.getEvents(params);
      }
      const eventDetailMatch = path.match(/^\/api\/events\/([a-zA-Z0-9_-]+)$/);
      if (eventDetailMatch) {
        return window.FE2_Mock.getEventById(eventDetailMatch[1]);
      }
      const schedulesMatch = path.match(/^\/api\/events\/([a-zA-Z0-9_-]+)\/schedules$/);
      if (schedulesMatch) {
        return window.FE2_Mock.getEventSchedules(schedulesMatch[1]);
      }
      const ticketsMatch = path.match(/^\/api\/schedules\/([a-zA-Z0-9_-]+)\/tickets$/);
      if (ticketsMatch) {
        return window.FE2_Mock.getScheduleTickets(ticketsMatch[1]);
      }
      if (path === '/api/artists') {
        return window.FE2_Mock.getArtists();
      }
      if (path === '/api/wards') {
        return window.FE2_Mock.getWards();
      }
    }

    // Khi gọi API thật
    const res = await fetch(url, options);
    if (!res.ok) {
      let errorMsg = `Lỗi hệ thống (${res.status})`;
      try {
        const errJson = await res.json();
        if (errJson && errJson.message) errorMsg = errJson.message;
      } catch (e) {}
      throw new Error(errorMsg);
    }
    const json = await res.json();
    return json.data !== undefined ? json.data : json;
  }
};

function formatCurrency(amount) {
  if (amount == null || isNaN(amount)) return '0 ₫';
  return Number(amount).toLocaleString('vi-VN') + ' ₫';
}

function formatDate(isoStr) {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  return d.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

function formatDateTime(isoStr) {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  return d.toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

function escapeHtml(text) {
  if (text == null) return '';
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return String(text).replace(/[&<>"']/g, m => map[m]);
}

function getStatusBadge(status) {
  const map = {
    'Đang mở bán': 'success',
    'Sắp diễn ra': 'primary',
    'Mở đặt vé sớm': 'info',
    'Đang diễn ra': 'warning',
    'Đã kết thúc': 'secondary',
    'APPROVED': 'success',
    'PENDING': 'warning',
    'REJECTED': 'danger'
  };
  const color = map[status] || 'secondary';
  return `<span class="badge bg-${color}">${escapeHtml(status)}</span>`;
}

function getCreateTypeBadge(type) {
  const map = {
    'YEARLY': '<span class="badge bg-purple text-white">Hàng năm</span>',
    'MONTHLY': '<span class="badge bg-teal text-white">Hàng tháng</span>',
    'WEEKLY': '<span class="badge bg-info text-dark">Hàng tuần</span>'
  };
  return map[type] || '<span class="badge bg-light text-secondary border">Một lần</span>';
}

// Cập nhật số lượng vé trên icon giỏ hàng của navbar nếu có
function updateNavbarCartBadge() {
  const badge = document.getElementById('navbarCartBadge');
  if (!badge) return;
  try {
    const cart = JSON.parse(localStorage.getItem('eventTicketsCart')) || [];
    const totalQty = cart.reduce((sum, item) => sum + (item.quantity || 1), 0);
    badge.textContent = totalQty;
    badge.classList.toggle('d-none', totalQty === 0);
  } catch (e) {
    badge.classList.add('d-none');
  }
}

// Lắng nghe sự kiện storage để đồng bộ giỏ hàng giữa các tab
window.addEventListener('storage', updateNavbarCartBadge);
document.addEventListener('DOMContentLoaded', updateNavbarCartBadge);

window.FE2_API = FE2_API;
window.formatCurrency = formatCurrency;
window.formatDate = formatDate;
window.formatDateTime = formatDateTime;
window.escapeHtml = escapeHtml;
window.getStatusBadge = getStatusBadge;
window.getCreateTypeBadge = getCreateTypeBadge;
window.updateNavbarCartBadge = updateNavbarCartBadge;
