/* Reusable confirm modal (admin + user).
   Usage:
     window.confirmModal({ title, message, confirmText, cancelText, tone })
       .then((ok) => { ... });

   Also supports declarative attributes:
     - data-confirm="Message"
     - data-confirm-title="..."
     - data-confirm-ok="..."
     - data-confirm-cancel="..."
     - data-confirm-tone="danger|info"

   Works for <form> submit buttons/links:
     - Put data-confirm on <form> or on <button type="submit"> or on <a>.
*/
(function () {
  if (window.confirmModal) return;

  function ensureModal() {
    var existing = document.getElementById('appConfirmModal');
    if (existing) return existing;

    var modal = document.createElement('div');
    modal.id = 'appConfirmModal';
    modal.className = 'confirm-modal';
    modal.setAttribute('aria-hidden', 'true');
    modal.innerHTML =
      '<div class="confirm-modal__backdrop" data-confirm-close="true"></div>' +
      '<div class="confirm-modal__panel" role="dialog" aria-modal="true" aria-labelledby="appConfirmModalTitle">' +
      '  <div class="confirm-modal__header">' +
      '    <div class="confirm-modal__icon" aria-hidden="true"></div>' +
      '    <h3 class="confirm-modal__title" id="appConfirmModalTitle">Xác nhận</h3>' +
      '    <button type="button" class="confirm-modal__close" aria-label="Đóng" data-confirm-close="true">×</button>' +
      '  </div>' +
      '  <div class="confirm-modal__body">' +
      '    <div class="confirm-modal__message" id="appConfirmModalMessage"></div>' +
      '  </div>' +
      '  <div class="confirm-modal__actions">' +
      '    <button type="button" class="btn btn--secondary confirm-modal__cancel" data-confirm-cancel="true">Hủy</button>' +
      '    <button type="button" class="btn btn--primary confirm-modal__ok" data-confirm-ok="true">Xác nhận</button>' +
      '  </div>' +
      '</div>';

    document.body.appendChild(modal);
    return modal;
  }

  function setTone(modal, tone) {
    tone = tone || 'info';
    modal.classList.remove('confirm-modal--info', 'confirm-modal--danger');
    modal.classList.add(tone === 'danger' ? 'confirm-modal--danger' : 'confirm-modal--info');
  }

  function openModal(opts) {
    var modal = ensureModal();
    var titleEl = document.getElementById('appConfirmModalTitle');
    var msgEl = document.getElementById('appConfirmModalMessage');
    var okBtn = modal.querySelector('[data-confirm-ok="true"]');
    var cancelBtn = modal.querySelector('[data-confirm-cancel="true"]');

    if (!okBtn || !cancelBtn) return Promise.resolve(false);

    setTone(modal, opts && opts.tone);
    if (titleEl) titleEl.textContent = (opts && opts.title) ? String(opts.title) : 'Xác nhận';
    if (msgEl) msgEl.textContent = (opts && opts.message) ? String(opts.message) : 'Bạn có chắc chắn muốn thực hiện thao tác này?';
    okBtn.textContent = (opts && opts.confirmText) ? String(opts.confirmText) : 'Xác nhận';
    cancelBtn.textContent = (opts && opts.cancelText) ? String(opts.cancelText) : 'Hủy';

    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.classList.add('confirm-modal-open');

    // focus management: focus OK by default for faster keyboard
    try { okBtn.focus(); } catch (e) {}

    return new Promise(function (resolve) {
      var done = false;

      function cleanup() {
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('confirm-modal-open');
        modal.removeEventListener('click', onClick, true);
        document.removeEventListener('keydown', onKeydown, true);
      }

      function finish(result) {
        if (done) return;
        done = true;
        cleanup();
        resolve(!!result);
      }

      function onClick(e) {
        var t = e && e.target ? e.target : null;
        if (!t) return;
        if (t.getAttribute && t.getAttribute('data-confirm-close') === 'true') return finish(false);
        if (t.getAttribute && t.getAttribute('data-confirm-cancel') === 'true') return finish(false);
        if (t.getAttribute && t.getAttribute('data-confirm-ok') === 'true') return finish(true);
      }

      function onKeydown(e) {
        if (!modal.classList.contains('is-open')) return;
        if (e.key === 'Escape') return finish(false);
        if (e.key === 'Enter') {
          // Enter should confirm only if focus is inside modal
          var active = document.activeElement;
          if (active && modal.contains(active)) return finish(true);
        }
      }

      modal.addEventListener('click', onClick, true);
      document.addEventListener('keydown', onKeydown, true);
    });
  }

  window.confirmModal = openModal;

  function getConfirmOptionsFromEl(el) {
    if (!el || !el.getAttribute) return null;
    var msg = el.getAttribute('data-confirm');
    if (!msg) return null;
    return {
      title: el.getAttribute('data-confirm-title') || 'Xác nhận',
      message: msg,
      confirmText: el.getAttribute('data-confirm-ok') || 'Xác nhận',
      cancelText: el.getAttribute('data-confirm-cancel') || 'Hủy',
      tone: el.getAttribute('data-confirm-tone') || 'danger'
    };
  }

  // Declarative handling for links
  document.addEventListener('click', function (e) {
    var t = e && e.target ? e.target : null;
    if (!t) return;
    if (t.nodeType === 3) t = t.parentElement;
    if (!t) return;
    var link = t.closest ? t.closest('a[data-confirm]') : null;
    if (!link) return;
    if (link.getAttribute('data-confirm-disabled') === 'true') return;

    e.preventDefault();
    var href = link.getAttribute('href');
    var opts = getConfirmOptionsFromEl(link);
    openModal(opts).then(function (ok) {
      if (!ok) return;
      if (href) window.location.href = href;
    });
  }, true);

  // Declarative handling for form submit (capture phase to run before other handlers)
  document.addEventListener('submit', function (e) {
    var form = e.target;
    if (!form || !form.getAttribute) return;

    // Find confirm options on submitter, then form
    var submitter = e.submitter || null;
    var opts = getConfirmOptionsFromEl(submitter) || getConfirmOptionsFromEl(form);
    if (!opts) return;

    // Avoid double-confirm loops
    if (form.__confirmBypassedOnce) {
      form.__confirmBypassedOnce = false;
      return;
    }

    // Keep UX consistent for select-driven submit (e.g. onchange on status dropdown):
    // if user cancels confirmation, restore previous displayed value.
    var activeEl = document.activeElement;
    var revertSelect = null;
    var prevValue = null;
    if (
      activeEl &&
      form.contains(activeEl) &&
      activeEl.tagName === 'SELECT'
    ) {
      revertSelect = activeEl;
      prevValue = activeEl.getAttribute('data-confirm-prev');
      if (prevValue === null || prevValue === undefined) {
        prevValue = activeEl.defaultValue;
      }
    }

    e.preventDefault();
    form.__confirmPending = true;
    openModal(opts).then(function (ok) {
      form.__confirmPending = false;
      if (!ok) {
        if (revertSelect && prevValue !== null && prevValue !== undefined) {
          try { revertSelect.value = prevValue; } catch (err) {}
        }
        return;
      }
      // bypass once then submit programmatically
      form.__confirmBypassedOnce = true;
      try {
        form.requestSubmit ? form.requestSubmit(submitter || undefined) : form.submit();
      } catch (err) {
        try { form.submit(); } catch (e2) {}
      }

      // Update remembered previous value after confirmed submit attempt.
      if (revertSelect) {
        try { revertSelect.setAttribute('data-confirm-prev', revertSelect.value); } catch (err) {}
      }
    });
  }, true);

  // Track previous value on focus for select elements.
  document.addEventListener('focusin', function (e) {
    var el = e && e.target ? e.target : null;
    if (!el || el.tagName !== 'SELECT') return;
    try {
      el.setAttribute('data-confirm-prev', el.value);
    } catch (err) {}
  });
})();
