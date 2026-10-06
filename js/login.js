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

showView("login");