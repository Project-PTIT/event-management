document.addEventListener('DOMContentLoaded',()=>{
 const path=location.pathname;
 const links=[
  ['/organizer/events.html','Sự kiện của tôi'],
  ['/organizer/event-form.html','Tạo sự kiện'],
  ['/organizer/schedules.html','Suất diễn & nghệ sĩ'],
  ['/organizer/tickets.html','Hạng vé'],
  ['/organizer/statistics.html','Thống kê']
 ];
 const active=links.find(([p])=>path.endsWith(p))?.[0]||'';
 const nav=links.map(([p,t])=>`<a class="${p===active?'active':''}" href="${p}">${t}</a>`).join('');
 const root=document.querySelector('[data-layout]');
 if(root) root.innerHTML=`<div class="app"><aside class="sidebar"><div class="brand">Event<span>Tickets</span></div><nav class="nav">${nav}<a href="/index.html">← Trang công khai</a></nav></aside><div class="main"><header class="topbar"><strong>Khu vực Organizer</strong><small>Quản lý sự kiện và bán vé</small></header><main class="content" id="page-content"></main></div></div>`;
});
