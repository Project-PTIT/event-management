// Form tạo / sửa sự kiện của Organizer.
document.addEventListener('DOMContentLoaded', async () => {
  const root = document.getElementById('page-content');
  const eventId = new URLSearchParams(location.search).get('id');

  root.innerHTML = `
    <div class="heading">
      <h1 id="pageTitle">${eventId ? 'Sửa sự kiện' : 'Tạo sự kiện mới'}</h1>
      <a class="btn btn-light" href="/organizer/events.html">← Danh sách</a>
    </div>
    <section class="panel">
      <div id="notice"></div>
      <form id="form">
        <div class="form-grid">
          <div class="field full">
            <label for="title">Tên sự kiện *</label>
            <input id="title" required maxlength="1000">
          </div>
          <div class="field full">
            <label for="imageFile">Ảnh sự kiện * (JPG/PNG/GIF, tối đa 5MB)</label>
            <input id="imageFile" type="file" accept="image/png,image/jpeg,image/gif">
            <input id="image" required maxlength="1000" placeholder="Hoặc dán đường dẫn ảnh https://...">
            <img id="imagePreview" alt="" style="display:none;max-width:320px;margin-top:8px;border-radius:8px">
          </div>
          <div class="field full">
            <label for="desc">Mô tả *</label>
            <textarea id="desc" required maxlength="1000"></textarea>
          </div>
          <div class="field">
            <label for="createType">Chu kỳ</label>
            <select id="createType">
              <option value="">Không lặp</option>
              <option value="WEEKLY">Hàng tuần</option>
              <option value="MONTHLY">Hàng tháng</option>
              <option value="YEARLY">Hàng năm</option>
            </select>
          </div>
        </div>
        <div class="actions">
          <button class="btn btn-primary" id="save" type="submit">Lưu sự kiện</button>
        </div>
      </form>
    </section>`;

  const notice = document.getElementById('notice');
  const fields = ['title', 'image', 'desc', 'createType'];
  const imageInput = document.getElementById('image');
  const preview = document.getElementById('imagePreview');
  function showPreview() {
    const v = imageInput.value.trim();
    preview.style.display = v ? 'block' : 'none';
    if (v) preview.src = v;
  }
  imageInput.addEventListener('change', showPreview);
  document.getElementById('imageFile').addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    notice.innerHTML = '';
    const fd = new FormData();
    fd.append('file', file);
    try {
      const res = await fetch('/api/organizer/uploads/event-image', { method: 'POST', body: fd, credentials: 'same-origin' });
      const json = await res.json().catch(() => null);
      if (!res.ok || !json || !json.success) throw new Error((json && json.message) || 'Tải ảnh thất bại.');
      imageInput.value = json.data.url;
      showPreview();
    } catch (err) {
      notice.innerHTML = `<div class="notice error">${Utils.esc(err.message)}</div>`;
    }
  });

  // Khi sửa: nạp dữ liệu hiện có của sự kiện vào form
  if (eventId) {
    try {
      const event = await API.get('/api/events/' + encodeURIComponent(eventId));
      fields.forEach((key) => {
        if (event[key] !== undefined && event[key] !== null) document.getElementById(key).value = event[key];
      });
      showPreview();
    } catch (err) {
      notice.innerHTML = `<div class="notice error">Không tải được sự kiện: ${Utils.esc(err.message)}</div>`;
    }
  }

  document.getElementById('form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const saveBtn = document.getElementById('save');
    saveBtn.disabled = true;
    notice.innerHTML = '';
    try {
      const payload = {
        title: document.getElementById('title').value.trim(),
        image: document.getElementById('image').value.trim(),
        desc: document.getElementById('desc').value.trim(),
        createType: document.getElementById('createType').value || null
      };
      if (eventId) await API.put('/api/organizer/events/' + encodeURIComponent(eventId), payload);
      else await API.post('/api/organizer/events', payload);
      notice.innerHTML = '<div class="notice success">Lưu sự kiện thành công.</div>';
      setTimeout(() => { location.href = '/organizer/events.html'; }, 700);
    } catch (err) {
      notice.innerHTML = `<div class="notice error">${Utils.esc(err.message)}</div>`;
      saveBtn.disabled = false; // lỗi thì cho thử lại; thành công thì giữ khóa để tránh gửi hai lần
    }
  });
});
