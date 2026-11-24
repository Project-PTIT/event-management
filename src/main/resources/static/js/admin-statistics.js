// Dashboard thống kê Admin. Gọi GET /api/admin/statistics/overview.
// Số liệu hoàn toàn lấy từ API, không có dữ liệu giả. Phần đọc dữ liệu chấp nhận vài tên trường phổ biến;
// chỉ số nào API không trả sẽ hiện "—" kèm ghi chú. Cần đổi tên trường thì chỉ sửa các mảng KEYS bên dưới.

(function () {
  const ENDPOINT = '/api/admin/statistics/overview';

  const KEYS = {
    users: ['totalUsers', 'userCount', 'totalUser', 'totalAccounts', 'users'],
    revenue: ['totalRevenue', 'revenue', 'totalRevenueAmount'],
    pending: ['pendingEvents', 'eventsPending', 'pendingEventCount', 'pendingCount', 'pending'],
    approved: ['approvedEvents', 'eventsApproved', 'approvedEventCount', 'approvedCount', 'approved'],
    rejected: ['rejectedEvents', 'eventsRejected', 'rejectedEventCount', 'rejectedCount', 'rejected'],
    total: ['totalEvents', 'eventCount', 'totalEventCount'],
    byStatus: ['eventsByStatus', 'eventStatusCounts', 'eventCountByStatus', 'eventsStatus', 'statusCounts', 'events']
  };

  const stateBox = document.getElementById('state');
  const content = document.getElementById('content');
  const reloadBtn = document.getElementById('reloadBtn');

  function pick(obj, keys) {
    if (!obj || typeof obj !== 'object') return undefined;
    for (let i = 0; i < keys.length; i++) {
      const v = obj[keys[i]];
      if (v !== undefined && v !== null && typeof v !== 'object') return v;
    }
    return undefined;
  }

  // Chấp nhận { PENDING: 3, ... } hoặc [{ status: 'PENDING', count: 3 }, ...]
  function statusCounts(data) {
    const result = {};
    for (let i = 0; i < KEYS.byStatus.length; i++) {
      const v = data[KEYS.byStatus[i]];
      if (Array.isArray(v)) {
        v.forEach((row) => {
          const status = String(row.status || row.name || '').toUpperCase();
          const count = row.count !== undefined ? row.count : row.total;
          if (status && count !== undefined) result[status] = Number(count);
        });
        return result;
      }
      if (v && typeof v === 'object') {
        Object.keys(v).forEach((k) => { result[k.toUpperCase()] = Number(v[k]); });
        return result;
      }
    }
    return result;
  }

  function normalize(data) {
    const byStatus = statusCounts(data || {});
    const num = (direct, status) => {
      const v = direct !== undefined ? Number(direct) : byStatus[status];
      return Number.isFinite(v) ? v : undefined;
    };
    const pending = num(pick(data, KEYS.pending), 'PENDING');
    const approved = num(pick(data, KEYS.approved), 'APPROVED');
    const rejected = num(pick(data, KEYS.rejected), 'REJECTED');
    let total = pick(data, KEYS.total);
    total = total !== undefined ? Number(total) : undefined;
    if (total === undefined && [pending, approved, rejected].every((v) => v !== undefined)) total = pending + approved + rejected;
    const users = pick(data, KEYS.users);
    const revenue = pick(data, KEYS.revenue);
    return {
      users: users !== undefined ? Number(users) : undefined,
      revenue: revenue !== undefined ? Number(revenue) : undefined,
      pending, approved, rejected, total
    };
  }

  const show = (v, fmt) => (v === undefined || !Number.isFinite(v) ? '—' : fmt(v));

  function showState(html) {
    content.style.display = 'none';
    stateBox.style.display = '';
    stateBox.innerHTML = html;
  }

  function renderBars(s) {
    const rows = [
      { name: 'Chờ duyệt', cls: 'pending', value: s.pending },
      { name: 'Đã duyệt', cls: 'approved', value: s.approved },
      { name: 'Từ chối', cls: 'rejected', value: s.rejected }
    ];
    const max = Math.max(1, ...rows.map((r) => r.value || 0));
    document.getElementById('bars').innerHTML = rows.map((r) => {
      const width = r.value === undefined ? 0 : Math.round((r.value / max) * 100);
      return '<div class="ev-bar-row"><div class="ev-bar-name">' + r.name + '</div>'
        + '<div class="ev-bar-track"><div class="ev-bar-fill ' + r.cls + '" style="width:' + width + '%"></div></div>'
        + '<div class="ev-bar-num">' + show(r.value, Utils.formatNumber) + '</div></div>';
    }).join('');
  }

  function render(data) {
    const s = normalize(data);
    document.getElementById('statUsers').textContent = show(s.users, Utils.formatNumber);
    document.getElementById('statRevenue').textContent = show(s.revenue, Utils.formatCurrency);
    document.getElementById('statEventsTotal').textContent = show(s.total, Utils.formatNumber);
    document.getElementById('statPending').textContent = show(s.pending, Utils.formatNumber);
    document.getElementById('statApproved').textContent = show(s.approved, Utils.formatNumber);
    document.getElementById('statRejected').textContent = show(s.rejected, Utils.formatNumber);
    renderBars(s);

    const missing = [];
    if (s.users === undefined) missing.push('tổng người dùng');
    if (s.revenue === undefined) missing.push('tổng doanh thu');
    if (s.pending === undefined || s.approved === undefined || s.rejected === undefined) missing.push('số sự kiện theo trạng thái');
    const notice = document.getElementById('notice');
    if (missing.length) {
      Utils.showAlert(notice, 'warning', 'Chưa có số liệu: ' + missing.join(', ') + '.');
    } else {
      notice.innerHTML = '';
    }
    stateBox.style.display = 'none';
    content.style.display = '';
  }

  function hasAnyData(data) {
    return data && typeof data === 'object' && Object.keys(data).length > 0;
  }

  async function load() {
    reloadBtn.disabled = true;
    showState('<span class="ev-spinner dark"></span><div class="mt-2">Đang tải số liệu...</div>');
    try {
      const data = await Api.get(ENDPOINT);
      if (!hasAnyData(data)) {
        showState('Chưa có dữ liệu thống kê.');
      } else {
        render(data);
      }
    } catch (err) {
      let message = err.message;
      if (err.status === 404) {
        message = 'Chưa có dữ liệu thống kê. Vui lòng thử lại sau.';
      }
      showState('<div class="alert alert-danger ev-alert">' + Utils.escapeHtml(message) + '</div>'
        + '<button type="button" class="ev-btn ev-btn-outline" id="retryBtn">Thử lại</button>');
      document.getElementById('retryBtn').addEventListener('click', load);
    } finally {
      reloadBtn.disabled = false;
    }
  }

  (async function init() {
    await Layout.init({ active: '/admin/statistics.html' });
    const admin = await Auth.requireRole('ROLE_ADMIN'); // backend vẫn kiểm tra lại quyền ở API
    if (!admin) return;
    reloadBtn.addEventListener('click', load);
    load();
  })();
})();
