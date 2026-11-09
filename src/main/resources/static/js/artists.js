// FE2 - Logic trang danh sách nghệ sĩ (artists.html)
document.addEventListener('DOMContentLoaded', () => {
  let allArtists = [];

  const container = document.getElementById('artistsContainer');
  const countEl = document.getElementById('artistCount');
  const searchInput = document.getElementById('artistSearchInput');
  const messageEl = document.getElementById('artistMessage');

  async function loadArtists() {
    try {
      allArtists = await FE2_API.fetch('/api/artists');
      renderArtists(allArtists);
    } catch (err) {
      container.innerHTML = `
        <div class="col-12 text-center py-5">
          <div class="alert alert-danger d-inline-block">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>Không thể tải danh sách nghệ sĩ: ${escapeHtml(err.message)}
          </div>
        </div>
      `;
    }
  }

  function renderArtists(artists) {
    countEl.textContent = artists.length;

    if (!artists || artists.length === 0) {
      container.innerHTML = `
        <div class="col-12 text-center py-5">
          <i class="bi bi-people text-muted" style="font-size: 3rem;"></i>
          <p class="mt-3 text-muted">Không tìm thấy nghệ sĩ phù hợp.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = artists.map(artist => {
      // Đường dẫn ảnh từ thư mục static hoặc url fallback
      const imgPath = artist.image || '/images/artists/den-vau.jpg';

      return `
        <div class="col">
          <div class="card h-100 shadow-sm artist-card">
            <div class="artist-img-wrapper">
              <img src="${escapeHtml(imgPath)}" 
                   alt="${escapeHtml(artist.artistName)}" 
                   class="artist-img"
                   loading="lazy"
                   onerror="this.onerror=null; this.src='https://placehold.co/400x400/1e293b/ffffff?text=${encodeURIComponent(artist.artistName)}';">
            </div>
            <div class="card-body d-flex flex-column text-center p-3">
              <div class="mb-2">
                <span class="badge text-bg-light border text-secondary px-2 py-1">
                  <i class="bi bi-geo-alt-fill text-danger me-1"></i>${escapeHtml(artist.nationality || 'Việt Nam')}
                </span>
              </div>
              <h3 class="card-title h5 fw-bold text-dark mb-2">${escapeHtml(artist.artistName)}</h3>
              <p class="card-text text-muted small flex-grow-1" style="display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;">
                ${escapeHtml(artist.bio || 'Chưa có thông tin tiểu sử chi tiết.')}
              </p>
              <div class="mt-3 pt-2 border-top">
                <a href="/events.html?keyword=${encodeURIComponent(artist.artistName)}" class="btn btn-outline-primary btn-sm w-100 rounded-pill">
                  <i class="bi bi-calendar-event me-1"></i>Xem sự kiện
                </a>
              </div>
            </div>
          </div>
        </div>
      `;
    }).join('');
  }

  // Lọc theo từ khóa tìm kiếm
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      const q = e.target.value.toLowerCase().trim();
      const filtered = allArtists.filter(a => 
        (a.artistName && a.artistName.toLowerCase().includes(q)) ||
        (a.nationality && a.nationality.toLowerCase().includes(q)) ||
        (a.bio && a.bio.toLowerCase().includes(q))
      );
      renderArtists(filtered);
    });
  }

  loadArtists();
});
