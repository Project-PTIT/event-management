
document.addEventListener('DOMContentLoaded', async () => {
    const root = document.getElementById('page-content');
    const params = new URLSearchParams(location.search);
    let eventId = params.get('eventId');
    let schedules = [];

    root.innerHTML = `
    <div class="heading">
        <div>
            <h1>Suất diễn & nghệ sĩ</h1>
            <div class="muted">Quản lý suất diễn và gán nghệ sĩ.</div>
        </div>
        <a class="btn btn-light" href="/organizer/events.html">← Sự kiện</a>
    </div>
    <section class="panel">
        <div id="notice"></div>
        <div class="toolbar">
            <label class="field" style="min-width:260px;flex:1">
                <span>Sự kiện</span>
                <select id="eventSelect"></select>
            </label>
            <button id="newSchedule" class="btn btn-primary">＋ Thêm suất diễn</button>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Suất diễn</th>
                        <th>Bắt đầu</th>
                        <th>Kết thúc</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody id="rows"></tbody>
            </table>
        </div>
    </section>

    <section class="panel" id="editor">
        <h2 id="editorTitle">Tạo suất diễn</h2>
        <form id="scheduleForm">
            <input id="scheduleId" type="hidden">
            <div class="form-grid">
                <div class="field">
                    <label>Địa điểm *</label>
                    <select id="venueId" required></select>
                </div>
                <div class="field">
                    <label>Thời gian bắt đầu *</label>
                    <input id="startDatetime" type="datetime-local" required>
                </div>
                <div class="field">
                    <label>Thời gian kết thúc *</label>
                    <input id="endDatetime" type="datetime-local" required>
                </div>
            </div>
            <div class="row-between">
                <span class="muted">Thời gian kết thúc phải sau thời gian bắt đầu.</span>
                <button class="btn btn-primary">Lưu suất diễn</button>
            </div>
        </form>
    </section>

    <section class="panel">
        <h2>Gán nghệ sĩ</h2>
        <form id="artistForm" class="form-grid">
            <div class="field">
                <label>Suất diễn *</label>
                <select id="artistSchedule" required></select>
            </div>
            <div class="field">
                <label>Nghệ sĩ *</label>
                <select id="artistId" required></select>
            </div>
            <div class="field">
                <label>Vai trò</label>
                <input id="artistRole" placeholder="Ví dụ: Ca sĩ chính">
            </div>
            <div class="field" style="align-self:end">
                <button class="btn btn-primary">Gán nghệ sĩ</button>
            </div>
        </form>
    </section>`;

    const notice = document.getElementById('notice');
    const rows = document.getElementById('rows');
    const select = document.getElementById('eventSelect');

    const msg = (s, err = false) => {
        notice.innerHTML = `<div class="notice ${err ? 'error' : 'success'}">${Utils.esc(s)}</div>`;
    };

    // Tải sự kiện
    try {
        const events = Utils.array(await API.get('/api/organizer/events'));

        select.innerHTML = '<option value="">Chọn sự kiện</option>' +
            events.map(e => `
                <option value="${Utils.esc(e.eventId)}">
                    ${Utils.esc(e.title)}
                </option>
            `).join('');

        if (eventId) select.value = eventId;
    } catch (e) {
        msg('Không tải được sự kiện: ' + e.message, true);
    }

    // Tải địa điểm
    try {
        const venues = Utils.array(await API.get('/api/venues'));

        document.getElementById('venueId').innerHTML =
            '<option value="">Chọn địa điểm</option>' +
            venues.map(v => `
                <option value="${Utils.esc(v.id)}">
                    ${Utils.esc(v.venueName)}
                </option>
            `).join('');
    } catch (e) {
        document.getElementById('venueId').innerHTML =
            '<option value="">Không tải được địa điểm</option>';
    }

    // Tải nghệ sĩ
    try {
        const artists = Utils.array(await API.get('/api/artists'));

        document.getElementById('artistId').innerHTML =
            '<option value="">Chọn nghệ sĩ</option>' +
            artists.map(a => `
                <option value="${Utils.esc(a.id)}">
                    ${Utils.esc(a.artistName)}
                </option>
            `).join('');
    } catch (e) {
        document.getElementById('artistId').innerHTML =
            '<option value="">Không tải được nghệ sĩ</option>';
    }

    // Tải suất diễn
    async function load() {
        eventId = select.value;

        if (!eventId) {
            schedules = [];
            rows.innerHTML = '<tr><td colspan="5" class="empty">Hãy chọn sự kiện.</td></tr>';
            document.getElementById('artistSchedule').innerHTML =
                '<option value="">Chọn suất diễn</option>';
            return;
        }

        try {
            schedules = Utils.array(
                await API.get('/api/schedules/events/' + encodeURIComponent(eventId))
            );

            rows.innerHTML = schedules.map(s => {
                const id = s.id;

                return `
                <tr>
                    <td>${Utils.esc(s.venue?.venueName || 'Suất diễn ' + id)}</td>
                    <td>${Utils.esc(Utils.date(s.startDatetime))}</td>
                    <td>${Utils.esc(Utils.date(s.endDatetime))}</td>
                    <td><span class="badge">${Utils.esc(s.status || '—')}</span></td>
                    <td>
                        <button class="btn btn-light edit" data-id="${Utils.esc(id)}">
                            Sửa
                        </button>
                    </td>
                </tr>`;
            }).join('') ||
            '<tr><td colspan="5" class="empty">Chưa có suất diễn.</td></tr>';

            document.getElementById('artistSchedule').innerHTML =
                '<option value="">Chọn suất diễn</option>' +
                schedules.map(s => `
                    <option value="${Utils.esc(s.id)}">
                        ${Utils.esc(s.venue?.venueName || 'Suất diễn ' + s.id)}
                    </option>
                `).join('');

        } catch (e) {
            msg('Không tải được suất diễn: ' + e.message, true);
        }
    }

    select.onchange = () => {
        eventId = select.value;
        history.replaceState(
            null,
            '',
            eventId ? '?eventId=' + encodeURIComponent(eventId) : location.pathname
        );
        load();
    };

    // Tạo suất diễn mới
    document.getElementById('newSchedule').onclick = () => {
        document.getElementById('scheduleForm').reset();
        document.getElementById('scheduleId').value = '';
        document.getElementById('editorTitle').textContent = 'Tạo suất diễn';
        document.getElementById('editor').scrollIntoView({ behavior: 'smooth' });
    };

    // Chỉnh sửa suất diễn
    rows.addEventListener('click', e => {
        const b = e.target.closest('.edit');
        if (!b) return;

        const s = schedules.find(x => String(x.id) === b.dataset.id);
        if (!s) return;

        document.getElementById('scheduleId').value = s.id;
        document.getElementById('venueId').value = s.venue?.id || '';
        document.getElementById('startDatetime').value =
            String(s.startDatetime || '').slice(0, 16);
        document.getElementById('endDatetime').value =
            String(s.endDatetime || '').slice(0, 16);

        document.getElementById('editorTitle').textContent = 'Sửa suất diễn';
        document.getElementById('editor').scrollIntoView({ behavior: 'smooth' });
    });

    // Lưu suất diễn
    document.getElementById('scheduleForm').onsubmit = async e => {
        e.preventDefault();

        if (!select.value) {
            msg('Hãy chọn sự kiện trước.', true);
            return;
        }

        const id = document.getElementById('scheduleId').value;
        const start = document.getElementById('startDatetime').value;
        const end = document.getElementById('endDatetime').value;

        if (new Date(end) <= new Date(start)) {
            msg('Thời gian kết thúc phải sau thời gian bắt đầu.', true);
            return;
        }

        const payload = {
            venueId: document.getElementById('venueId').value,
            startDatetime: start,
            endDatetime: end
        };

        try {
            if (id) {
                await API.put(
                    '/api/schedules/organizer/' + encodeURIComponent(id),
                    payload
                );
            } else {
                await API.post(
                    '/api/schedules/organizer/events/' + encodeURIComponent(select.value),
                    payload
                );
            }

            msg('Lưu suất diễn thành công.');
            e.target.reset();
            document.getElementById('scheduleId').value = '';
            await load();

        } catch (err) {
            msg(err.message, true);
        }
    };

    // Gán nghệ sĩ
    document.getElementById('artistForm').onsubmit = async e => {
        e.preventDefault();

        const sid = document.getElementById('artistSchedule').value;
        const artistId = document.getElementById('artistId').value;

        if (!sid || !artistId) {
            msg('Chọn suất diễn và nghệ sĩ.', true);
            return;
        }

        try {
            await API.post(
                '/api/schedules/organizer/' + encodeURIComponent(sid) + '/artists',
                {
                    artistId,
                    role: document.getElementById('artistRole').value.trim() || null
                }
            );

            msg('Gán nghệ sĩ thành công.');
            e.target.reset();

        } catch (err) {
            msg(err.message, true);
        }
    };

    if (eventId) select.value = eventId;
    await load();
});
