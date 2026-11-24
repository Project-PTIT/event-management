// Đã tích hợp API thật nên để false (không dùng dữ liệu giả nữa)
const USE_MOCK = false;

const STATUS_BADGE = {
  'Chờ thanh toán': 'warning',
  'Đã thanh toán': 'success',
  'Đã hủy': 'secondary',
  'Đã hoàn tiền': 'info',
  'Chờ hoàn tiền': 'primary',
  'PENDING': 'warning',
  'APPROVED': 'success',
  'REJECTED': 'danger'
};

function formatMoney(n) {
  return Number(n || 0).toLocaleString('vi-VN') + ' ₫';
}

function formatDateTime(s) {
  return s ? new Date(s).toLocaleString('vi-VN') : '';
}

// Chống chèn HTML lạ từ dữ liệu người dùng nhập
function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text == null ? '' : text;
  return div.innerHTML;
}

// Danh sách có thể là mảng hoặc dạng phân trang { content: [...] }
function toList(data) {
  if (Array.isArray(data)) return data;
  if (data && Array.isArray(data.content)) return data.content;
  return [];
}

function statusBadge(status) {
  const color = STATUS_BADGE[status] || 'secondary';
  return `<span class="badge text-bg-${color}">${escapeHtml(status)}</span>`;
}

function showMessage(text, type) {
  const box = document.getElementById('message');
  if (box) box.innerHTML = `<div class="alert alert-${type}">${escapeHtml(text)}</div>`;
}

// Gọi API, trả về phần "data"; lỗi thì ném Error kèm message của server
async function callApi(method, url, body) {
  if (USE_MOCK) return mockApi(method, url, body);

  const options = { method: method, headers: {} };
  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }
  options.credentials = 'same-origin';
  const res = await fetch(url, options);
  if (res.status === 401) {
    // Phiên hết hạn hoặc chưa đăng nhập: về trang đăng nhập và quay lại đúng trang này sau khi đăng nhập
    location.replace('/login.html?expired=1&next=' + encodeURIComponent(location.pathname + location.search));
    throw new Error('Bạn cần đăng nhập');
  }
  let json = null;
  try { json = await res.json(); } catch (e) { }
  if (!res.ok || !json || !json.success) {
    throw new Error((json && json.message) || 'Có lỗi xảy ra (mã ' + res.status + ')');
  }
  return json.data;
}