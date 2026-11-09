// FE2 - Logic trang danh sách & tìm kiếm sự kiện (events.html)
document.addEventListener('DOMContentLoaded', async () => {
  const gridContainer = document.getElementById('eventsGrid');
  const countLabel = document.getElementById('totalEventsFound');
  const paginationList = document.getElementById('paginationList');
  const filterForm = document.getElementById('eventFilterForm');
  const keywordInput = document.getElementById('filterKeyword');
  const wardSelect = document.getElementById('filterWard');
  const createTypeSelect = document.getElementById('filterCreateType');
  const resetBtn = document.getElementById('btnResetFilter');

  // Đọc params từ URL
  const urlParams = new URLSearchParams(window.location.search);
  let currentPage = parseInt(urlParams.get('page') || 0, 10);
  const pageSize = 6;

  if (urlParams.get('keyword')) {
    keywordInput.value = urlParams.get('keyword');
  }
  if (urlParams.get('createType')) {
    createTypeSelect.value = urlParams.get('createType');
  }

  // 1. Tải danh sách Ward
  async function loadWards() {
    try {
      const wards = await FE2_API.fetch('/api/wards');
      if (Array.isArray(wards)) {
        wards.forEach(w => {
          const opt = document.createElement('option');
          opt.value = w.wardId;
          opt.textContent = `${w.wardName} (${w.provinceName || 'Hà Nội'})`;
          if (urlParams.get('wardId') === w.wardId) {
            opt.selected = true;
          }
          wardSelect.appendChild(opt);
        });
      }
    } catch (e) {
      console.warn('Không tải được danh mục phường:', e);
    }
  }

  // 2. Tải sự kiện theo bộ lọc
  async function fetchEvents(page = 0) {
    currentPage = page;
    gridContainer.innerHTML = `
      <div class="col-12 text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="mt-2 text-muted">Đang tìm kiếm sự kiện...</p>
      </div>
    `;

    const params = new URLSearchParams();
    if (keywordInput.value.trim()) params.set('keyword', keywordInput.value.trim());
    if (wardSelect.value) params.set('wardId', wardSelect.value);
    if (createTypeSelect.value) params.set('createType', createTypeSelect.value);
    params.set('page', currentPage);
    params.set('size', pageSize);

    // Cập nhật URL trình duyệt không reload để người dùng lưu bookmark / chia sẻ link
    const newRelativePathQuery = window.location.pathname + '?' + params.toString();
    history.replaceState(null, '', newRelativePathQuery);

    try {
      const res = await FE2_API.fetch('/api/events?' + params.toString());
      const events = Array.isArray(res) ? res : (res.content || []);
      const totalElements = res.totalElements !== undefined ? res.totalElements : events.length;
      const totalPages = res.totalPages !== undefined ? res.totalPages : Math.ceil(totalElements / pageSize);

      countLabel.textContent = totalElements;
      renderEvents(events);
      renderPagination(totalPages, currentPage);
    } catch (err) {
      gridContainer.innerHTML = `
        <div class="col-12 text-center py-5">
          <div class="alert alert-danger d-inline-block">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>Lỗi tải sự kiện: ${escapeHtml(err.message)}
          </div>
        </div>
      `;
      countLabel.textContent = '0';
      paginationList.innerHTML = '';
    }
  }

  function renderEvents(events) {
    if (!events || events.length === 0) {
      gridContainer.innerHTML = `
        <div class="col-12 text-center py-5">
          <i class="bi bi-search text-muted" style="font-size: 3rem;"></i>
          <h4 class="h5 mt-3 text-dark">Không tìm thấy sự kiện nào</h4>
          <p class="text-muted small">Vui lòng thử tìm với từ khóa khác hoặc điều chỉnh lại bộ lọc.</p>
        </div>
      `;
      return;
    }

    gridContainer.innerHTML = events.map(e => {
      const minPriceText = e.minPrice ? formatCurrency(e.minPrice) : 'Từ 300.000 ₫';
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
                <span class="text-success small fw-bold">
                  <i class="bi bi-shield-check me-1"></i>Chính thức
                </span>
                <a href="/event-detail.html?id=${encodeURIComponent(e.eventId)}" class="btn btn-primary btn-sm rounded-pill px-3">
                  Xem chi tiết <i class="bi bi-arrow-right ms-1"></i>
                </a>
              </div>
            </div>
          </div>
        </div>
      `;
    }).join('');
  }

  function renderPagination(totalPages, activePage) {
    if (totalPages <= 1) {
      paginationList.innerHTML = '';
      return;
    }

    let html = `
      <li class="page-item ${activePage === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#" data-page="${activePage - 1}" aria-label="Previous">
          <span aria-hidden="true">&laquo;</span>
        </a>
      </li>
    `;

    for (let p = 0; p < totalPages; p++) {
      html += `
        <li class="page-item ${p === activePage ? 'active' : ''}">
          <a class="page-link" href="#" data-page="${p}">${p + 1}</a>
        </li>
      `;
    }

    html += `
      <li class="page-item ${activePage === totalPages - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" data-page="${activePage + 1}" aria-label="Next">
          <span aria-hidden="true">&raquo;</span>
        </a>
      </li>
    `;

    paginationList.innerHTML = html;

    paginationList.querySelectorAll('.page-link').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        const p = parseInt(btn.dataset.page, 10);
        if (!isNaN(p) && p >= 0 && p < totalPages && p !== activePage) {
          fetchEvents(p);
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
      });
    });
  }

  // Sự kiện Form submit
  filterForm.addEventListener('submit', (e) => {
    e.preventDefault();
    fetchEvents(0);
  });

  // Nút đặt lại bộ lọc
  resetBtn.addEventListener('click', () => {
    keywordInput.value = '';
    wardSelect.value = '';
    createTypeSelect.value = '';
    fetchEvents(0);
  });

  await loadWards();
  await fetchEvents(currentPage);
});
