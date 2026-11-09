
document.addEventListener('DOMContentLoaded', async () => {
    const id = new URLSearchParams(location.search).get('id');
    const root = document.getElementById('page-content');

    root.innerHTML = `
    <div class="heading">
        <div>
            <h1 id="title">${id ? 'Sửa sự kiện' : 'Tạo sự kiện mới'}</h1>
            <div class="muted">Sự kiện mới sẽ được gửi Admin duyệt với trạng thái PENDING.</div>
        </div>
        <a class="btn btn-light" href="/organizer/events.html">← Danh sách</a>
    </div>
    <section class="panel">
        <div id="notice"></div>
        <form id="form">
            <div class="form-grid">
                <div class="field full">
                    <label for="eventName">Tên sự kiện *</label>
                    <input id="eventName" maxlength="1000" required>
                </div>
                <div class="field full">
                    <label for="image">Đường dẫn ảnh *</label>
                    <input id="image" maxlength="1000" required>
                </div>
                <div class="field full">
                    <label for="description">Mô tả *</label>
                    <textarea id="description" maxlength="1000" required></textarea>
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
            <div class="row-between">
                <span class="muted">Các trường có dấu * là bắt buộc.</span>
                <button class="btn btn-primary" id="save" type="submit">Lưu sự kiện</button>
            </div>
        </form>
    </section>`;

    const notice = document.getElementById('notice');

    function fill(e) {
        document.getElementById('eventName').value = e.title || '';
        document.getElementById('image').value = e.image || '';
        document.getElementById('description').value = e.desc || '';
        document.getElementById('createType').value = e.createType || '';
    }

    if (id) {
        try {
            const e = await API.get('/api/events/' + encodeURIComponent(id));
            fill(e);
        } catch (err) {
            notice.innerHTML = `<div class="notice error">
                Không tải được sự kiện: ${Utils.esc(err.message)}
            </div>`;
        }
    }

    document.getElementById('form').onsubmit = async ev => {
        ev.preventDefault();

        const btn = document.getElementById('save');
        btn.disabled = true;
        notice.innerHTML = '';

        const payload = {
            title: document.getElementById('eventName').value.trim(),
            image: document.getElementById('image').value.trim(),
            desc: document.getElementById('description').value.trim(),
            createType: document.getElementById('createType').value || null
        };

        try {
            if (id) {
                await API.put('/api/organizer/events/' + encodeURIComponent(id), payload);
            } else {
                await API.post('/api/organizer/events', payload);
            }

            notice.innerHTML = '<div class="notice success">Lưu sự kiện thành công.</div>';
            setTimeout(() => location.href = '/organizer/events.html', 700);

        } catch (err) {
            notice.innerHTML = `<div class="notice error">${Utils.esc(err.message)}</div>`;
        } finally {
            btn.disabled = false;
        }
    };
});
