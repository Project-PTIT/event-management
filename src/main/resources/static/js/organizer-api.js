(function(){
  const base = (window.APP_CONFIG && window.APP_CONFIG.API_BASE_URL || '').replace(/\/$/,'');
  async function request(path, options={}) {
    const opts = {...options, credentials:'same-origin', headers:{...(options.body ? {'Content-Type':'application/json'} : {}), ...(options.headers||{})}};
    let response;
    try { response = await fetch(base + path, opts); }
    catch(e) { throw new Error('Không kết nối được máy chủ. Hãy kiểm tra ứng dụng Spring Boot.'); }
    if (response.status === 401) { location.href='/login.html'; throw new Error('Phiên đăng nhập hết hạn.'); }
    if (response.status === 403) throw new Error('Bạn không có quyền thực hiện thao tác này.');
    let body={};
    const text=await response.text();
    try { body=text ? JSON.parse(text) : {}; } catch { body={message:text}; }
    if (!response.ok || body.success === false) throw new Error(body.message || `Lỗi HTTP ${response.status}`);
    return Object.prototype.hasOwnProperty.call(body,'data') ? body.data : body;
  }
  window.API = {
    request,
    get:(p)=>request(p),
    post:(p,d)=>request(p,{method:'POST',body:JSON.stringify(d)}),
    put:(p,d)=>request(p,{method:'PUT',body:JSON.stringify(d)}),
    del:(p)=>request(p,{method:'DELETE'})
  };
  window.Utils = {
    esc(v){return String(v ?? '').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));},
    money(v){return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND',maximumFractionDigits:0}).format(Number(v||0));},
    date(v){if(!v)return '—';const d=new Date(v);return Number.isNaN(d.getTime())?String(v):d.toLocaleString('vi-VN');},
    pick(o,...keys){for(const k of keys)if(o&&o[k]!==undefined&&o[k]!==null)return o[k];return '';},
    array(v){if(Array.isArray(v))return v;if(v&&Array.isArray(v.content))return v.content;if(v&&Array.isArray(v.items))return v.items;return [];}
  };
})();
