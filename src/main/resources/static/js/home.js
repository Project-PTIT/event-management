// FE2 - Logic Trang chủ (index.html)
document.addEventListener('DOMContentLoaded', async () => {
  const featuredContainer = document.getElementById('featuredEventsContainer');
  const upcomingContainer = document.getElementById('upcomingEventsContainer');
  const artistsContainer = document.getElementById('topArtistsContainer');

  // 1. Tải danh sách sự kiện nổi bật & sự kiện sắp tới
  async function loadHomeEvents() {
    try {
      const res = await FE2_API.fetch('/api/events?page=0&size=10');
      const events = Array.isArray(res) ? res : (res.content || []);

      // Sự kiện nổi bật (featured hoặc 3 sự kiện đầu)
      const featured = events.filter(e => e.featured).slice(0, 3);
      renderFeaturedEvents(featured.length > 0 ? featured : events.slice(0, 3));

      // Sự kiện sắp diễn ra
      renderUpcomingEvents(events.slice(0, 6));
    } catch (err) {
      if (featuredContainer) {
        featuredContainer.innerHTML = `<div class="col-12 alert alert-warning">Không thể nạp sự kiện: ${escapeHtml(err.message)}</div>`;
      }
      if (upcomingContainer) {
        upcomingContainer.innerHTML = `<div class="col-12 alert alert-warning">Không thể nạp sự kiện: ${escapeHtml(err.message)}</div>`;
      }
    }
  }

  // 2. Tải nghệ sĩ tiêu biểu (lấy 6 người đầu tiên)
  async function loadTopArtists() {
    if (!artistsContainer) return;
    try {
      const artists = await FE2_API.fetch('/api/artists');
      const top6 = (artists || []).slice(0, 6);

      artistsContainer.innerHTML = top6.map(a => `
        <div class="col artist-item">
          <a href="/artists.html" class="text-decoration-none text-dark d-block">
            <img src="${escapeHtml(a.image || '/images/artists/den-vau.jpg')}" 
                 alt="${escapeHtml(a.artistName)}" 
                 class="artist-avatar-circle mb-2"
                 onerror="this.onerror=null; this.src='https://placehold.co/200x200/1e293b/ffffff?text=${encodeURIComponent(a.artistName)}';">
            <div class="fw-bold text-truncate px-1">${escapeHtml(a.artistName)}</div>
            <small class="text-muted">${escapeHtml(a.nationality || 'Việt Nam')}</small>
          </a>
        </div>
      `).join('');
    } catch (e) {
      artistsContainer.innerHTML = `<div class="col-12 text-muted">Không thể nạp nghệ sĩ.</div>`;
    }
  }

  function renderFeaturedEvents(events) {
    if (!featuredContainer) return;
    if (!events || events.length === 0) {
      featuredContainer.innerHTML = `<div class="col-12 text-muted">Chưa có sự kiện nổi bật nào.</div>`;
      return;
    }

    featuredContainer.innerHTML = events.map(e => {
      const minPriceText = e.minPrice ? formatCurrency(e.minPrice) : 'Liên hệ';
      const wardName = e.ward?.wardName || 'Hà Nội';
      const organizerName = e.organizer?.name || 'Ban tổ chức';

      return `
        <div class="col">
          <div class="card h-100 shadow-sm event-card border-0">
            <div class="event-img-wrapper">
              <img src="${escapeHtml(e.image)}" 
                   alt="${escapeHtml(e.title)}" 
                   class="event-img"
                   loading="lazy"
                   onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=800&auto=format&fit=crop&q=80';">
              <div class="position-absolute top-0 start-0 m-3">
                ${getCreateTypeBadge(e.createType)}
              </div>
              <div class="position-absolute bottom-0 end-0 m-3">
                <span class="badge bg-dark bg-opacity-75 text-warning fw-semibold px-2 py-1">
                  Từ ${minPriceText}
                </span>
              </div>
            </div>
            <div class="card-body d-flex flex-column p-4">
              <div class="text-muted small mb-1">
                <i class="bi bi-geo-alt-fill text-danger me-1"></i>${escapeHtml(wardName)} &bull; ${escapeHtml(organizerName)}
              </div>
              <h3 class="card-title h5 fw-bold text-dark mb-2 text-truncate" title="${escapeHtml(e.title)}">
                ${escapeHtml(e.title)}
              </h3>
              <p class="card-text text-muted small flex-grow-1" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                ${escapeHtml(e.desc)}
              </p>
              <div class="mt-3 pt-3 border-top d-flex justify-content-between align-items-center">
                <span class="text-success fw-bold">
                  <i class="bi bi-check-circle-fill me-1"></i>Đang mở bán
                </span>
                <a href="/event-detail.html?id=${encodeURIComponent(e.eventId)}" class="btn btn-primary btn-sm rounded-pill px-3">
                  Đặt vé ngay <i class="bi bi-arrow-right ms-1"></i>
                </a>
              </div>
            </div>
          </div>
        </div>
      `;
    }).join('');
  }

  function renderUpcomingEvents(events) {
    if (!upcomingContainer) return;
    if (!events || events.length === 0) {
      upcomingContainer.innerHTML = `<div class="col-12 text-muted">Chưa có sự kiện nào.</div>`;
      return;
    }

    upcomingContainer.innerHTML = events.map(e => {
      const minPriceText = e.minPrice ? formatCurrency(e.minPrice) : 'Từ 300.000 ₫';
      const wardName = e.ward?.wardName || 'Trung tâm';

      return `
        <div class="col">
          <div class="card h-100 shadow-sm event-card">
            <div class="event-img-wrapper">
              <img src="${escapeHtml(e.image)}" 
                   alt="${escapeHtml(e.title)}" 
                   class="event-img"
                   loading="lazy"
                   onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&auto=format&fit=crop&q=80';">
              <div class="position-absolute top-0 end-0 m-2">
                ${getStatusBadge(e.status === 'APPROVED' ? 'Đang mở bán' : e.status)}
              </div>
            </div>
            <div class="card-body d-flex flex-column p-3">
              <div class="text-muted small mb-1">
                <i class="bi bi-geo-alt me-1"></i>${escapeHtml(wardName)}
              </div>
              <h4 class="card-title h6 fw-bold text-dark mb-2 text-truncate" title="${escapeHtml(e.title)}">
                ${escapeHtml(e.title)}
              </h4>
              <p class="card-text text-muted small flex-grow-1" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                ${escapeHtml(e.desc)}
              </p>
              <div class="mt-2 pt-2 border-top d-flex justify-content-between align-items-center">
                <span class="fw-bold text-primary">${minPriceText}</span>
                <a href="/event-detail.html?id=${encodeURIComponent(e.eventId)}" class="btn btn-outline-primary btn-sm rounded-pill">
                  Chi tiết
                </a>
              </div>
            </div>
          </div>
        </div>
      `;
    }).join('');
  }

  // Khởi chạy
  await Promise.all([loadHomeEvents(), loadTopArtists()]);
});
