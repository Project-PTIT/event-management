// Admin: danh sách người dùng, lọc theo role (gọi API) và tìm nhanh trên danh sách đã tải. Dùng GET /api/admin/users.
(function () {
  const BADGE = { ROLE_ADMIN: 'ev-badge-admin', ROLE_ORGANIZER: 'ev-badge-organizer', ROLE_CUSTOMER: 'ev-badge-customer' };
  const stateBox = document.getElementById('state');
  const tableWrap = document.getElementById('tableWrap');
  const rowsBox = document.getElementById('rows');
  const roleFilter = document.getElementById('roleFilter');
  const search = document.getElementById('search');
  const count = document.getElementById('count');
  let users = [];

  function showState(html) {
    tableWrap.style.display = 'none';
    stateBox.style.display = '';
    stateBox.innerHTML = html;
  }

  function renderRows() {
    const q = search.value.trim().toLowerCase();
    const list = users.filter((u) => !q || [u.id, u.name, u.email, u.phoneNumber].some((v) => String(v || '').toLowerCase().includes(q)));
    count.textContent = list.length + ' tài khoản';
    if (list.length === 0) { showState('Không có người dùng phù hợp.'); return; }
    stateBox.style.display = 'none';
    tableWrap.style.display = '';
    rowsBox.innerHTML = list.map((u) => '<tr>'
      + '<td>' + Utils.escapeHtml(u.id) + '</td>'
      + '<td>' + Utils.escapeHtml(u.name) + '</td>'
      + '<td>' + Utils.escapeHtml(u.email) + '</td>'
      + '<td>' + Utils.escapeHtml(u.phoneNumber) + '</td>'
      + '<td><span class="ev-badge ' + (BADGE[u.role] || '') + '">' + Utils.escapeHtml(Auth.roleLabel(u.role)) + '</span></td>'
      + '</tr>').join('');
  }

  async function load() {
    showState('<span class="ev-spinner dark"></span><div class="mt-2">Đang tải danh sách...</div>');
    try {
      users = (await Api.get('/api/admin/users', { query: { role: roleFilter.value } })) || [];
      renderRows();
    } catch (err) {
      showState('<div class="alert alert-danger ev-alert">' + Utils.escapeHtml(err.message) + '</div>'
        + '<button type="button" class="ev-btn ev-btn-outline" id="retryBtn">Thử lại</button>');
      document.getElementById('retryBtn').addEventListener('click', load);
    }
  }

  (async function init() {
    await Layout.init({ active: '/admin/users.html' });
    const admin = await Auth.requireRole('ROLE_ADMIN');
    if (!admin) return;
    roleFilter.addEventListener('change', load);
    search.addEventListener('input', renderRows);
    load();
  })();
})();
