async function loadOrders() {
  const status = document.getElementById('statusFilter').value;
  // encodeURIComponent: đổi chữ có dấu và khoảng trắng thành dạng an toàn trên URL
  const url = '/api/orders/me' + (status ? '?status=' + encodeURIComponent(status) : '');
  const body = document.getElementById('ordersBody');
  body.innerHTML = '<tr><td colspan="5">Đang tải...</td></tr>';

  try {
    const orders = toList(await callApi('GET', url));
    if (orders.length === 0) {
      body.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Chưa có đơn hàng nào</td></tr>';
      return;
    }
    body.innerHTML = orders.map(o => `
      <tr>
        <td><a href="/order-detail.html?id=${encodeURIComponent(o.orderId)}">${escapeHtml(o.orderId)}</a></td>
        <td>${formatDateTime(o.createdAt)}</td>
        <td>${formatMoney(o.totalAmount)}</td>
        <td>${statusBadge(o.status)}${o.status === 'Chờ thanh toán' && o.expiresAt
              ? `<div class="small text-muted">Hết hạn lúc ${formatDateTime(o.expiresAt)}</div>` : ''}
            ${o.status === 'Chờ hoàn tiền' ? '<div class="small text-muted">Đang chờ quản trị viên xử lý</div>' : ''}</td>
        <td class="text-nowrap">${o.status === 'Chờ thanh toán'
              ? `<a class="btn btn-sm btn-primary" href="/order-detail.html?id=${encodeURIComponent(o.orderId)}">Thanh toán</a>
                 <button class="btn btn-sm btn-outline-danger btn-cancel"
                         data-id="${escapeHtml(o.orderId)}">Hủy đơn</button>`
              : (o.status === 'Đã thanh toán' && o.refundableUntil && new Date() < new Date(o.refundableUntil.replace(' ', 'T'))
                  ? `<a class="btn btn-sm btn-outline-secondary" href="/order-detail.html?id=${encodeURIComponent(o.orderId)}#refund">Yêu cầu hoàn tiền</a>`
                  : '')}</td>
      </tr>`).join('');
  } catch (err) {
    body.innerHTML = '';
    showMessage(err.message, 'danger');
  }
}

document.getElementById('statusFilter').addEventListener('change', loadOrders);

// Gắn 1 listener lên tbody vì các nút bị tạo lại mỗi lần vẽ
document.getElementById('ordersBody').addEventListener('click', async function (e) {
  if (!e.target.classList.contains('btn-cancel')) return;
  if (!confirm('Bạn chắc chắn muốn hủy đơn này?')) return;
  try {
    await callApi('PUT', '/api/orders/' + e.target.dataset.id + '/cancel');
    showMessage('Đã hủy đơn ' + e.target.dataset.id, 'success');
    loadOrders();
  } catch (err) {
    showMessage(err.message, 'danger');
  }
});

loadOrders();