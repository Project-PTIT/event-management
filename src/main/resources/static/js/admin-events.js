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
        <td>${escapeHtml(ev.title || ev.eventName)}</td>
        <td>${escapeHtml(ev.organizerName)}</td>
        <td>${escapeHtml(ev.createType || 'Không lặp')}</td>
        <td>${statusBadge(ev.status)}</td>
        <td class="text-nowrap"><button class="btn btn-sm btn-outline-primary btn-view" data-id="${escapeHtml(ev.eventId)}">Xem</button>
            ${ev.status === 'PENDING'
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
  if (e.target.classList.contains('btn-view')) { openDetail(e.target.dataset.id); return; }
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

// Xem chi tiết sự kiện (ảnh, mô tả, suất diễn) trước khi duyệt. Admin xem được mọi trạng thái.
async function openDetail(eventId) {
  const body = document.getElementById('eventModalBody');
  const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('eventModal'));
  document.getElementById('eventModalTitle').textContent = 'Chi tiết sự kiện ' + eventId;
  body.innerHTML = '<div class="text-center text-muted py-4">Đang tải...</div>';
  modal.show();
  try {
    const [ev, schedules] = await Promise.all([
      callApi('GET', '/api/events/' + encodeURIComponent(eventId)),
      callApi('GET', '/api/events/' + encodeURIComponent(eventId) + '/schedules')
    ]);
    const list = Array.isArray(schedules) ? schedules : [];
    body.innerHTML = `
      <img src="${escapeHtml(ev.image)}" alt="" class="img-fluid rounded mb-3 w-100" style="max-height:320px;object-fit:cover"
           onerror="this.onerror=null; this.src='/images/placeholder-event.svg';">
      <h5>${escapeHtml(ev.title)}</h5>
      <p class="text-muted mb-1">Nhà tổ chức: ${escapeHtml(ev.organizerName)} (${escapeHtml(ev.organizerId)})</p>
      <p class="mb-1">Chu kỳ: ${escapeHtml(ev.createType || 'Không lặp')} | Trạng thái: ${statusBadge(ev.status)}</p>
      <p class="mt-3" style="white-space:pre-wrap">${escapeHtml(ev.desc)}</p>
      <h6 class="mt-4">Suất diễn (${list.length})</h6>
      ${list.length === 0 ? '<div class="text-muted small">Chưa có suất diễn.</div>' : `
        <ul class="list-group">${list.map(s => `
          <li class="list-group-item">
            <strong>${formatDateTime(s.startDatetime)}</strong> – ${formatDateTime(s.endDatetime)}
            <div class="small text-muted">${escapeHtml(s.venue ? s.venue.venueName : 'Chưa có địa điểm')}
              ${s.venue && s.venue.address ? ' · ' + escapeHtml(s.venue.address) : ''}</div>
            <div class="small">${escapeHtml(s.status || '')}${Array.isArray(s.artists) && s.artists.length
              ? ' · Nghệ sĩ: ' + s.artists.map(a => escapeHtml(a.artistName)).join(', ') : ''}</div>
          </li>`).join('')}</ul>`}`;
  } catch (err) {
    body.innerHTML = '<div class="alert alert-danger mb-0">' + escapeHtml(err.message) + '</div>';
  }
}

loadEvents();