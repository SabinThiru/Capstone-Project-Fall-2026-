// CampusPath auth page login + signup UI
const loginView = document.getElementById("loginView");
const signupView = document.getElementById("signupView");

function showView(view) {
  const showSignup = view === "signup";
  loginView.hidden = showSignup;
  signupView.hidden = !showSignup;
  document.title = showSignup ? "Create account | CampusPath" : "Log in | CampusPath";
}

document.getElementById("showSignup").addEventListener("click", (event) => {
  event.preventDefault();
  showView("signup");
});

document.getElementById("showLogin").addEventListener("click", (event) => {
  event.preventDefault();
  showView("login");
});

// Shows and hides password
document.querySelectorAll(".toggle-password").forEach((btn) => {
  btn.addEventListener("click", () => {
    const input = document.getElementById(btn.dataset.target);
    const showing = input.type === "text";
    input.type = showing ? "password" : "text";
    btn.textContent = showing ? "Show" : "Hide";
    btn.setAttribute("aria-label", showing ? "Show password" : "Hide password");
  });
});

document.querySelectorAll("form").forEach((form) => {
  form.addEventListener("submit", (event) => event.preventDefault());
});

// Logic to check whether passwords match during sign up
const signupPassword = document.getElementById("signupPassword");
const confirmPassword = document.getElementById("confirmPassword");
const passwordMatchError = document.getElementById("passwordMatchError");

function passwordsMatch() {
  const mismatch =
    confirmPassword.value !== "" && signupPassword.value !== confirmPassword.value;
  passwordMatchError.textContent = mismatch ? "Passwords do not match." : "";
  confirmPassword.closest(".field").classList.toggle("has-error", mismatch);
  return !mismatch;
}

//check while user types live
signupPassword.addEventListener("input", passwordsMatch);
confirmPassword.addEventListener("input", passwordsMatch);

//Recheck during submission
document.getElementById("registerForm").addEventListener("submit", () => {
  if (!passwordsMatch()) {
    confirmPassword.focus();
    return; 
  }
});

//Terms and conditions checbox
const agreeTerms = document.getElementById("agreeTerms");
const termsError = document.getElementById("termsError");

agreeTerms.addEventListener("change", () => (termsError.textContent = ""));

document.getElementById("registerForm").addEventListener("submit", () => {
  if (!agreeTerms.checked) {
    termsError.textContent = "You must agree to the terms to create an account.";
  }
});

showView("login");