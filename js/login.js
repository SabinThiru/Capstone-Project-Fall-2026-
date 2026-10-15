// CampusPath auth page login + signup UI
const loginView = document.getElementById("loginView");
const signupView = document.getElementById("signupView");
const verifyView = document.getElementById("verifyView");

function showView(view) {
  loginView.hidden = view !== "login";
  signupView.hidden = view !== "signup";
  verifyView.hidden = view !== "verify";

  const titles = {
    login: "Log in",
    signup: "Create account",
    verify: "Verify email"
  };

  document.title = `${titles[view]} | CampusPath`;
}

document.getElementById("showSignup").addEventListener("click", (event) => {
  event.preventDefault();
  showView("signup");
});

document.getElementById("showLogin").addEventListener("click", (event) => {
  event.preventDefault();
  showView("login");
});

document.getElementById("showLoginFromVerify")
  .addEventListener("click", (event) => {
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

const loginForm = document.getElementById("loginForm");

const loginError = document.createElement("p");
loginError.className = "error-text";
loginError.setAttribute("role", "alert");
loginForm.appendChild(loginError);

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const button = loginForm.querySelector('button[type="submit"]');
  button.disabled = true;
  loginError.textContent = "";
  sessionStorage.removeItem("idToken");

  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        email: document.getElementById("loginEmail").value.trim(),
        password: document.getElementById("loginPassword").value
      })
    });

    if (!response.ok) {
      throw new Error(
        response.status === 401
          ? "Login failed. Check your credentials and verify your email."
          : `Login failed (${response.status}). Please try again.`
      );
    }

    const tokens = await response.json();

    if (!tokens.idToken) {
      throw new Error("Login did not return an ID token.");
    }

    sessionStorage.setItem("idToken", tokens.idToken);
    document.getElementById("loginPassword").value = "";
    window.location.href = "dashboard.html";
  } catch (error) {
    loginError.textContent =
      error instanceof TypeError
        ? "Cannot reach the backend. Check that it is running."
        : error.message;
  } finally {
    button.disabled = false;
  }
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

//Password requirements
const rulePassword = document.getElementById("signupPassword");
const ruleItems = {
  length: document.querySelector('#passwordRules [data-rule="length"]'),
  special: document.querySelector('#passwordRules [data-rule="special"]'),
};

function checkPasswordRules() {
  const value = rulePassword.value;
  const hasLength = value.length >= 8;
  const hasSpecial = /[^A-Za-z0-9\s]/.test(value);

  ruleItems.length.classList.toggle("met", hasLength);
  ruleItems.special.classList.toggle("met", hasSpecial);

  // Makes the browser block submission until both rules are met
  rulePassword.setCustomValidity(
    hasLength && hasSpecial
      ? ""
      : "Password must be at least 8 characters and include one special character."
  );
}

rulePassword.addEventListener("input", checkPasswordRules);
checkPasswordRules();

showView("login");