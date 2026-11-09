
document.addEventListener('DOMContentLoaded', async () => {
    const root = document.getElementById('page-content');

    root.innerHTML = `
    <div class="heading">
        <div>
            <h1>Thống kê bán vé</h1>
            <div class="muted">Tổng hợp số vé và doanh thu ước tính theo sự kiện.</div>
        </div>
        <button id="refresh" class="btn btn-light">↻ Làm mới</button>
    </div>
    <div id="notice"></div>
    <div class="cards">
        <div class="stat-card">
            <div class="label">Doanh thu ước tính</div>
            <div class="value" id="revenue">—</div>
        </div>
        <div class="stat-card">
            <div class="label">Số vé đã bán</div>
            <div class="value" id="sold">—</div>
        </div>
        <div class="stat-card">
            <div class="label">Số sự kiện</div>
            <div class="value" id="events">—</div>
        </div>
    </div>
    <section class="panel">
        <h2>Doanh thu ước tính theo sự kiện</h2>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Sự kiện</th>
                        <th>Doanh thu ước tính</th>
                        <th>Vé đã bán</th>
                    </tr>
                </thead>
                <tbody id="rows"></tbody>
            </table>
        </div>
        <div id="chart" class="chart-bars"></div>
    </section>
    <section class="panel">
        <h2>Doanh thu thực tế theo tháng</h2>
        <p class="muted">Chỉ tính các đơn hàng có trạng thái Đã thanh toán.</p>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr><th>Tháng</th><th>Doanh thu</th></tr>
                </thead>
                <tbody id="monthlyRows"></tbody>
            </table>
        </div>
    </section>`;

    const $ = id => document.getElementById(id);
    const notice = $('notice');
    const rows = $('rows');
    const monthlyRows = $('monthlyRows');

    const number = v => Number(v || 0);
    const format = v => new Intl.NumberFormat('vi-VN').format(v);

    async function load() {
        $('refresh').disabled = true;
        notice.innerHTML = '';

        try {
            const [ticketsData, revenueData] = await Promise.all([
                API.get('/api/tickets'),
                API.get('/api/tickets/stats/revenue')
            ]);

            const tickets = Utils.array(ticketsData);
            const monthly = Utils.array(revenueData);

            const groups = {};

            tickets.forEach(t => {
                const name = t.eventTitle || 'Chưa xác định';
                const sold = Math.max(
                    0,
                    number(t.totalQuantity) - number(t.remainingQuantity)
                );
                const revenue = number(t.price) * sold;

                if (!groups[name]) {
                    groups[name] = { sold: 0, revenue: 0 };
                }

                groups[name].sold += sold;
                groups[name].revenue += revenue;
            });

            const list = Object.entries(groups).map(([name, data]) => ({
                name,
                ...data
            }));

            const totalSold = list.reduce((s, e) => s + e.sold, 0);
            const totalRevenue = list.reduce((s, e) => s + e.revenue, 0);

            $('revenue').textContent = Utils.money(totalRevenue);
            $('sold').textContent = format(totalSold);
            $('events').textContent = format(list.length);

            rows.innerHTML = list.map(e => `
                <tr>
                    <td><strong>${Utils.esc(e.name)}</strong></td>
                    <td>${Utils.esc(Utils.money(e.revenue))}</td>
                    <td>${format(e.sold)}</td>
                </tr>
            `).join('') || `
                <tr><td colspan="3" class="empty">Chưa có dữ liệu.</td></tr>
            `;

            const max = Math.max(1, ...list.map(e => e.revenue));

            $('chart').innerHTML = list.slice(0, 8).map(e => `
                <div class="bar-item">
                    <div class="bar-value">${Utils.esc(Utils.money(e.revenue))}</div>
                    <div class="bar" style="height:${Math.max(3, e.revenue / max * 150)}px"></div>
                    <div>${Utils.esc(e.name)}</div>
                </div>
            `).join('');

            monthlyRows.innerHTML = monthly.map(item => {
                const month = item[0];
                const revenue = number(item[1]);

                return `
                    <tr>
                        <td>Tháng ${Utils.esc(month)}</td>
                        <td><strong>${Utils.esc(Utils.money(revenue))}</strong></td>
                    </tr>
                `;
            }).join('') || `
                <tr><td colspan="2" class="empty">Chưa có dữ liệu.</td></tr>
            `;

        } catch (e) {
            notice.innerHTML = `
                <div class="notice error">
                    Không tải được thống kê: ${Utils.esc(e.message)}
                </div>`;
        } finally {
            $('refresh').disabled = false;
        }
    }

    $('refresh').onclick = load;
    await load();
});
