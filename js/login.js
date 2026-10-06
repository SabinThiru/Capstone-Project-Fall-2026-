// CampusPath login page UI
const form = document.getElementById("loginForm");
const passwordInput = document.getElementById("password");
const toggleBtn = document.getElementById("togglePassword");

// Shows and hides password
toggleBtn.addEventListener("click", () => {
  const showing = passwordInput.type === "text";
  passwordInput.type = showing ? "password" : "text";
  toggleBtn.textContent = showing ? "Show" : "Hide";
  toggleBtn.setAttribute("aria-label", showing ? "Show password" : "Hide password");
});

form.addEventListener("submit", (event) => {
  event.preventDefault();
});