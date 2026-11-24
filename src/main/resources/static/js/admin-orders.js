async function loadOrders() {
  const status = document.getElementById('statusFilter').value;
  const url = '/api/admin/orders' + (status ? '?status=' + encodeURIComponent(status) : '');
  const body = document.getElementById('ordersBody');
  body.innerHTML = '<tr><td colspan="7">Đang tải...</td></tr>';

  try {
    const orders = toList(await callApi('GET', url));
    if (orders.length === 0) {
      body.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Không có đơn hàng nào</td></tr>';
      return;
    }
    body.innerHTML = orders.map(o => `
      <tr>
        <td>${escapeHtml(o.orderId)}</td>
        <td>${escapeHtml(o.customerName)}</td>
        <td>${formatDateTime(o.createdAt)}</td>
        <td>${formatMoney(o.totalAmount)}</td>
        <td>${statusBadge(o.status)}</td>
        <td class="small" style="max-width:280px">
          ${o.refundReason ? `<div>Lý do khách: ${escapeHtml(o.refundReason)}</div>` : ''}
          ${o.refundRequestedAt ? `<div class="text-muted">Gửi lúc ${formatDateTime(o.refundRequestedAt)}</div>` : ''}
          ${o.refundResponse ? `<div>Phản hồi: ${escapeHtml(o.refundResponse)}</div>` : ''}
        </td>
        <td class="text-nowrap">${actionButtons(o)}</td>
      </tr>`).join('');
  } catch (err) {
    body.innerHTML = '';
    showMessage(err.message, 'danger');
  }
}

function actionButtons(o) {
  const id = escapeHtml(o.orderId);
  if (o.status === 'Chờ hoàn tiền') {
    return `<button class="btn btn-sm btn-success btn-refund" data-id="${id}" data-request="1">Duyệt hoàn tiền</button>
            <button class="btn btn-sm btn-outline-secondary btn-reject" data-id="${id}">Từ chối</button>`;
  }
  if (o.status === 'Đã thanh toán') {
    // Hoàn tiền trực tiếp (không có yêu cầu của khách), ví dụ khi sự kiện bị hủy
    return `<button class="btn btn-sm btn-outline-danger btn-refund" data-id="${id}">Hoàn tiền</button>`;
  }
  return '';
}

document.getElementById('statusFilter').addEventListener('change', loadOrders);

// Gắn 1 listener lên tbody vì các nút bị tạo lại mỗi lần vẽ
document.getElementById('ordersBody').addEventListener('click', async function (e) {
  const btn = e.target.closest('button');
  if (!btn) return;
  const id = btn.dataset.id;

  if (btn.classList.contains('btn-refund')) {
    const msg = btn.dataset.request
      ? 'Duyệt yêu cầu hoàn tiền cho đơn ' + id + '? Vé sẽ được hoàn lại kho.'
      : 'Hoàn tiền trực tiếp cho đơn ' + id + ' (khách chưa gửi yêu cầu)? Vé sẽ được hoàn lại kho.';
    if (!confirm(msg)) return;
    try {
      await callApi('PUT', '/api/admin/orders/' + encodeURIComponent(id) + '/refund', {});
      showMessage('Đã hoàn tiền đơn ' + id, 'success');
      loadOrders();
    } catch (err) {
      showMessage(err.message, 'danger');
    }
  } else if (btn.classList.contains('btn-reject')) {
    const note = prompt('Lý do từ chối yêu cầu hoàn tiền đơn ' + id + ' (bắt buộc, khách sẽ thấy):');
    if (note === null) return;
    if (note.trim().length < 3) { showMessage('Vui lòng nhập lý do từ chối (ít nhất 3 ký tự).', 'warning'); return; }
    try {
      await callApi('PUT', '/api/admin/orders/' + encodeURIComponent(id) + '/refund-reject', { note: note.trim() });
      showMessage('Đã từ chối yêu cầu hoàn tiền đơn ' + id, 'success');
      loadOrders();
    } catch (err) {
      showMessage(err.message, 'danger');
    }
  }
});

loadOrders();
