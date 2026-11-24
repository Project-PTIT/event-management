const MOCK_ORDERS = [
  { orderId: 'DH001', status: 'Chờ thanh toán', totalAmount: 1000000,
    createdAt: '2026-10-20T09:30:00', paymentAt: null,
    items: [{ ticketId: 'V001', ticketName: 'VIP', eventName: 'Đêm nhạc Mùa Thu', price: 500000, quantity: 2 }] },
  { orderId: 'DH002', status: 'Đã thanh toán', totalAmount: 300000,
    createdAt: '2026-10-18T14:00:00', paymentAt: '2026-10-18T14:05:00',
    items: [{ ticketId: 'V002', ticketName: 'Thường', eventName: 'Lễ hội Ẩm thực', price: 150000, quantity: 2 }] },
  { orderId: 'DH003', status: 'Đã hủy', totalAmount: 500000,
    createdAt: '2026-10-10T08:00:00', paymentAt: null,
    items: [{ ticketId: 'V001', ticketName: 'VIP', eventName: 'Đêm nhạc Mùa Thu', price: 500000, quantity: 1 }] }
];

const MOCK_EVENTS = [
  { eventId: 'SK01', eventName: 'Đêm nhạc Mùa Thu', organizerName: 'Công ty ABC', createType: null, status: 'PENDING' },
  { eventId: 'SK02', eventName: 'Lễ hội Ẩm thực', organizerName: 'Công ty XYZ', createType: 'YEARLY', status: 'APPROVED' },
  { eventId: 'SK03', eventName: 'Hội chợ Sách', organizerName: 'Công ty ABC', createType: 'MONTHLY', status: 'REJECTED' }
];

function mockApi(method, url, body) {
  return new Promise(function (resolve, reject) {
    setTimeout(function () {
      try { resolve(handleMock(method, url, body)); } catch (e) { reject(e); }
    }, 300);
  });
}

function handleMock(method, url, body) {
  if (method === 'GET' && url.startsWith('/api/orders/me')) {
    const status = new URLSearchParams(url.split('?')[1] || '').get('status');
    return status ? MOCK_ORDERS.filter(o => o.status === status) : MOCK_ORDERS;
  }

  const m = url.match(/^\/api\/orders\/(\w+)$/);
  if (method === 'GET' && m) return MOCK_ORDERS.find(o => o.orderId === m[1]);

  if (method === 'POST' && url === '/api/orders') return MOCK_ORDERS[0];

  // Admin: danh sách tất cả đơn, kèm tên khách hàng (giả định, cần hỏi BE5 tên trường thật)
  if (method === 'GET' && url.startsWith('/api/admin/orders')) {
    const names = { DH001: 'Nguyễn Văn An', DH002: 'Trần Thị Bình', DH003: 'Lê Văn Cường' };
    const status = new URLSearchParams(url.split('?')[1] || '').get('status');
    const list = status ? MOCK_ORDERS.filter(o => o.status === status) : MOCK_ORDERS;
    return list.map(o => Object.assign({}, o, { customerName: names[o.orderId] || '' }));
  }

  // Hoàn tiền: chỉ nhận đơn "Đã thanh toán", giống quy tắc của BE5
  const refund = url.match(/^\/api\/admin\/orders\/(\w+)\/refund$/);
  if (method === 'PUT' && refund) {
    const order = MOCK_ORDERS.find(o => o.orderId === refund[1]);
    if (!order || order.status !== 'Đã thanh toán') {
      throw new Error('Chỉ hoàn tiền cho đơn đã thanh toán');
    }
    order.status = 'Đã hoàn tiền';
    return null;
  }

  if (method === 'GET' && url.startsWith('/api/admin/events')) {
    const status = new URLSearchParams(url.split('?')[1] || '').get('status');
    return status ? MOCK_EVENTS.filter(e => e.status === status) : MOCK_EVENTS;
  }

  const act = url.match(/^\/api\/admin\/events\/(\w+)\/(approve|reject)$/);
  if (method === 'PUT' && act) {
    const ev = MOCK_EVENTS.find(e => e.eventId === act[1]);
    if (!ev || ev.status !== 'PENDING') {
      throw new Error('Chỉ duyệt hoặc từ chối sự kiện đang PENDING');
    }
    ev.status = act[2] === 'approve' ? 'APPROVED' : 'REJECTED';
    return null;
  }

  if (/\/(pay|cancel)$/.test(url)) return null;

  throw new Error('Mock chưa có: ' + method + ' ' + url);
}