async function loadOrders() {
  const status = document.getElementById('statusFilter').value;
  const url = '/api/admin/orders' + (status ? '?status=' + encodeURIComponent(status) : '');
  const body = document.getElementById('ordersBody');
  body.innerHTML = '<tr><td colspan="6">Đang tải...</td></tr>';

  try {
    const orders = toList(await callApi('GET', url));
    if (orders.length === 0) {
      body.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Không có đơn hàng nào</td></tr>';
      return;
    }
    body.innerHTML = orders.map(o => `
      <tr>
        <td>${escapeHtml(o.orderId)}</td>
        <td>${escapeHtml(o.customerName)}</td>
        <td>${formatDateTime(o.createdAt)}</td>
        <td>${formatMoney(o.totalAmount)}</td>
        <td>${statusBadge(o.status)}</td>
        <td>${o.status === 'Đã thanh toán'
              ? `<button class="btn btn-sm btn-outline-danger btn-refund"
                         data-id="${escapeHtml(o.orderId)}">Hoàn tiền</button>`
              : ''}</td>
      </tr>`).join('');
  } catch (err) {
    body.innerHTML = '';
    showMessage(err.message, 'danger');
  }
}

document.getElementById('statusFilter').addEventListener('change', loadOrders);

// Gắn 1 listener lên tbody vì các nút bị tạo lại mỗi lần vẽ
document.getElementById('ordersBody').addEventListener('click', async function (e) {
  if (!e.target.classList.contains('btn-refund')) return;
  const id = e.target.dataset.id;
  if (!confirm('Hoàn tiền cho đơn ' + id + '? Vé sẽ được hoàn lại kho.')) return;
  try {
    await callApi('PUT', '/api/admin/orders/' + id + '/refund');
    showMessage('Đã hoàn tiền đơn ' + id, 'success');
    loadOrders();
  } catch (err) {
    showMessage(err.message, 'danger');
  }
});

loadOrders();