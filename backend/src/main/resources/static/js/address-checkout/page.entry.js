import { bootstrapCheckoutStep4 } from "./checkout-step4.bootstrap.js";
import { bootstrapAccountAddressBook } from "./account-step4.bootstrap.js";
import { initAccountPasswordUi } from "./account-password.module.js";

document.addEventListener("DOMContentLoaded", async function () {
    if (document.getElementById("checkoutForm")) {
        try {
            await bootstrapCheckoutStep4();
        } catch (err) {
            console.error("Checkout bootstrap failed:", err);
        }
    }

    if (document.getElementById("waAddressListRoot")) {
        try {
            await bootstrapAccountAddressBook();
        } catch (err) {
            console.error("Account address bootstrap failed:", err);
        }
    }

    if (document.getElementById("accountChangePasswordForm")) {
        initAccountPasswordUi();
    }
});
