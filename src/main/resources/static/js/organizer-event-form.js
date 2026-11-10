document.addEventListener('DOMContentLoaded',async()=>{
 const root=document.getElementById('page-content'), id=new URLSearchParams(location.search).get('id');
 root.innerHTML=`<div class="heading"><div><h1 id="title">${id?'Sửa sự kiện':'Tạo sự kiện mới'}</h1><div class="muted">Sự kiện mới sẽ được gửi Admin duyệt với trạng thái PENDING.</div></div><a class="btn btn-light" href="/organizer/events.html">← Danh sách</a></div><section class="panel"><div id="notice"></div><form id="form"><div class="form-grid">
 <div class="field full"><label for="eventName">Tên sự kiện *</label><input id="eventName" required maxlength="255"></div>
 <div class="field full"><label for="description">Mô tả</label><textarea id="description"></textarea></div>
 <div class="field"><label for="createType">Chu kỳ</label><select id="createType"><option value="">Không lặp</option><option value="WEEKLY">Hàng tuần</option><option value="MONTHLY">Hàng tháng</option><option value="YEARLY">Hàng năm</option></select></div>
 <div class="field"><label for="wardId">Phường</label><input id="wardId" placeholder="Mã phường, ví dụ P001"></div>
 <div class="field"><label for="startDate">Ngày bắt đầu (nếu DTO có trường này)</label><input id="startDate" type="date"></div>
 <div class="field"><label for="endDate">Ngày kết thúc (nếu DTO có trường này)</label><input id="endDate" type="date"></div>
 </div><div class="row-between"><span class="muted">Các trường bắt buộc có thể thay đổi theo DTO Backend.</span><button class="btn btn-primary" id="save" type="submit">Lưu sự kiện</button></div></form></section>`;
 const notice=document.getElementById('notice');
 function fill(e){for(const k of ['eventName','description','createType','wardId','startDate','endDate'])if(e[k]!==undefined&&e[k]!==null)document.getElementById(k).value=e[k];}
 if(id){try{const e=await API.get('/api/events/'+encodeURIComponent(id));fill(e);document.getElementById('title').textContent='Sửa sự kiện';}catch(err){notice.innerHTML=`<div class="notice error">Không tải được chi tiết sự kiện: ${Utils.esc(err.message)}. Nếu API chi tiết không trả sự kiện của Organizer, hãy kiểm tra API Backend.</div>`;}}
 document.getElementById('form').onsubmit=async ev=>{ev.preventDefault();const btn=document.getElementById('save');btn.disabled=true;notice.innerHTML='';
  const payload={eventName:document.getElementById('eventName').value.trim(),description:document.getElementById('description').value.trim(),createType:document.getElementById('createType').value||null,wardId:document.getElementById('wardId').value.trim()||null,startDate:document.getElementById('startDate').value||null,endDate:document.getElementById('endDate').value||null};
  try{if(id)await API.put('/api/organizer/events/'+encodeURIComponent(id),payload);else await API.post('/api/organizer/events',payload);notice.innerHTML='<div class="notice success">Lưu sự kiện thành công.</div>';setTimeout(()=>location.href='/organizer/events.html',700);}
  catch(err){notice.innerHTML=`<div class="notice error">${Utils.esc(err.message)}</div>`;}finally{btn.disabled=false;}
 };
});
