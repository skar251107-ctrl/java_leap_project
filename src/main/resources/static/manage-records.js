const resourceConfig = {
  'blood-groups': {
    label: 'Blood group',
    plural: 'Blood groups',
    fields: [
      { name: 'bloodGroup', label: 'Blood group', type: 'select', required: true, options: ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'] },
      { name: 'description', label: 'Description' }
    ]
  },
  donors: {
    label: 'Donor',
    plural: 'Donors',
    fields: [
      { name: 'name', label: 'Full name', required: true },
      { name: 'phone', label: 'Phone', type: 'tel', required: true },
      { name: 'bloodGroup', label: 'Blood group', type: 'select', required: true, dynamicOptions: 'blood-groups' },
      { name: 'city', label: 'City', required: true },
      { name: 'lastDonationDate', label: 'Last donation date', type: 'date' }
    ]
  },
  donations: {
    label: 'Donation',
    plural: 'Donations',
    fields: [
      { name: 'donorId', label: 'Donor', type: 'select', required: true, dynamicOptions: 'donors' },
      { name: 'donationDate', label: 'Donation date', type: 'date', required: true },
      { name: 'hospitalName', label: 'Hospital name', required: true }
    ]
  },
  'admin-users': {
    label: 'Admin user',
    plural: 'Admin users',
    fields: [
      { name: 'username', label: 'Username', required: true, minLength: 3 },
      { name: 'email', label: 'Email', type: 'email' },
      { name: 'role', label: 'Role', required: true },
      { name: 'password', label: 'Password (12+ characters)', type: 'password', required: true, minLength: 12 }
    ]
  }
};

const form = document.getElementById('recordForm');
const fieldsElement = document.getElementById('recordFields');
const listElement = document.getElementById('recordList');
const statusElement = document.getElementById('manageStatus');
const saveButton = document.getElementById('saveButton');
const cancelButton = document.getElementById('cancelEditButton');
let activeResource = 'blood-groups';
let records = [];
let relatedOptions = { 'blood-groups': [], donors: [] };
let editingId = null;

function fieldOptions(field) {
  if (field.options) return field.options.map(value => [value, value]);
  if (field.dynamicOptions === 'blood-groups') {
    return relatedOptions['blood-groups'].map(group => [group.bloodGroup, group.bloodGroup]);
  }
  if (field.dynamicOptions === 'donors') {
    return relatedOptions.donors.map(donor => [donor.id, `${donor.name} · ${donor.phone}`]);
  }
  return [];
}

function renderFields() {
  const config = resourceConfig[activeResource];
  fieldsElement.innerHTML = config.fields.map(field => {
    const id = `field-${field.name}`;
    let input;
    if (field.type === 'select') {
      const options = fieldOptions(field);
      input = `<select id="${id}" name="${field.name}"${field.required ? ' required' : ''}><option value="">${options.length ? 'Select...' : 'No records available'}</option>${options.map(([value, label]) => `<option value="${bloodlinkEscape(value)}">${bloodlinkEscape(label)}</option>`).join('')}</select>`;
    } else {
      input = `<input id="${id}" name="${field.name}" type="${field.type || 'text'}"${field.minLength ? ` minlength="${field.minLength}"` : ''}${field.required ? ' required' : ''}${field.type === 'password' ? ' autocomplete="new-password"' : ''}>`;
    }
    return `<div class="field"><label for="${id}">${field.label}</label>${input}</div>`;
  }).join('');
}

function resetForm() {
  form.reset();
  editingId = null;
  const passwordInput = form.elements.namedItem('password');
  if (passwordInput) {
    passwordInput.required = true;
    passwordInput.placeholder = 'At least 12 characters';
  }
  document.getElementById('formTitle').textContent = `Add ${resourceConfig[activeResource].label.toLowerCase()}`;
  saveButton.textContent = `Add ${resourceConfig[activeResource].label.toLowerCase()}`;
  cancelButton.hidden = true;
}

function displayDetails(record) {
  if (activeResource === 'blood-groups') return record.description || record.bloodGroup;
  if (activeResource === 'donors') return `${BLOOD_GROUP_LABELS[record.bloodGroup] || record.bloodGroup} · ${record.city} · ${record.phone} · Last donated ${record.lastDonationDate || 'not recorded'}`;
  if (activeResource === 'donations') return `${record.donationDate} · ${record.hospitalName}`;
  return record.email;
}

function displayTitle(record) {
  if (activeResource === 'blood-groups') return record.bloodGroup;
  if (activeResource === 'donors') return record.name;
  if (activeResource === 'donations') return record.donorName;
  return record.username;
}

function renderRecords() {
  const config = resourceConfig[activeResource];
  document.getElementById('recordCount').textContent = `${records.length} record${records.length === 1 ? '' : 's'}`;
  document.getElementById('listTitle').textContent = config.plural;
  if (!records.length) {
    listElement.innerHTML = `<p class="muted">No ${config.plural.toLowerCase()} yet.</p>`;
    return;
  }
  listElement.innerHTML = records.map(record => `
    <article class="record-item">
      <div class="record-main">
        <h3>${bloodlinkEscape(displayTitle(record))}</h3>
        <p>${bloodlinkEscape(displayDetails(record))}</p>
      </div>
      <div class="record-actions">
        <button class="button secondary" type="button" data-edit-id="${bloodlinkEscape(record.id)}">Edit</button>
        <button class="button secondary" type="button" data-delete-id="${bloodlinkEscape(record.id)}">Delete</button>
      </div>
    </article>`).join('');
}

async function loadRecords() {
  listElement.innerHTML = '<p class="muted">Loading records...</p>';
  try {
    [relatedOptions['blood-groups'], relatedOptions.donors] = await Promise.all([
      bloodlinkRequest('/blood-groups'), bloodlinkRequest('/donors')
    ]);
    renderFields();
    records = await bloodlinkRequest(`/${activeResource}`);
    renderRecords();
  } catch (error) {
    records = [];
    document.getElementById('recordCount').textContent = '';
    listElement.innerHTML = '<p class="muted">Records could not be loaded.</p>';
    bloodlinkSetStatus(statusElement, error.message || 'Records could not be loaded.', 'error');
  }
}

function switchResource(resource) {
  activeResource = resource;
  document.querySelectorAll('.resource-tab').forEach(tab => {
    tab.setAttribute('aria-selected', String(tab.dataset.resource === resource));
  });
  bloodlinkSetStatus(statusElement, '');
  renderFields();
  resetForm();
  loadRecords();
}

function startEdit(id) {
  const record = records.find(item => String(item.id) === String(id));
  if (!record) return;
  resetForm();
  editingId = record.id;
  const passwordInput = form.elements.namedItem('password');
  if (passwordInput) {
    passwordInput.required = false;
    passwordInput.placeholder = 'Leave blank to keep current password';
  }
  for (const field of resourceConfig[activeResource].fields) {
    const input = form.elements.namedItem(field.name);
    if (input) input.value = record[field.name] ?? '';
  }
  document.getElementById('formTitle').textContent = `Edit ${resourceConfig[activeResource].label.toLowerCase()}`;
  saveButton.textContent = 'Save changes';
  cancelButton.hidden = false;
  document.getElementById('formTitle').scrollIntoView({ block: 'nearest', behavior: 'smooth' });
}

form.addEventListener('submit', async event => {
  event.preventDefault();
  const payload = Object.fromEntries(new FormData(form).entries());
  for (const name of ['donorId']) {
    if (payload[name] !== undefined) payload[name] = Number(payload[name]);
  }
  if (payload.lastDonationDate === '') {
    payload.lastDonationDate = null;
  }
  if (payload.password === '') {
    delete payload.password;
  }
  if (payload.email === '') payload.email = null;
  const id = editingId;
  const config = resourceConfig[activeResource];
  saveButton.disabled = true;
  try {
    const method = id === null ? 'POST' : 'PUT';
    const path = id === null ? `/${activeResource}` : `/${activeResource}/${encodeURIComponent(id)}`;
    await bloodlinkRequest(path, { method, body: JSON.stringify(payload) });
    resetForm();
    bloodlinkSetStatus(statusElement, `${config.label} ${id === null ? 'created' : 'updated'}.`, 'success');
    await loadRecords();
  } catch (error) {
    bloodlinkSetStatus(statusElement, error.message || 'Could not save this record.', 'error');
  } finally {
    saveButton.disabled = false;
  }
});

cancelButton.addEventListener('click', resetForm);

document.querySelector('.resource-tabs').addEventListener('click', event => {
  const tab = event.target.closest('[data-resource]');
  if (tab && tab.dataset.resource !== activeResource) switchResource(tab.dataset.resource);
});

listElement.addEventListener('click', async event => {
  const editButton = event.target.closest('[data-edit-id]');
  if (editButton) {
    startEdit(editButton.dataset.editId);
    return;
  }
  const deleteButton = event.target.closest('[data-delete-id]');
  if (!deleteButton) return;
  const record = records.find(item => String(item.id) === deleteButton.dataset.deleteId);
  if (!record || !window.confirm(`Delete this ${resourceConfig[activeResource].label.toLowerCase()}?`)) return;
  deleteButton.disabled = true;
  try {
    await bloodlinkRequest(`/${activeResource}/${encodeURIComponent(record.id)}`, { method: 'DELETE' });
    if (String(editingId) === String(record.id)) resetForm();
    bloodlinkSetStatus(statusElement, `${resourceConfig[activeResource].label} deleted.`, 'success');
    await loadRecords();
  } catch (error) {
    deleteButton.disabled = false;
    bloodlinkSetStatus(statusElement, error.message || 'Could not delete this record.', 'error');
  }
});

document.querySelectorAll('.resource-tab').forEach(tab => {
  tab.addEventListener('keydown', event => {
    if (!['ArrowLeft', 'ArrowRight'].includes(event.key)) return;
    event.preventDefault();
    const tabs = [...document.querySelectorAll('.resource-tab')];
    const direction = event.key === 'ArrowRight' ? 1 : -1;
    const nextTab = tabs[(tabs.indexOf(tab) + direction + tabs.length) % tabs.length];
    nextTab.focus();
    switchResource(nextTab.dataset.resource);
  });
});

switchResource(activeResource);