/**
 * Account security UI module:
 * - Toggle password form section
 * - Toggle password input visibility
 */
export function initAccountPasswordUi() {
    const formWrap = document.getElementById("accountChangePasswordForm");
    const btnToggle = document.getElementById("btnTogglePasswordForm");

    if (formWrap && btnToggle) {
        const toggleText = btnToggle.querySelector(".btn-toggle-password-form__text");

        function updateToggleButton(visible) {
            const iconOpen = btnToggle.querySelector(".btn-toggle-password-form__icon-open");
            const iconClose = btnToggle.querySelector(".btn-toggle-password-form__icon-close");
            if (toggleText) toggleText.style.display = visible ? "none" : "";
            if (iconOpen) iconOpen.style.display = visible ? "none" : "inline-block";
            if (iconClose) iconClose.style.display = visible ? "inline-block" : "none";
            btnToggle.setAttribute("title", visible ? "Đóng" : "Đổi mật khẩu");
            btnToggle.setAttribute("aria-expanded", String(visible));
        }

        if (window.location.hash === "#doi-mat-khau" && !formWrap.classList.contains("account-form-wrap--visible")) {
            formWrap.classList.add("account-form-wrap--visible");
            updateToggleButton(true);
        }

        btnToggle.addEventListener("click", function () {
            const isVisible = formWrap.classList.contains("account-form-wrap--visible");
            formWrap.classList.toggle("account-form-wrap--visible", !isVisible);
            updateToggleButton(!isVisible);
        });

        if (formWrap.classList.contains("account-form-wrap--visible")) {
            updateToggleButton(true);
        }
    }

    document.querySelectorAll(".password-toggle").forEach(function (btn) {
        btn.addEventListener("click", function () {
            const field = btn.closest(".password-field")?.querySelector("input");
            if (!field) return;
            const eye = btn.querySelector(".icon-eye");
            const eyeOff = btn.querySelector(".icon-eye-off");
            if (field.type === "password") {
                field.type = "text";
                if (eye) eye.hidden = true;
                if (eyeOff) eyeOff.hidden = false;
                btn.setAttribute("aria-label", "Ẩn mật khẩu");
                btn.setAttribute("title", "Ẩn mật khẩu");
            } else {
                field.type = "password";
                if (eye) eye.hidden = false;
                if (eyeOff) eyeOff.hidden = true;
                btn.setAttribute("aria-label", "Hiện mật khẩu");
                btn.setAttribute("title", "Hiện mật khẩu");
            }
        });
    });
}
