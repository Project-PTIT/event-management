const orderCode = new URLSearchParams(location.search).get('id');

async function loadOrder() {
  if (!orderCode) {
    showMessage('Thiếu mã đơn hàng trên đường dẫn', 'danger');
    return;
  }
  try {
    const o = await callApi('GET', '/api/orders/' + encodeURIComponent(orderCode));

    document.getElementById('orderCode').textContent = o.orderId;
    document.getElementById('orderInfo').innerHTML = `
      <p class="mb-1">Trạng thái: ${statusBadge(o.status)}</p>
      <p class="mb-1">Ngày đặt: ${formatDateTime(o.createdAt)}</p>
      <p class="mb-1">Thanh toán lúc: ${o.paymentAt ? formatDateTime(o.paymentAt) : 'Chưa thanh toán'}</p>
      <p class="fs-5">Tổng tiền: <strong>${formatMoney(o.totalAmount)}</strong></p>`;

    document.getElementById('itemsBody').innerHTML = (o.items || []).map(i => `
      <tr>
        <td>${escapeHtml(i.eventName)}</td>
        <td>${escapeHtml(i.ticketName)}</td>
        <td>${formatMoney(i.price)}</td>
        <td>${i.quantity}</td>
        <td>${formatMoney(i.price * i.quantity)}</td>
      </tr>`).join('');
  } catch (err) {
    showMessage(err.message, 'danger');
  }
}

loadOrder();