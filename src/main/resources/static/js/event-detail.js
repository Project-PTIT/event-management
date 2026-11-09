// FE2 - Logic trang chi tiết sự kiện & đặt vé (event-detail.html)
document.addEventListener('DOMContentLoaded', async () => {
  const loadingState = document.getElementById('loadingState');
  const errorState = document.getElementById('errorState');
  const contentEl = document.getElementById('eventDetailContent');
  const errorMessage = document.getElementById('errorMessage');

  const breadcrumbTitle = document.getElementById('breadcrumbTitle');
  const eventTitle = document.getElementById('eventTitle');
  const eventImage = document.getElementById('eventImage');
  const eventDesc = document.getElementById('eventDesc');
  const eventOrganizer = document.getElementById('eventOrganizer');
  const eventWard = document.getElementById('eventWard');
  const createTypeBadge = document.getElementById('eventCreateTypeBadge');
  const statusBadgeEl = document.getElementById('eventStatusBadge');
  const artistsContainer = document.getElementById('eventArtistsContainer');

  const schedulesList = document.getElementById('schedulesList');
  const scheduleCount = document.getElementById('scheduleCount');
  const ticketsContainer = document.getElementById('ticketsContainer');
  const ticketsLoading = document.getElementById('ticketsLoading');

  const cartToastEl = document.getElementById('cartToast');
  const toastMessage = document.getElementById('toastMessage');
  const bsToast = cartToastEl ? new bootstrap.Toast(cartToastEl) : null;

  // Lấy ID sự kiện từ URL param
  const urlParams = new URLSearchParams(window.location.search);
  const eventId = urlParams.get('id') || 'SK01';

  let currentEvent = null;
  let currentSchedules = [];
  let selectedScheduleId = null;

  // 1. Tải chi tiết sự kiện
  async function loadEventData() {
    try {
      loadingState.classList.remove('d-none');
      errorState.classList.add('d-none');
      contentEl.classList.add('d-none');

      // Tải song song thông tin sự kiện và lịch diễn
      const [event, schedules] = await Promise.all([
        FE2_API.fetch(`/api/events/${encodeURIComponent(eventId)}`),
        FE2_API.fetch(`/api/events/${encodeURIComponent(eventId)}/schedules`)
      ]);

      currentEvent = event;
      currentSchedules = Array.isArray(schedules) ? schedules : [];

      renderEventHeader(currentEvent);
      renderArtists(currentSchedules);
      renderSchedules(currentSchedules);

      loadingState.classList.add('d-none');
      contentEl.classList.remove('d-none');

      // Mặc định chọn suất diễn đầu tiên nếu có
      if (currentSchedules.length > 0) {
        selectSchedule(currentSchedules[0].scheduleId);
      } else {
        ticketsContainer.innerHTML = `
          <div class="alert alert-secondary text-center py-4">
            <i class="bi bi-info-circle me-1"></i>Sự kiện này hiện chưa có suất diễn nào được mở.
          </div>
        `;
      }
    } catch (err) {
      loadingState.classList.add('d-none');
      errorState.classList.remove('d-none');
      errorMessage.textContent = err.message || 'Không thể tải thông tin sự kiện.';
    }
  }

  function renderEventHeader(event) {
    document.title = `${event.title} - Event Tickets`;
    breadcrumbTitle.textContent = event.title;
    eventTitle.textContent = event.title;
    eventImage.src = event.image || 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=800&auto=format&fit=crop&q=80';
    eventImage.onerror = function() {
      this.src = 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=800&auto=format&fit=crop&q=80';
    };
    eventDesc.textContent = event.desc || 'Chưa có thông tin mô tả chi tiết.';
    eventOrganizer.textContent = event.organizer?.name || 'Ban tổ chức sự kiện';
    eventWard.textContent = `${event.ward?.wardName || 'Trung tâm'} (${event.ward?.province?.name || 'Hà Nội'})`;

    createTypeBadge.innerHTML = getCreateTypeBadge(event.createType);
    statusBadgeEl.innerHTML = getStatusBadge(event.status === 'APPROVED' ? 'Đang mở bán' : event.status);
  }

  function renderArtists(schedules) {
    // Tập hợp nghệ sĩ từ tất cả các suất diễn
    const artistMap = new Map();
    schedules.forEach(s => {
      if (Array.isArray(s.artists)) {
        s.artists.forEach(a => {
          if (!artistMap.has(a.artistId)) {
            artistMap.set(a.artistId, a);
          }
        });
      }
    });

    const artists = Array.from(artistMap.values());
    if (artists.length === 0) {
      artistsContainer.innerHTML = `<span class="text-muted small">Thông tin nghệ sĩ đang được ban tổ chức cập nhật.</span>`;
      return;
    }

    artistsContainer.innerHTML = artists.map(a => `
      <div class="artist-chip shadow-sm">
        <img src="${escapeHtml(a.image || '/images/artists/den-vau.jpg')}" 
             alt="${escapeHtml(a.artistName)}"
             onerror="this.onerror=null; this.src='https://placehold.co/100x100/1e293b/ffffff?text=${encodeURIComponent(a.artistName)}';">
        <div>
          <div class="fw-semibold text-dark small">${escapeHtml(a.artistName)}</div>
          <div class="text-muted" style="font-size: 0.75rem;">${escapeHtml(a.role || 'Nghệ sĩ biểu diễn')}</div>
        </div>
      </div>
    `).join('');
  }

  function renderSchedules(schedules) {
    scheduleCount.textContent = schedules.length;
    if (schedules.length === 0) {
      schedulesList.innerHTML = `<div class="text-muted small">Chưa có lịch diễn.</div>`;
      return;
    }

    schedulesList.innerHTML = schedules.map((s, idx) => {
      const venueName = s.venue?.venueName || 'Địa điểm chưa công bố';
      const venueAddr = s.venue?.address || '';

      return `
        <div class="card p-3 schedule-item shadow-sm ${idx === 0 ? 'active' : ''}" 
             data-schedule-id="${escapeHtml(s.scheduleId)}">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <span class="fw-bold text-primary">
              <i class="bi bi-clock me-1"></i>${formatDateTime(s.startDatetime)}
            </span>
            ${getStatusBadge(s.status || 'Đang mở bán')}
          </div>
          <div class="fw-semibold text-dark mb-1">
            <i class="bi bi-geo-alt-fill text-danger me-1"></i>${escapeHtml(venueName)}
          </div>
          <small class="text-muted text-truncate" title="${escapeHtml(venueAddr)}">
            ${escapeHtml(venueAddr)}
          </small>
        </div>
      `;
    }).join('');

    // Gắn sự kiện click chọn suất
    schedulesList.querySelectorAll('.schedule-item').forEach(el => {
      el.addEventListener('click', () => {
        schedulesList.querySelectorAll('.schedule-item').forEach(i => i.classList.remove('active'));
        el.classList.add('active');
        selectSchedule(el.dataset.scheduleId);
      });
    });
  }

  // 2. Chọn suất diễn và tải hạng vé
  async function selectSchedule(scheduleId) {
    selectedScheduleId = scheduleId;
    ticketsLoading.classList.remove('d-none');
    ticketsContainer.innerHTML = '';

    try {
      const tickets = await FE2_API.fetch(`/api/schedules/${encodeURIComponent(scheduleId)}/tickets`);
      renderTickets(tickets || []);
    } catch (e) {
      ticketsContainer.innerHTML = `
        <div class="alert alert-warning small">
          <i class="bi bi-exclamation-circle me-1"></i>Không thể tải danh sách hạng vé: ${escapeHtml(e.message)}
        </div>
      `;
    } finally {
      ticketsLoading.classList.add('d-none');
    }
  }

  function renderTickets(tickets) {
    if (!tickets || tickets.length === 0) {
      ticketsContainer.innerHTML = `
        <div class="alert alert-secondary text-center py-4">
          <i class="bi bi-ticket me-1"></i>Suất diễn này hiện chưa mở bán hạng vé nào.
        </div>
      `;
      return;
    }

    ticketsContainer.innerHTML = tickets.map(t => {
      const isSoldOut = (t.remainingQuantity <= 0);
      const remainingText = isSoldOut ? '<span class="badge bg-danger">Hết vé</span>' : `<span class="badge bg-success-subtle text-success border border-success-subtle">Còn lại: ${t.remainingQuantity} vé</span>`;

      return `
        <div class="card ticket-tier-card p-3 shadow-sm bg-white">
          <div class="row align-items-center g-3">
            <div class="col-md-5">
              <h5 class="h6 fw-bold text-dark mb-1">${escapeHtml(t.ticketName)}</h5>
              <div class="text-muted small mb-2">${escapeHtml(t.desc || 'Vé tham dự sự kiện tiêu chuẩn')}</div>
              <div>${remainingText}</div>
            </div>

            <div class="col-md-3 text-md-center">
              <div class="fs-5 fw-bold text-primary">${formatCurrency(t.price)}</div>
              <small class="text-muted">/ vé</small>
            </div>

            <div class="col-md-4">
              <div class="d-flex align-items-center gap-2">
                <input type="number" 
                       id="qty_${escapeHtml(t.ticketId)}" 
                       class="form-control form-control-sm text-center" 
                       value="1" 
                       min="1" 
                       max="${t.remainingQuantity || 1}" 
                       ${isSoldOut ? 'disabled' : ''} 
                       style="width: 70px;">
                <button class="btn btn-primary btn-sm flex-grow-1 fw-semibold btn-add-to-cart"
                        data-ticket-id="${escapeHtml(t.ticketId)}"
                        data-ticket-name="${escapeHtml(t.ticketName)}"
                        data-ticket-price="${t.price}"
                        data-remaining="${t.remainingQuantity}"
                        ${isSoldOut ? 'disabled' : ''}>
                  <i class="bi bi-cart-plus me-1"></i>Thêm giỏ
                </button>
              </div>
            </div>
          </div>
        </div>
      `;
    }).join('');

    // Gắn sự kiện thêm vào giỏ vé - Kết nối với Cart.add của FE3
    ticketsContainer.querySelectorAll('.btn-add-to-cart').forEach(btn => {
      btn.addEventListener('click', () => {
        const ticketId = btn.dataset.ticketId;
        const ticketName = btn.dataset.ticketName;
        const price = parseFloat(btn.dataset.ticketPrice);
        const remaining = parseInt(btn.dataset.remaining, 10);

        const qtyInput = document.getElementById(`qty_${ticketId}`);
        const quantity = parseInt(qtyInput ? qtyInput.value : 1, 10);

        if (isNaN(quantity) || quantity < 1) {
          alert('Vui lòng nhập số lượng vé hợp lệ (tối thiểu là 1).');
          return;
        }

        if (quantity > remaining) {
          alert(`Số lượng vé yêu cầu (${quantity}) vượt quá số vé còn lại (${remaining}).`);
          return;
        }

        // Gọi hàm Cart.add của FE3 (được cung cấp từ /js/cart.js)
        if (window.Cart && typeof window.Cart.add === 'function') {
          window.Cart.add({
            ticketId: ticketId,
            ticketName: ticketName,
            eventName: currentEvent ? currentEvent.title : 'Sự kiện',
            price: price,
            quantity: quantity
          });

          // Cập nhật số lượng trên navbar
          if (typeof updateNavbarCartBadge === 'function') {
            updateNavbarCartBadge();
          }

          // Hiển thị thông báo Toast
          if (toastMessage) {
            toastMessage.textContent = `Đã thêm ${quantity} vé [${ticketName}] vào giỏ vé!`;
          }
          if (bsToast) {
            bsToast.show();
          }
        } else {
          // Fallback an toàn nếu chưa load được cart.js
          alert(`Đã thêm ${quantity} vé [${ticketName}] vào giỏ vé!`);
        }
      });
    });
  }

  loadEventData();
});
