const BLOODLINK_API = '/api';
const BLOOD_GROUP_LABELS = {};

async function bloodlinkLoadBloodGroups(select, placeholder) {
  const groups = await bloodlinkRequest('/blood-groups');
  select.innerHTML = `<option value="">${bloodlinkEscape(placeholder)}</option>${groups.map(group => {
    BLOOD_GROUP_LABELS[group.bloodGroup] = group.bloodGroup;
    return `<option value="${bloodlinkEscape(group.bloodGroup)}">${bloodlinkEscape(group.bloodGroup)}</option>`;
  }).join('')}`;
  return groups;
}

async function bloodlinkRequest(path, options = {}) {
  const response = await fetch(`${BLOODLINK_API}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  });
  if (!response.ok) {
    let message = 'Request failed';
    try {
      const payload = await response.json();
      message = payload.message || message;
    } catch (error) { }
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return response.json();
}

function bloodlinkEscape(value) {
  return String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[character]);
}

function bloodlinkDonorCard(donor, options = {}) {
  const phone = String(donor.phone || '').trim();
  const phoneDigits = phone.replace(/\D/g, '');
  const contact = options.hideContact ? '' : phone ? `
    <div class="contact-actions">
      <a class="button" href="tel:${encodeURIComponent(phone)}">Call donor</a>
      ${phoneDigits ? `<a class="button secondary" href="https://wa.me/${phoneDigits}" target="_blank" rel="noopener">WhatsApp</a>` : ''}
    </div>` : '<p class="muted">No contact number provided.</p>';
  const deleteAction = options.showDelete ? `
    <div class="contact-actions"><button class="button secondary" type="button" data-delete-donor="${bloodlinkEscape(donor.id)}" data-donor-name="${bloodlinkEscape(donor.name)}">Delete donor</button></div>` : '';
  return `
    <article class="donor-card">
      <h2>${bloodlinkEscape(donor.name)} <span class="blood-type">${bloodlinkEscape(BLOOD_GROUP_LABELS[donor.bloodGroup] || donor.bloodGroup)}</span></h2>
      <p class="muted">${bloodlinkEscape(donor.city)} · <span class="availability">${donor.available ? 'Available' : 'Unavailable'}</span></p>
      <p class="muted">Last donation: ${bloodlinkEscape(donor.lastDonationDate || 'No previous donation')}</p>
      <p><a href="/donor-details.html?id=${encodeURIComponent(donor.id)}">View donor profile</a></p>
      ${contact}
      ${deleteAction}
    </article>`;
}

function bloodlinkSetStatus(element, message, kind = '') {
  element.textContent = message;
  element.className = `status${kind ? ` ${kind}` : ''}`;
}
