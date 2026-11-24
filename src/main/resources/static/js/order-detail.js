const orderCode = new URLSearchParams(location.search).get('id');
const PAYMENT_METHODS = ['Chuyển khoản', 'Thẻ', 'Ví điện tử'];

// Server trả 'yyyy-MM-dd HH:mm:ss'; đổi sang Date để so sánh
function parseServerDate(s) {
  return s ? new Date(String(s).replace(' ', 'T')) : null;
}

function actionsHtml(o) {
  // Chờ thanh toán: thanh toán tiếp hoặc hủy đơn
  if (o.status === 'Chờ thanh toán') {
    return `
      <div class="card card-body border-warning">
        <p class="mb-2">Đơn đang giữ chỗ cho bạn đến <strong>${formatDateTime(o.expiresAt)}</strong>.
          Quá thời gian này đơn sẽ tự hủy và vé được trả lại.</p>
        <div class="d-flex flex-wrap gap-2 align-items-center">
          <select id="payMethod" class="form-select" style="max-width:220px">
            ${PAYMENT_METHODS.map(m => `<option>${escapeHtml(m)}</option>`).join('')}
          </select>
          <button id="btnPayNow" class="btn btn-primary">Thanh toán ngay</button>
          <button id="btnCancelOrder" class="btn btn-outline-danger">Hủy đơn</button>
        </div>
        <div class="small text-muted mt-2">Chế độ demo: thanh toán được giả lập, không trừ tiền thật.</div>
      </div>`;
  }

  // Đã thanh toán: gửi yêu cầu hoàn tiền (nếu còn hạn)
  if (o.status === 'Đã thanh toán') {
    const deadline = parseServerDate(o.refundableUntil);
    const canRefund = deadline && new Date() < deadline;
    const rejected = o.refundResponse
      ? `<div class="alert alert-warning">Yêu cầu hoàn tiền trước đó đã bị từ chối. Phản hồi từ quản trị viên:
           <strong>${escapeHtml(o.refundResponse)}</strong></div>` : '';
    if (!canRefund) {
      return rejected + `<div class="text-muted small">Đơn đã quá hạn yêu cầu hoàn tiền
        (phải gửi trước giờ diễn sớm nhất ít nhất 24 giờ).</div>`;
    }
    return rejected + `
      <div class="card card-body" id="refund">
        <h6 class="mb-2">Yêu cầu hoàn tiền</h6>
        <p class="small text-muted mb-2">Bạn có thể gửi yêu cầu đến <strong>${formatDateTime(o.refundableUntil)}</strong>.
          Quản trị viên sẽ xem xét và duyệt hoặc từ chối.</p>
        <textarea id="refundReason" class="form-control mb-2" rows="3" maxlength="500"
                  placeholder="Nhập lý do hoàn tiền (ít nhất 5 ký tự)"></textarea>
        <div><button id="btnRequestRefund" class="btn btn-outline-danger">Gửi yêu cầu hoàn tiền</button></div>
      </div>`;
  }

  // Chờ hoàn tiền: hiển thị yêu cầu đã gửi, cho rút lại
  if (o.status === 'Chờ hoàn tiền') {
    return `
      <div class="alert alert-primary mb-0">
        <p class="mb-1"><strong>Yêu cầu hoàn tiền đang chờ quản trị viên xử lý.</strong></p>
        <p class="mb-1">Gửi lúc: ${formatDateTime(o.refundRequestedAt)}</p>
        <p class="mb-2">Lý do: ${escapeHtml(o.refundReason)}</p>
        <button id="btnWithdrawRefund" class="btn btn-sm btn-outline-secondary">Rút yêu cầu</button>
      </div>`;
  }

  if (o.status === 'Đã hoàn tiền') {
    return `<div class="alert alert-info mb-0">Đơn đã được hoàn tiền.
      ${o.refundResponse ? 'Ghi chú: <strong>' + escapeHtml(o.refundResponse) + '</strong>' : ''}</div>`;
  }
  return '';
}

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
      <p class="mb-1">Thanh toán lúc: ${o.paymentAt ? formatDateTime(o.paymentAt) : 'Chưa thanh toán'}
        ${o.paymentMethod ? '(' + escapeHtml(o.paymentMethod) + ')' : ''}</p>
      <p class="fs-5">Tổng tiền: <strong>${formatMoney(o.totalAmount)}</strong></p>`;

    document.getElementById('orderActions').innerHTML = actionsHtml(o);

    document.getElementById('itemsBody').innerHTML = (o.details || o.items || []).map(i => `
      <tr>
        <td>${escapeHtml(i.eventName)}</td>
        <td>${escapeHtml(i.ticketName)}</td>
        <td>${formatMoney(i.price)}</td>
        <td>${i.quantity}</td>
        <td>${formatMoney(i.price * i.quantity)}</td>
      </tr>`).join('');

    if (location.hash === '#refund') {
      const box = document.getElementById('refund');
      if (box) box.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  } catch (err) {
    showMessage(err.message, 'danger');
  }
}

// Chạy một thao tác, khóa nút trong lúc chờ, xong thì tải lại đơn
async function runAction(btn, successText, fn) {
  btn.disabled = true;
  try {
    await fn();
    await loadOrder();
    showMessage(successText, 'success');
  } catch (err) {
    showMessage(err.message, 'danger');
    btn.disabled = false;
  }
}

document.getElementById('orderActions').addEventListener('click', function (e) {
  const btn = e.target.closest('button');
  if (!btn) return;
  const id = encodeURIComponent(orderCode);

  if (btn.id === 'btnPayNow') {
    const method = document.getElementById('payMethod').value;
    runAction(btn, 'Thanh toán thành công.',
      () => callApi('POST', '/api/orders/' + id + '/pay', { paymentMethod: method }));
  } else if (btn.id === 'btnCancelOrder') {
    if (!confirm('Bạn chắc chắn muốn hủy đơn này?')) return;
    runAction(btn, 'Đã hủy đơn.', () => callApi('PUT', '/api/orders/' + id + '/cancel'));
  } else if (btn.id === 'btnRequestRefund') {
    const reason = document.getElementById('refundReason').value.trim();
    if (reason.length < 5) { showMessage('Vui lòng nhập lý do hoàn tiền (ít nhất 5 ký tự).', 'warning'); return; }
    if (!confirm('Gửi yêu cầu hoàn tiền cho đơn này?')) return;
    runAction(btn, 'Đã gửi yêu cầu hoàn tiền, vui lòng chờ quản trị viên xử lý.',
      () => callApi('POST', '/api/orders/' + id + '/refund-request', { reason: reason }));
  } else if (btn.id === 'btnWithdrawRefund') {
    if (!confirm('Rút lại yêu cầu hoàn tiền?')) return;
    runAction(btn, 'Đã rút yêu cầu hoàn tiền.',
      () => callApi('PUT', '/api/orders/' + id + '/refund-request/withdraw'));
  }
});

loadOrder();
