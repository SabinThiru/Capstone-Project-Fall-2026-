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

