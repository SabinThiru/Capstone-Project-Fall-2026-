const requestView = document.getElementById("requestView");
const sentView = document.getElementById("sentView");
const forgotForm = document.getElementById("forgotForm");
const forgotEmail = document.getElementById("forgotEmail");
const forgotEmailField = document.getElementById("forgotEmailField");
const forgotEmailError = document.getElementById("forgotEmailError");
const sendResetBtn = document.getElementById("sendResetBtn");
const sendResetLabel = sendResetBtn.querySelector(".btn-label");
const sentEmail = document.getElementById("sentEmail");
const resendBtn = document.getElementById("resendBtn");
const changeEmailBtn = document.getElementById("changeEmailBtn");
const RESEND_SECONDS = 30;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
let resendTimer = null;

function requestPasswordReset(email) {
  return new Promise((resolve) => setTimeout(resolve, 800));
}

/* Helpers */

function showView(name) {
  const showSent = name === "sent";
  requestView.hidden = showSent;
  sentView.hidden = !showSent;
  document.title = showSent
    ? "Check your email | CampusPath"
    : "Forgot password | CampusPath";
}

function setError(message) {
  forgotEmailError.textContent = message;
  forgotEmailField.classList.toggle("has-error", Boolean(message));
  forgotEmail.setAttribute("aria-invalid", message ? "true" : "false");
}

function validateEmail() {
  const value = forgotEmail.value.trim();
  if (!value) {
    setError("Please enter your email address.");
    return false;
  }
  if (!EMAIL_PATTERN.test(value)) {
    setError("Please enter a valid email address.");
    return false;
  }
  setError("");
  return true;
}

function setSending(sending) {
  sendResetBtn.disabled = sending;
  sendResetLabel.textContent = sending ? "Sending…" : "Request reset link";
}

function startResendCooldown() {
  clearInterval(resendTimer);
  let remaining = RESEND_SECONDS;
  resendBtn.disabled = true;
  resendBtn.textContent = `resend in ${remaining}s`;

  resendTimer = setInterval(() => {
    remaining -= 1;
    if (remaining <= 0) {
      clearInterval(resendTimer);
      resendBtn.disabled = false;
      resendBtn.textContent = "resend the email";
    } else {
      resendBtn.textContent = `resend in ${remaining}s`;
    }
  }, 1000);
}