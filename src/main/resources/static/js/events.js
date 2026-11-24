// Logic trang danh sách & tìm kiếm sự kiện (events.html)
document.addEventListener('DOMContentLoaded', async () => {
  const gridContainer = document.getElementById('eventsGrid');
  const countLabel = document.getElementById('totalEventsFound');
  const paginationList = document.getElementById('paginationList');
  const filterForm = document.getElementById('eventFilterForm');
  const keywordInput = document.getElementById('filterKeyword');
  const provinceSelect = document.getElementById('filterProvince');
  const wardSelect = document.getElementById('filterWard');
  const createTypeSelect = document.getElementById('filterCreateType');
  const resetBtn = document.getElementById('btnResetFilter');
  const fromDateInput = document.getElementById('filterFromDate');
  const toDateInput = document.getElementById('filterToDate');
  const typeChips = document.getElementById('typeChips');
  const advPanel = document.getElementById('advFilters');
  const toggleBtn = document.getElementById('btnToggleFilters');
  const filterCount = document.getElementById('filterCount');
  const activeBox = document.getElementById('activeFilters');
  const TYPE_LABELS = { NONE: 'Một lần', WEEKLY: 'Hàng tuần', MONTHLY: 'Hàng tháng', YEARLY: 'Hàng năm' };

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
  if (urlParams.get('fromDate')) fromDateInput.value = urlParams.get('fromDate');
  if (urlParams.get('toDate')) toDateInput.value = urlParams.get('toDate');

  // Bật/tắt bảng bộ lọc nâng cao
  function setAdvOpen(open) {
    advPanel.hidden = !open;
    toggleBtn.setAttribute('aria-expanded', String(open));
  }
  toggleBtn.addEventListener('click', () => setAdvOpen(advPanel.hidden));

  // Chip chu kỳ <-> ô chọn ẩn (giữ nguyên giá trị cũ mà phần còn lại của file đọc)
  function syncChips() {
    typeChips.querySelectorAll('.ev-chip').forEach((c) => c.classList.toggle('active', c.dataset.type === createTypeSelect.value));
  }
  typeChips.addEventListener('click', (e) => {
    const chip = e.target.closest('.ev-chip');
    if (!chip) return;
    createTypeSelect.value = chip.dataset.type;
    syncChips();
    fetchEvents(0);
  });

  // Hiện các bộ lọc đang áp dụng dưới dạng thẻ, bấm × để bỏ từng bộ lọc
  function renderActiveFilters() {
    const tags = [];
    const provinceName = provinceSelect.value && provinceSelect.options[provinceSelect.selectedIndex].textContent;
    const wardName = wardSelect.value && wardSelect.options[wardSelect.selectedIndex].textContent;
    if (keywordInput.value.trim()) tags.push({ key: 'keyword', text: 'Từ khóa: ' + keywordInput.value.trim() });
    if (createTypeSelect.value) tags.push({ key: 'type', text: 'Chu kỳ: ' + (TYPE_LABELS[createTypeSelect.value] || createTypeSelect.value) });
    if (provinceName) tags.push({ key: 'province', text: provinceName });
    if (wardName) tags.push({ key: 'ward', text: wardName });
    if (fromDateInput.value) tags.push({ key: 'from', text: 'Từ ' + formatDate(fromDateInput.value) });
    if (toDateInput.value) tags.push({ key: 'to', text: 'Đến ' + formatDate(toDateInput.value) });

    activeBox.innerHTML = tags.map((t) =>
      `<span class="ev-tag">${escapeHtml(t.text)}<button type="button" data-remove="${t.key}" aria-label="Bỏ bộ lọc">&times;</button></span>`
    ).join('');

    // Số bộ lọc nâng cao đang bật (hiện trên nút "Bộ lọc")
    const advCount = [provinceSelect.value, wardSelect.value, fromDateInput.value, toDateInput.value].filter(Boolean).length;
    filterCount.textContent = advCount;
    filterCount.classList.toggle('d-none', advCount === 0);
  }

  activeBox.addEventListener('click', async (e) => {
    const btn = e.target.closest('[data-remove]');
    if (!btn) return;
    const key = btn.dataset.remove;
    if (key === 'keyword') keywordInput.value = '';
    if (key === 'type') { createTypeSelect.value = ''; syncChips(); }
    if (key === 'from') fromDateInput.value = '';
    if (key === 'to') toDateInput.value = '';
    if (key === 'ward') wardSelect.value = '';
    if (key === 'province') { provinceSelect.value = ''; await loadWards('', null); }
    fetchEvents(0);
  });

  // 1a. Tải danh sách Tỉnh/Thành phố (GET /api/provinces)
  async function loadProvinces() {
    try {
      const provinces = await FE2_API.fetch('/api/provinces');
      if (Array.isArray(provinces)) {
        provinces.forEach(p => {
          const opt = document.createElement('option');
          opt.value = p.provinceId;
          opt.textContent = p.provinceName;
          if (urlParams.get('provinceId') === p.provinceId) {
            opt.selected = true;
          }
          provinceSelect.appendChild(opt);
        });
      }
    } catch (e) {
      console.warn('Không tải được danh mục tỉnh/thành:', e);
    }
  }

  // 1b. Tải danh sách Phường theo tỉnh/thành đang chọn (GET /api/wards?provinceId=...).
  // Chưa chọn tỉnh thì tải tất cả phường. Luôn xóa lựa chọn phường cũ trước khi nạp lại.
  async function loadWards(provinceId, selectedWardId) {
    wardSelect.innerHTML = '<option value="">-- Tất cả phường --</option>';
    try {
      const url = provinceId ? '/api/wards?provinceId=' + encodeURIComponent(provinceId) : '/api/wards';
      const wards = await FE2_API.fetch(url);
      if (Array.isArray(wards)) {
        wards.forEach(w => {
          const opt = document.createElement('option');
          opt.value = w.wardId;
          // Khi chưa chọn tỉnh, ghi kèm tên tỉnh để phân biệt các phường cùng tên
          opt.textContent = provinceId ? w.wardName : `${w.wardName} (${w.provinceName})`;
          if (selectedWardId && selectedWardId === w.wardId) {
            opt.selected = true;
          }
          wardSelect.appendChild(opt);
        });
      }
    } catch (e) {
      console.warn('Không tải được danh mục phường:', e);
    }
  }

  // Đổi tỉnh/thành: bỏ lựa chọn phường cũ và tải phường của tỉnh mới
  provinceSelect.addEventListener('change', async () => {
    await loadWards(provinceSelect.value, null);
    fetchEvents(0);
  });
  wardSelect.addEventListener('change', () => fetchEvents(0));
  fromDateInput.addEventListener('change', () => fetchEvents(0));
  toDateInput.addEventListener('change', () => fetchEvents(0));

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
    if (provinceSelect.value) params.set('provinceId', provinceSelect.value);
    if (wardSelect.value) params.set('wardId', wardSelect.value);
    if (createTypeSelect.value) params.set('createType', createTypeSelect.value);
    if (fromDateInput.value) params.set('fromDate', fromDateInput.value);
    if (toDateInput.value) params.set('toDate', toDateInput.value);
    renderActiveFilters();
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
        <div class="col-12 ev-empty">
          <div class="ev-empty-icon"><i class="bi bi-search"></i></div>
          <h4>Không tìm thấy sự kiện nào</h4>
          <p class="small mb-3">Hãy thử từ khóa khác hoặc nới lỏng bộ lọc.</p>
          <button type="button" class="ev-btn ev-btn-outline ev-btn-sm" id="btnEmptyReset">Xóa bộ lọc</button>
        </div>`;
      document.getElementById('btnEmptyReset').addEventListener('click', () => resetBtn.click());
      return;
    }

    gridContainer.innerHTML = events.map(e => {
      // Backend trả organizerName (phẳng). Giá vé nằm ở từng hạng vé nên không hiển thị giá ở danh sách
      const organizerName = e.organizerName || 'Ban tổ chức';
      // minPrice do backend tính từ các hạng vé còn mở bán; chưa có thì ghi "Sắp mở bán" (không dùng giá giả)
      const priceText = e.minPrice != null ? 'Từ ' + formatCurrency(e.minPrice) : 'Sắp mở bán';
      return `
        <div class="col">
          <article class="ev-event-card">
            <a class="ev-event-media" href="/event-detail.html?id=${encodeURIComponent(e.eventId)}" tabindex="-1" aria-hidden="true">
              <img src="${escapeHtml(e.image)}" alt="" loading="lazy"
                   onerror="this.onerror=null; this.src='/images/placeholder-event.svg';">
              <span class="ev-event-tag">${getCreateTypeBadge(e.createType)}</span>
            </a>
            <div class="ev-event-body">
              <div class="ev-event-org"><i class="bi bi-building me-1"></i>${escapeHtml(organizerName)}</div>
              <h3 class="ev-event-title" title="${escapeHtml(e.title)}">${escapeHtml(e.title)}</h3>
              <p class="ev-event-desc">${escapeHtml(e.desc)}</p>
              <div class="ev-event-foot">
                <span class="ev-event-price">${escapeHtml(priceText)}</span>
                <a href="/event-detail.html?id=${encodeURIComponent(e.eventId)}" class="ev-btn ev-btn-primary ev-btn-sm">Xem chi tiết</a>
              </div>
            </div>
          </article>
        </div>`;
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
    provinceSelect.value = '';
    createTypeSelect.value = '';
    fromDateInput.value = '';
    toDateInput.value = '';
    syncChips();
    loadWards('', null).then(() => fetchEvents(0));
  });

  await loadProvinces();
  await loadWards(urlParams.get('provinceId') || '', urlParams.get('wardId'));
  syncChips();
  // Mở sẵn bộ lọc nâng cao nếu URL đã có tỉnh/phường/ngày
  if (provinceSelect.value || urlParams.get('wardId') || fromDateInput.value || toDateInput.value) setAdvOpen(true);
  await fetchEvents(currentPage);
});
