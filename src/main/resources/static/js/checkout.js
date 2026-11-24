const cartItems = Cart.getItems();

function renderSummary() {
  document.getElementById('summaryBody').innerHTML = cartItems.map(i => `
    <tr>
      <td>${escapeHtml(i.eventName)}</td>
      <td>${escapeHtml(i.ticketName)}</td>
      <td>${i.quantity}</td>
      <td>${formatMoney(i.price * i.quantity)}</td>
    </tr>`).join('');
  document.getElementById('summaryTotal').textContent = formatMoney(Cart.getTotal());
}

async function placeOrder() {
  const btn = document.getElementById('btnPay');
  btn.disabled = true;   // chống bấm 2 lần tạo 2 đơn

  // Bước 1: tạo đơn. Chỉ gửi ticketId và quantity, giá do server tự lấy
  let order;
  try {
    order = await callApi('POST', '/api/orders', {
      items: cartItems.map(i => ({ ticketId: i.ticketId, quantity: i.quantity }))
    });
  } catch (err) {
    showMessage(err.message, 'danger');   // ví dụ: hết vé
    btn.disabled = false;
    return;
  }

  Cart.clear();   // đơn đã tạo thì giỏ không còn cần nữa

  // Bước 2: thanh toán
  try {
    await callApi('POST', '/api/orders/' + order.orderId + '/pay', {
      paymentMethod: document.getElementById('paymentMethod').value
    });
    location.href = '/order-detail.html?id=' + encodeURIComponent(order.orderId);
  } catch (err) {
    showMessage('Đơn ' + order.orderId + ' đã được tạo nhưng thanh toán chưa thành công ('
      + err.message + '). Bạn có thể vào "Đơn hàng của tôi" để xem hoặc hủy đơn.', 'warning');
  }
}

if (cartItems.length === 0) {
  location.href = '/cart.html';
} else {
  renderSummary();
  document.getElementById('btnPay').addEventListener('click', placeOrder);
}