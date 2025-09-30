const usersList = document.getElementById('users');
const countEl = document.getElementById('count');
const searchEl = document.getElementById('search');
const refreshBtn = document.getElementById('refresh');
const createForm = document.getElementById('create-form');

const template = document.getElementById('user-card-template');
const editDialog = document.getElementById('edit-dialog');
const editForm = document.getElementById('edit-form');

const API = {
  async list(q) {
    const url = q && q.trim() ? `/api/users?q=${encodeURIComponent(q.trim())}` : '/api/users';
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch users');
    return res.json();
  },
  async create(data) {
    const res = await fetch('/api/users', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) throw new Error('Failed to create user');
    return res.json();
  },
  async update(id, data) {
    const res = await fetch(`/api/users/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) throw new Error('Failed to update user');
    return res.json();
  },
  async remove(id) {
    const res = await fetch(`/api/users/${id}`, { method: 'DELETE' });
    if (!res.ok && res.status !== 204) throw new Error('Failed to delete user');
  },
};

function timeAgo(iso) {
  const date = new Date(iso);
  const seconds = Math.floor((Date.now() - date.getTime()) / 1000);
  const rtf = new Intl.RelativeTimeFormat(undefined, { numeric: 'auto' });
  const units = [
    ['year', 31536000],
    ['month', 2592000],
    ['day', 86400],
    ['hour', 3600],
    ['minute', 60],
    ['second', 1],
  ];
  for (const [unit, value] of units) {
    if (Math.abs(seconds) >= value || unit === 'second') {
      return rtf.format(-Math.round(seconds / value), unit);
    }
  }
}

function render(users) {
  usersList.innerHTML = '';
  countEl.textContent = users.length;
  for (const user of users) {
    const node = template.content.cloneNode(true);
    const card = node.querySelector('.card');
    const img = node.querySelector('.avatar');
    const name = node.querySelector('.name');
    const email = node.querySelector('.email');
    const ts = node.querySelector('.timestamp');
    const editBtn = node.querySelector('.edit');
    const deleteBtn = node.querySelector('.delete');

    img.src = user.avatarUrl || `https://unavatar.io/${encodeURIComponent(user.email || user.name || 'user')}`;
    img.alt = `${user.name}'s avatar`;
    name.textContent = user.name || '(no name)';
    email.textContent = user.email || '';
    ts.textContent = user.createdAt ? `Joined ${timeAgo(user.createdAt)}` : '';

    editBtn.addEventListener('click', () => openEdit(user));
    deleteBtn.addEventListener('click', async () => {
      if (!confirm(`Delete ${user.name}?`)) return;
      await API.remove(user.id);
      await reload();
    });

    usersList.appendChild(node);
  }
}

async function reload() {
  const q = searchEl.value;
  const data = await API.list(q);
  // Sort newest first
  data.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
  render(data);
}

createForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const data = {
    name: document.getElementById('name').value.trim(),
    email: document.getElementById('email').value.trim(),
    avatarUrl: document.getElementById('avatarUrl').value.trim(),
  };
  if (!data.name || !data.email) return;
  await API.create(data);
  createForm.reset();
  await reload();
});

refreshBtn.addEventListener('click', reload);
searchEl.addEventListener('input', debounce(reload, 250));

function debounce(fn, wait) {
  let t;
  return (...args) => {
    clearTimeout(t);
    t = setTimeout(() => fn(...args), wait);
  };
}

function openEdit(user) {
  document.getElementById('edit-id').value = user.id;
  document.getElementById('edit-name').value = user.name || '';
  document.getElementById('edit-email').value = user.email || '';
  document.getElementById('edit-avatar').value = user.avatarUrl || '';
  if (typeof editDialog.showModal === 'function') {
    editDialog.showModal();
  } else {
    alert('Your browser does not support <dialog>.');
  }
}

editForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('edit-id').value;
  const data = {
    name: document.getElementById('edit-name').value.trim(),
    email: document.getElementById('edit-email').value.trim(),
    avatarUrl: document.getElementById('edit-avatar').value.trim(),
  };
  await API.update(id, data);
  editDialog.close();
  await reload();
});

// Initial load
reload().catch(err => console.error(err));

