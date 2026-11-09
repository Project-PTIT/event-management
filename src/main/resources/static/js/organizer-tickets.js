
document.addEventListener('DOMContentLoaded', async () => {
    const root = document.getElementById('page-content');

    root.innerHTML = `
    <div class="heading">
        <div>
            <h1>Quản lý hạng vé</h1>
            <div class="muted">Tạo và cập nhật giá, số lượng vé.</div>
        </div>
        <a class="btn btn-light" href="/organizer/schedules.html">Quản lý suất diễn</a>
    </div>
    <section class="panel">
        <div id="notice"></div>
        <div class="toolbar">
            <label class="field" style="flex:1;min-width:260px">
                <span>Chọn suất diễn</span>
                <select id="scheduleSelect"></select>
            </label>
            <button id="reload" class="btn btn-light">Tải hạng vé</button>
            <button id="new" class="btn btn-primary">＋ Tạo hạng vé</button>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Hạng vé</th>
                        <th>Giá</th>
                        <th>Tổng vé</th>
                        <th>Còn lại</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody id="rows"></tbody>
            </table>
        </div>
    </section>
    <section class="panel">
        <h2 id="formTitle">Tạo hạng vé</h2>
        <form id="form">
            <input id="ticketId" type="hidden">
            <div class="form-grid">
                <div class="field">
                    <label>Tên hạng vé *</label>
                    <input id="ticketName" required maxlength="150">
                </div>
                <div class="field">
                    <label>Giá (VNĐ) *</label>
                    <input id="price" type="number" min="0" required>
                </div>
                <div class="field">
                    <label>Tổng số vé *</label>
                    <input id="totalQuantity" type="number" min="1" required>
                </div>
                <div class="field">
                    <label>Số vé còn lại *</label>
                    <input id="remainingQuantity" type="number" min="0" required>
                </div>
                <div class="field full">
                    <label>Mô tả</label>
                    <textarea id="description"></textarea>
                </div>
            </div>
            <div class="row-between">
                <span class="muted">Số vé còn lại không được lớn hơn tổng số vé.</span>
                <button class="btn btn-primary">Lưu hạng vé</button>
            </div>
        </form>
    </section>`;

    const notice = document.getElementById('notice');
    const rows = document.getElementById('rows');
    const select = document.getElementById('scheduleSelect');
    const form = document.getElementById('form');
    let tickets = [];

    const msg = (s, err = false) => {
        notice.innerHTML = `<div class="notice ${err ? 'error' : 'success'}">${Utils.esc(s)}</div>`;
    };

    try {
        const data = Utils.array(await API.get('/api/tickets'));
        const schedules = [...new Map(
            data.filter(t => t.scheduleId).map(t => [t.scheduleId, t])
        ).values()];

        select.innerHTML = '<option value="">Chọn suất diễn</option>' +
            schedules.map(s => `
                <option value="${Utils.esc(s.scheduleId)}">
                    ${Utils.esc(s.eventTitle)} — ${Utils.esc(Utils.date(s.startDatetime))} — ${Utils.esc(s.venueName)}
                </option>
            `).join('');

        const wanted = new URLSearchParams(location.search).get('scheduleId');
        if (wanted) select.value = wanted;
    } catch (e) {
        msg('Không tải được danh sách vé: ' + e.message, true);
    }

    async function load() {
        const sid = select.value;

        if (!sid) {
            tickets = [];
            rows.innerHTML = '<tr><td colspan="5" class="empty">Chọn suất diễn.</td></tr>';
            return;
        }

        try {
            const all = Utils.array(await API.get('/api/tickets'));
            tickets = all.filter(t => t.scheduleId === sid);

            rows.innerHTML = tickets.map(t => `
                <tr>
                    <td><strong>${Utils.esc(t.ticketName)}</strong></td>
                    <td>${Utils.esc(Utils.money(t.price))}</td>
                    <td>${Utils.esc(t.totalQuantity)}</td>
                    <td>${Utils.esc(t.remainingQuantity)}</td>
                    <td>
                        <button class="btn btn-light edit" data-id="${Utils.esc(t.ticketId)}">Sửa</button>
                    </td>
                </tr>
            `).join('') || '<tr><td colspan="5" class="empty">Chưa có hạng vé.</td></tr>';
        } catch (e) {
            msg('Không tải được hạng vé: ' + e.message, true);
            rows.innerHTML = '';
        }
    }

    select.onchange = () => {
        history.replaceState(null, '', '?scheduleId=' + encodeURIComponent(select.value));
        load();
    };

    document.getElementById('reload').onclick = load;

    document.getElementById('new').onclick = () => {
        form.reset();
        document.getElementById('ticketId').value = '';
        document.getElementById('formTitle').textContent = 'Tạo hạng vé';
    };

    rows.addEventListener('click', e => {
        const btn = e.target.closest('.edit');
        if (!btn) return;

        const t = tickets.find(x => x.ticketId === btn.dataset.id);
        if (!t) return;

        document.getElementById('ticketId').value = t.ticketId;
        document.getElementById('ticketName').value = t.ticketName || '';
        document.getElementById('price').value = t.price;
        document.getElementById('totalQuantity').value = t.totalQuantity;
        document.getElementById('remainingQuantity').value = t.remainingQuantity;
        document.getElementById('description').value = t.desc || '';
        document.getElementById('formTitle').textContent = 'Sửa hạng vé';
    });

    form.onsubmit = async e => {
        e.preventDefault();

        const sid = select.value;
        const id = document.getElementById('ticketId').value;
        const total = Number(document.getElementById('totalQuantity').value);
        const remain = Number(document.getElementById('remainingQuantity').value);
        const price = Number(document.getElementById('price').value);

        if (!sid) return msg('Hãy chọn suất diễn.', true);
        if (remain > total || total < 1 || remain < 0 || price < 0) {
            return msg('Kiểm tra lại giá và số lượng vé.', true);
        }

        const payload = {
            scheduleId: sid,
            ticketName: document.getElementById('ticketName').value.trim(),
            price,
            desc: document.getElementById('description').value.trim(),
            totalQuantity: total,
            remainingQuantity: remain
        };

        try {
            if (id) {
                await API.put('/api/tickets/' + encodeURIComponent(id), payload);
            } else {
                await API.post('/api/tickets', payload);
            }

            msg('Lưu hạng vé thành công.');
            form.reset();
            document.getElementById('ticketId').value = '';
            await load();
        } catch (err) {
            msg(err.message, true);
        }
    };

    if (select.value) await load();
});
