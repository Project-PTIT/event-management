
document.addEventListener('DOMContentLoaded', async () => {
    const root = document.getElementById('page-content');

    root.innerHTML = `
    <div class="heading">
        <div>
            <h1>Sự kiện của tôi</h1>
            <div class="muted">Theo dõi trạng thái và cập nhật sự kiện.</div>
        </div>
        <a class="btn btn-primary" href="/organizer/event-form.html">＋ Tạo sự kiện</a>
    </div>
    <section class="panel">
        <div id="notice"></div>
        <div class="toolbar">
            <input id="keyword" placeholder="Tìm theo tên sự kiện…">
            <select id="status">
                <option value="">Tất cả trạng thái</option>
                <option value="PENDING">PENDING</option>
                <option value="APPROVED">APPROVED</option>
                <option value="REJECTED">REJECTED</option>
            </select>
            <button id="search" class="btn btn-light">Tìm kiếm</button>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Tên sự kiện</th>
                        <th>Loại tạo</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody id="rows"></tbody>
            </table>
        </div>
    </section>`;

    const $ = id => document.getElementById(id);
    const rows = $('rows');
    const notice = $('notice');

    const badge = status => {
        const s = status || 'PENDING';
        const cls = s === 'APPROVED' ? 'badge-approved'
            : s === 'REJECTED' ? 'badge-rejected'
            : 'badge-pending';

        return `<span class="badge ${cls}">${Utils.esc(s)}</span>`;
    };

    async function load() {
        notice.innerHTML = '';
        rows.innerHTML = '<tr><td colspan="4">Đang tải dữ liệu…</td></tr>';

        try {
            const status = $('status').value;
            const keyword = $('keyword').value.trim().toLowerCase();
            const url = '/api/organizer/events' +
                (status ? '?status=' + encodeURIComponent(status) : '');

            let data = Utils.array(await API.get(url));

            data = data.filter(e =>
                !keyword || String(e.title || '').toLowerCase().includes(keyword)
            );

            rows.innerHTML = data.map(e => `
                <tr>
                    <td>
                        <strong>${Utils.esc(e.title || 'Chưa có tên')}</strong>
                        <div class="muted">${Utils.esc(e.desc || '')}</div>
                    </td>
                    <td>${Utils.esc(e.createType || '—')}</td>
                    <td>${badge(e.status)}</td>
                    <td>
                        <div class="actions">
                            <a class="btn btn-light"
                               href="/organizer/event-form.html?id=${encodeURIComponent(e.eventId)}">Sửa</a>
                            <a class="btn btn-light"
                               href="/organizer/schedules.html?eventId=${encodeURIComponent(e.eventId)}">Suất diễn</a>
                        </div>
                    </td>
                </tr>
            `).join('') || `
                <tr><td colspan="4" class="empty">Chưa có sự kiện phù hợp.</td></tr>
            `;
        } catch (e) {
            notice.innerHTML = `
                <div class="notice error">${Utils.esc(e.message)}</div>
            `;
            rows.innerHTML = '';
        }
    }

    $('search').onclick = load;
    $('status').onchange = load;
    $('keyword').onkeydown = e => {
        if (e.key === 'Enter') load();
    };

    await load();
});
