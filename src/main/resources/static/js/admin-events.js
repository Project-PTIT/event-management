let currentStatus = 'PENDING';   // gửi lên API phải đúng chữ PENDING / APPROVED / REJECTED

async function loadEvents() {
  const body = document.getElementById('eventsBody');
  body.innerHTML = '<tr><td colspan="6">Đang tải...</td></tr>';
  try {
    const events = toList(await callApi('GET', '/api/admin/events?status=' + currentStatus));
    if (events.length === 0) {
      body.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Không có sự kiện nào</td></tr>';
      return;
    }
    body.innerHTML = events.map(ev => `
      <tr>
        <td>${escapeHtml(ev.eventId)}</td>
        <td>${escapeHtml(ev.eventName)}</td>
        <td>${escapeHtml(ev.organizerName)}</td>
        <td>${escapeHtml(ev.createType || 'Không lặp')}</td>
        <td>${statusBadge(ev.status)}</td>
        <td>${ev.status === 'PENDING'
              ? `<button class="btn btn-sm btn-success btn-approve" data-id="${escapeHtml(ev.eventId)}">Duyệt</button>
                 <button class="btn btn-sm btn-danger btn-reject" data-id="${escapeHtml(ev.eventId)}">Từ chối</button>`
              : ''}</td>
      </tr>`).join('');
  } catch (err) {
    body.innerHTML = '';
    showMessage(err.message, 'danger');
  }
}

// Đổi tab
document.getElementById('statusTabs').addEventListener('click', function (e) {
  const tab = e.target.closest('.nav-link');
  if (!tab) return;
  e.preventDefault();   // chặn thẻ <a href="#"> nhảy lên đầu trang
  document.querySelectorAll('#statusTabs .nav-link').forEach(a => a.classList.remove('active'));
  tab.classList.add('active');
  currentStatus = tab.dataset.status;
  loadEvents();
});

// Duyệt hoặc từ chối
document.getElementById('eventsBody').addEventListener('click', async function (e) {
  let action = null;
  if (e.target.classList.contains('btn-approve')) action = 'approve';
  if (e.target.classList.contains('btn-reject')) action = 'reject';
  if (!action) return;

  const text = action === 'approve' ? 'duyệt' : 'từ chối';
  if (!confirm('Bạn muốn ' + text + ' sự kiện này?')) return;

  try {
    await callApi('PUT', '/api/admin/events/' + e.target.dataset.id + '/' + action);
    showMessage('Đã ' + text + ' sự kiện ' + e.target.dataset.id, 'success');
    loadEvents();
  } catch (err) {
    showMessage(err.message, 'danger');
  }
});

loadEvents();