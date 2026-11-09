const CART_KEY = 'eventTicketsCart';

function readCart() {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY)) || [];
  } catch (e) {
    return [];
  }
}

function writeCart(items) {
  localStorage.setItem(CART_KEY, JSON.stringify(items));
}

const Cart = {
  getItems() {
    return readCart();
  },

  add(item) {
    const items = readCart();
    const found = items.find(i => i.ticketId === item.ticketId);
    if (found) {
      found.quantity += item.quantity;
    } else {
      items.push(item);
    }
    writeCart(items);
  },

  setQuantity(ticketId, quantity) {
    const items = readCart();
    const found = items.find(i => i.ticketId === ticketId);
    if (found) {
      found.quantity = quantity;
      writeCart(items);
    }
  },

  remove(ticketId) {
    writeCart(readCart().filter(i => i.ticketId !== ticketId));
  },

  clear() {
    localStorage.removeItem(CART_KEY);
  },

  getTotal() {
    return readCart().reduce((sum, i) => sum + i.price * i.quantity, 0);
  }
};

// ---------- Phần dưới chỉ chạy trên trang cart.html ----------

function renderCartPage() {
  const items = Cart.getItems();
  const empty = document.getElementById('cartEmpty');
  const content = document.getElementById('cartContent');

  if (items.length === 0) {
    empty.classList.remove('d-none');
    content.classList.add('d-none');
    return;
  }
  empty.classList.add('d-none');
  content.classList.remove('d-none');

  document.getElementById('cartBody').innerHTML = items.map(i => `
    <tr>
      <td>${escapeHtml(i.eventName)}<br>
          <small class="text-muted">${escapeHtml(i.ticketName)}</small></td>
      <td>${formatMoney(i.price)}</td>
      <td style="width:120px">
        <input type="number" min="1" class="form-control form-control-sm qty-input"
               data-id="${escapeHtml(i.ticketId)}" value="${i.quantity}">
      </td>
      <td>${formatMoney(i.price * i.quantity)}</td>
      <td><button class="btn btn-sm btn-outline-danger btn-remove"
                  data-id="${escapeHtml(i.ticketId)}">Xóa</button></td>
    </tr>`).join('');

  document.getElementById('cartTotal').textContent = formatMoney(Cart.getTotal());
}

function setupCartPage() {
  const body = document.getElementById('cartBody');

  // Gắn listener lên tbody vì các nút bị tạo lại mỗi lần vẽ
  body.addEventListener('click', function (e) {
    if (e.target.classList.contains('btn-remove')) {
      Cart.remove(e.target.dataset.id);
      renderCartPage();
    }
  });

  body.addEventListener('change', function (e) {
    if (e.target.classList.contains('qty-input')) {
      let q = parseInt(e.target.value, 10);
      if (isNaN(q) || q < 1) q = 1;
      Cart.setQuantity(e.target.dataset.id, q);
      renderCartPage();
    }
  });

  document.getElementById('btnClear').addEventListener('click', function () {
    if (confirm('Xóa toàn bộ giỏ vé?')) {
      Cart.clear();
      renderCartPage();
    }
  });

  renderCartPage();
}

// File này cũng được nạp ở trang của FE2 (không có cartBody), nên phải kiểm tra
if (document.getElementById('cartBody')) {
  setupCartPage();
}