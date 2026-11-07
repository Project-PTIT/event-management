document.addEventListener('DOMContentLoaded', async()=>{
 const root=document.getElementById('page-content');
 root.innerHTML=`<div class="heading"><div><h1>Sự kiện của tôi</h1><div class="muted">Theo dõi trạng thái duyệt và cập nhật thông tin sự kiện.</div></div><a class="btn btn-primary" href="/organizer/event-form.html">＋ Tạo sự kiện</a></div><section class="panel"><div id="notice"></div><div class="toolbar"><input id="keyword" placeholder="Tìm theo tên sự kiện…"><select id="status"><option value="">Tất cả trạng thái</option><option>PENDING</option><option>APPROVED</option><option>REJECTED</option></select><button id="search" class="btn btn-light">Tìm kiếm</button></div><div class="table-wrap"><table><thead><tr><th>Tên sự kiện</th><th>Ngày tạo</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody id="rows"></tbody></table></div><div class="empty hidden" id="empty">Chưa có sự kiện phù hợp.</div></section>`;
 const notice=document.getElementById('notice'), rows=document.getElementById('rows');
 const statusBadge=s=>`<span class="badge ${s==='APPROVED'?'badge-approved':s==='REJECTED'?'badge-rejected':'badge-pending'}">${Utils.esc(s||'PENDING')}</span>`;
 async function load(){notice.innerHTML='';rows.innerHTML='<tr><td colspan="4">Đang tải dữ liệu…</td></tr>';
  try{let data=Utils.array(await API.get('/api/organizer/events'));const st=document.getElementById('status').value, kw=document.getElementById('keyword').value.trim().toLowerCase();
   data=data.filter(e=>(!st||e.status===st)&&(!kw||String(Utils.pick(e,'eventName','name','title')).toLowerCase().includes(kw)));
   rows.innerHTML=data.map(e=>{const id=Utils.pick(e,'eventId','id');return `<tr><td><strong>${Utils.esc(Utils.pick(e,'eventName','name','title')||'Chưa có tên')}</strong><div class="muted">${Utils.esc(Utils.pick(e,'description')||'')}</div></td><td>${Utils.esc(Utils.date(Utils.pick(e,'createdAt','startDate')))}</td><td>${statusBadge(e.status)}</td><td><div class="actions"><a class="btn btn-light" href="/organizer/event-form.html?id=${encodeURIComponent(id)}">Sửa</a><a class="btn btn-light" href="/organizer/schedules.html?eventId=${encodeURIComponent(id)}">Suất diễn</a></div></td></tr>`}).join('');
   document.getElementById('empty').classList.toggle('hidden',data.length>0);if(!data.length)rows.innerHTML='';
  }catch(e){notice.innerHTML=`<div class="notice error">${Utils.esc(e.message)}</div>`;rows.innerHTML='';}
 }
 document.getElementById('search').onclick=load;document.getElementById('status').onchange=load;document.getElementById('keyword').onkeydown=e=>{if(e.key==='Enter')load()};load();
});
