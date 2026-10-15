// CampusPath auth page login + signup UI

const API_BASE_URL = "http://localhost:8080";

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

    await ensureStudentProfile(
      tokens.idToken,
      document.getElementById("loginEmail").value.trim()
    );

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

const registerForm = document.getElementById("registerForm");
const agreeTerms = document.getElementById("agreeTerms");

const registerError = document.createElement("p");
registerError.className = "error-text";
registerError.setAttribute("role", "alert");
registerForm.appendChild(registerError);

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  registerError.textContent = "";

  if (!passwordsMatch()) {
    confirmPassword.focus();
    return;
  }

  if (!agreeTerms.checked) {
    registerError.textContent = "Please agree to the terms.";
    return;
  }

  const email = document.getElementById("signupEmail").value.trim();
  const fullName = [
    document.getElementById("firstName").value.trim(),
    document.getElementById("lastName").value.trim()
  ].join(" ");

  const universityId = Number(
    document.getElementById("university").value
  );
  const euid = document.getElementById("studentId").value.trim();

  if (!universityId || !euid || !fullName.trim()) {
    registerError.textContent =
      "Please enter your name, university, and EUID.";
    return;
  }

  if (fullName.length > 200 || euid.length > 50) {
    registerError.textContent =
      "Name must be at most 200 characters and EUID at most 50.";
    return;
  }

  const button = registerForm.querySelector('button[type="submit"]');
  button.disabled = true;

  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email,
        password: signupPassword.value,
        fullName
      })
    });

    if (!response.ok) {
      throw new Error(
        `Signup failed (${response.status}). Check your details.`
      );
    }

    const result = await response.json();

    // Keep profile details for the RDS step after login.
    // Never store the password here.
    sessionStorage.setItem("pendingProfile", JSON.stringify({
      email,
      fullName,
      universityId,
      euid
    }));

    signupPassword.value = "";
    confirmPassword.value = "";
    checkPasswordRules();
    passwordsMatch();

    document.getElementById("loginEmail").value = email;

    if (result.confirmationRequired) {
      document.getElementById("verifyEmail").value = email;
      document.getElementById("verifyCode").value = "";
      document.getElementById("verifyError").textContent = "";
      showView("verify");
    } else {
      showView("login");
      loginError.textContent = "Account created. You can now log in.";
    }
  } catch (error) {
    registerError.textContent =
      error instanceof TypeError
        ? "Cannot reach the backend. Check that it is running."
        : error.message;
  } finally {
    button.disabled = false;
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



async function loadUniversities() {
  const dropdown = document.getElementById("university");
  dropdown.disabled = true;
  dropdown.replaceChildren(new Option("Loading universities...", ""));

  try {
    const response = await fetch(`${API_BASE_URL}/api/universities`);

    if (!response.ok) {
      throw new Error(`Could not load universities (${response.status}).`);
    }

    const universities = await response.json();

    dropdown.replaceChildren(new Option("Select your university", ""));

    universities.forEach((university) => {
      dropdown.add(
        new Option(
          `${university.name} (${university.abbreviation})`,
          university.id
        )
      );
    });

    dropdown.disabled = false;
  } catch (error) {
    dropdown.replaceChildren(
      new Option("Unable to load universities. Please reload.", "")
    );
    console.error("University loading failed:", error);
  }
}

loadUniversities();

const verifyForm = document.getElementById("verifyForm");
const verifyError = document.getElementById("verifyError");

verifyForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  verifyError.textContent = "";

  const email = document.getElementById("verifyEmail").value.trim();
  const confirmationCode =
    document.getElementById("verifyCode").value.trim();

  if (!confirmationCode) {
    verifyError.textContent = "Enter the code from your email.";
    return;
  }

  const button = verifyForm.querySelector('button[type="submit"]');
  button.disabled = true;

  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/confirm`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, confirmationCode })
    });

    if (!response.ok) {
      throw new Error(
        `Verification failed (${response.status}). Check your email and code.`
      );
    }

    // This endpoint returns plain text, not JSON.
    const message = await response.text();

    document.getElementById("verifyCode").value = "";
    document.getElementById("loginEmail").value = email;
    showView("login");
    loginError.textContent = message;
  } catch (error) {
    verifyError.textContent =
      error instanceof TypeError
        ? "Cannot reach the backend. Check that it is running."
        : error.message;
  } finally {
    button.disabled = false;
  }
});

async function ensureStudentProfile(idToken, email) {
  const headers = {
    Authorization: `Bearer ${idToken}`
  };

  const existing = await fetch(`${API_BASE_URL}/api/users/me`, {
    headers
  });

  // Returning students already have a profile.
  if (existing.ok) {
    return;
  }

  // Only a missing profile should trigger creation.
  if (existing.status !== 404) {
    throw new Error(`Could not load your profile (${existing.status}).`);
  }

  const pending = JSON.parse(
    sessionStorage.getItem("pendingProfile") || "null"
  );

  if (
    !pending ||
    pending.email.toLowerCase() !== email.toLowerCase()
  ) {
    throw new Error(
      "Your account exists, but your student profile is incomplete."
    );
  }

  const response = await fetch(`${API_BASE_URL}/api/users/me`, {
    method: "POST",
    headers: {
      ...headers,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      universityId: pending.universityId,
      fullName: pending.fullName,
      euid: pending.euid
    })
  });

  if (!response.ok) {
    const problem = await response.json().catch(() => null);
    throw new Error(
      problem?.detail ||
      `Could not create your profile (${response.status}).`
    );
  }

  sessionStorage.removeItem("pendingProfile");
  
  
}

