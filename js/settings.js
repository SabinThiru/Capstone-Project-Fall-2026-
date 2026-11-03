(function () {
  var KEY = "campuspath-theme";
  var root = document.documentElement;

  // Light and Dark Themes
  function clean(value) {
    return value === "light" ? "light" : "dark";
  }

  function readSaved() {
    try {
      return clean(localStorage.getItem(KEY));
    } catch (e) {
      return "dark";
    }
  }

  // Show a theme without saving for live preview
  function preview(value) {
    root.setAttribute("data-theme", clean(value));
  }

  // Remembers theme for every page
  function save(value) {
    var theme = clean(value);
    preview(theme);
    try {
      localStorage.setItem(KEY, theme);
    } catch (e) {}
  }

  preview(readSaved());

  window.CampusPathTheme = { preview: preview, save: save, saved: readSaved };
})();

function initSettingsPage() {
  const form = document.getElementById("settingsForm");
  if (!form) return; // not the settings page

  const API_ROOT = "http://localhost:8080/api";

  const TIMEZONES = [
    ["America/New_York", "Eastern Time"],
    ["America/Chicago", "Central Time"],
    ["America/Denver", "Mountain Time"],
    ["America/Los_Angeles", "Pacific Time"],
    ["America/Anchorage", "Alaska Time"],
    ["Pacific/Honolulu", "Hawaii Time"]
  ];

  const REMINDER_OPTIONS = [
    [10, "10 minutes before"],
    [30, "30 minutes before"],
    [60, "1 hour before"],
    [1440, "1 day before"]
  ];

  // Top bar
  const navUserName = document.getElementById("navUserName");
  const userAvatar = document.getElementById("userAvatar");

  // Section 1: account
  const profileForm = document.getElementById("profileForm");
  const fullNameInput = document.getElementById("fullName");
  const accountEmail = document.getElementById("accountEmail");
  const accountUniversity = document.getElementById("accountUniversity");
  const accountStudentId = document.getElementById("accountStudentId");
  const profileSaveBtn = document.getElementById("profileSaveBtn");
  const profileStatus = document.getElementById("profileStatus");

  const passwordForm = document.getElementById("passwordForm");
  const currentPassword = document.getElementById("currentPassword");
  const newPassword = document.getElementById("newPassword");
  const confirmPassword = document.getElementById("confirmPassword");
  const passwordMatchError = document.getElementById("passwordMatchError");
  const passwordSaveBtn = document.getElementById("passwordSaveBtn");
  const passwordIntro = document.getElementById("passwordIntro");
  const changePasswordBtn = document.getElementById("changePasswordBtn");
  const cancelPasswordBtn = document.getElementById("cancelPasswordBtn");
  const passwordDone = document.getElementById("passwordDone");
  const passwordStatus = document.getElementById("passwordStatus");
  const ruleItems = {
    length: document.querySelector('#pwRules [data-rule="length"]'),
    special: document.querySelector('#pwRules [data-rule="special"]')
  };

  // Section 2: preferences
  const themeSelect = document.getElementById("theme");
  const timezoneSelect = document.getElementById("timezone");
  const notificationsToggle = document.getElementById("notificationsEnabled");
  const emailToggle = document.getElementById("emailRemindersEnabled");
  const reminderSelect = document.getElementById("defaultReminderMinutes");
  const saveBtn = document.getElementById("saveBtn");
  const formStatus = document.getElementById("formStatus");

  function setStatus(element, message, type) {
    element.textContent = message;
    element.className = "form-status " + (type || "");
  }

  // Fills dropdown with [value, label] pairs
  function fillSelect(select, options) {
    select.innerHTML = "";
    options.forEach(([value, label]) => {
      const option = document.createElement("option");
      option.value = value;
      option.textContent = label;
      select.appendChild(option);
    });
  }

  function selectValue(select, value, fallbackLabel) {
    const exists = Array.from(select.options).some((o) => o.value === String(value));
    if (!exists) {
      const option = document.createElement("option");
      option.value = value;
      option.textContent = fallbackLabel;
      select.appendChild(option);
    }
    select.value = value;
  }

  function authError(message) {
    const error = new Error(message);
    error.name = "AuthError";
    return error;
  }

  function goToLogin() {
    sessionStorage.removeItem("idToken");
    window.location.replace("login.html");
  }
  async function api(path, options = {}) {
    const idToken = sessionStorage.getItem("idToken");
    if (!idToken) throw authError("Not logged in");

    const response = await fetch(API_ROOT + path, {
      ...options,
      headers: {
        Authorization: `Bearer ${idToken}`,
        "Content-Type": "application/json",
        ...(options.headers || {})
      }
    });

    if (response.status === 401) throw authError("Session expired");
    return response;
  }

  async function errorMessage(response, fallback) {
    const problem = await response.json().catch(() => ({}));
    return problem.detail || fallback;
  }

  function showName(fullName) {
    navUserName.textContent = fullName;
    userAvatar.textContent = fullName.charAt(0).toUpperCase();
  }

  async function loadProfile() {
    try {
      const response = await api("/users/me");
      if (!response.ok) return;
      const profile = await response.json();
      showName(profile.fullName);
      fullNameInput.value = profile.fullName;
      accountEmail.textContent = profile.email || "—";
      accountUniversity.textContent = (profile.university && profile.university.name) || "—";
      accountStudentId.textContent = profile.euid || "—";
    } catch (error) {
      console.error("Could not load profile:", error);
    }
  }

  // Preferences section
  async function loadSettings() {
    try {
      const response = await api("/users/me/settings");
      if (!response.ok) throw new Error(`Status ${response.status}`);
      const settings = await response.json();

      CampusPathTheme.save(settings.theme);
      themeSelect.value = CampusPathTheme.saved();
      selectValue(timezoneSelect, settings.timezone, settings.timezone);
      notificationsToggle.checked = settings.notificationsEnabled;
      emailToggle.checked = settings.emailRemindersEnabled;
      selectValue(
        reminderSelect,
        settings.defaultReminderMinutes,
        `${settings.defaultReminderMinutes} minutes before`
      );
    } catch (error) {
      if (error.name === "AuthError") return;
      setStatus(formStatus, "Could not load your settings. Check that the backend is running, then reload.", "error");
      console.error("Could not load settings:", error);
    }
  }

  profileForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const fullName = fullNameInput.value.trim();

    if (!fullName) {
      setStatus(profileStatus, "Enter your name.", "error");
      return;
    }

    profileSaveBtn.disabled = true;
    setStatus(profileStatus, "Saving...", "");

    try {
      const response = await api("/users/me", {
        method: "PUT",
        body: JSON.stringify({ fullName })
      });

      if (!response.ok) {
        setStatus(profileStatus, await errorMessage(response, "Could not update your name. Try again."), "error");
        return;
      }

      const profile = await response.json().catch(() => ({}));
      const savedName = profile.fullName || fullName;
      showName(savedName);
      fullNameInput.value = savedName;
      setStatus(profileStatus, "Name updated.", "success");
    } catch (error) {
      if (error.name === "AuthError") {
        setStatus(profileStatus, "", "");
        return;
      }
      setStatus(profileStatus, "Could not reach the server. Check your connection and try again.", "error");
      console.error("Could not update name:", error);
    } finally {
      profileSaveBtn.disabled = false;
    }
  });

  // Password change section - same rules apply as in sign-up page
  function checkPasswordRules() {
    const value = newPassword.value;
    const hasLength = value.length >= 8;
    const hasSpecial = /[^A-Za-z0-9\s]/.test(value);
    ruleItems.length.classList.toggle("met", hasLength);
    ruleItems.special.classList.toggle("met", hasSpecial);
    return hasLength && hasSpecial;
  }

  function checkPasswordsMatch() {
    const mismatch = confirmPassword.value !== "" && newPassword.value !== confirmPassword.value;
    passwordMatchError.textContent = mismatch ? "Passwords do not match." : "";
    return !mismatch;
  }

  newPassword.addEventListener("input", () => {
    checkPasswordRules();
    checkPasswordsMatch();
  });
  confirmPassword.addEventListener("input", checkPasswordsMatch);

  // Shows and hides password buttons
  document.querySelectorAll(".show-toggle").forEach((button) => {
    button.addEventListener("click", () => {
      const input = document.getElementById(button.dataset.target);
      const showing = input.type === "text";
      input.type = showing ? "password" : "text";
      button.textContent = showing ? "Show" : "Hide";
      button.setAttribute("aria-label", showing ? "Show password" : "Hide password");
    });
  });

  function openPasswordForm() {
    setStatus(passwordDone, "", "");
    setStatus(passwordStatus, "", "");
    passwordIntro.hidden = true;
    passwordForm.hidden = false;
    changePasswordBtn.setAttribute("aria-expanded", "true");
    currentPassword.focus();
  }

  function closePasswordForm() {
    passwordForm.reset();
    document.querySelectorAll("#passwordForm .show-toggle").forEach((button) => {
      document.getElementById(button.dataset.target).type = "password";
      button.textContent = "Show";
      button.setAttribute("aria-label", "Show password");
    });
    checkPasswordRules();
    passwordMatchError.textContent = "";
    setStatus(passwordStatus, "", "");
    passwordForm.hidden = true;
    passwordIntro.hidden = false;
    changePasswordBtn.setAttribute("aria-expanded", "false");
  }

  changePasswordBtn.addEventListener("click", openPasswordForm);
  cancelPasswordBtn.addEventListener("click", () => {
    closePasswordForm();
    changePasswordBtn.focus();
  });

  passwordForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!currentPassword.value) {
      setStatus(passwordStatus, "Enter your current password.", "error");
      return;
    }
    if (!checkPasswordRules()) {
      setStatus(passwordStatus, "Your new password needs at least 8 characters and one special character.", "error");
      return;
    }
    if (newPassword.value !== confirmPassword.value) {
      setStatus(passwordStatus, "The new passwords do not match.", "error");
      return;
    }
    if (newPassword.value === currentPassword.value) {
      setStatus(passwordStatus, "Choose a password that is different from your current one.", "error");
      return;
    }

    passwordSaveBtn.disabled = true;
    setStatus(passwordStatus, "Updating...", "");

    try {
      const response = await api("/auth/change-password", {
        method: "POST",
        body: JSON.stringify({
          currentPassword: currentPassword.value,
          newPassword: newPassword.value
        })
      });

      if (!response.ok) {
        setStatus(passwordStatus, await errorMessage(response, "Could not update your password. Try again."), "error");
        return;
      }

      closePasswordForm();
      setStatus(passwordDone, "Password updated.", "success");
    } catch (error) {
      if (error.name === "AuthError") {
        setStatus(passwordStatus, "", "");
        return;
      }
      setStatus(passwordStatus, "Could not reach the server. Check your connection and try again.", "error");
      console.error("Could not update password:", error);
    } finally {
      passwordSaveBtn.disabled = false;
    }
  });

  // Theme shows saved choice and previews it live
  themeSelect.value = CampusPathTheme.saved();
  themeSelect.addEventListener("change", () => {
    CampusPathTheme.preview(themeSelect.value);
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    saveBtn.disabled = true;
    setStatus(formStatus, "Saving...", "");

    const body = {
      theme: themeSelect.value,
      timezone: timezoneSelect.value,
      notificationsEnabled: notificationsToggle.checked,
      emailRemindersEnabled: emailToggle.checked,
      defaultReminderMinutes: Number(reminderSelect.value)
    };

    // Remembers the theme on the device
    CampusPathTheme.save(body.theme);

    try {
      const response = await api("/users/me/settings", {
        method: "PUT",
        body: JSON.stringify(body)
      });

      if (!response.ok) {
        setStatus(formStatus, await errorMessage(response, "Could not save your settings. Try again."), "error");
        return;
      }
      setStatus(formStatus, "Settings saved.", "success");
    } catch (error) {
      if (error.name === "AuthError") {
        setStatus(formStatus, "", "");
        return;
      }
      setStatus(formStatus, "Could not reach the server. Check your connection and try again.", "error");
      console.error("Could not save settings:", error);
    } finally {
      saveBtn.disabled = false;
    }
  });

  form.addEventListener("input", () => {
    if (formStatus.classList.contains("success")) setStatus(formStatus, "", "");
  });
  profileForm.addEventListener("input", () => {
    if (profileStatus.classList.contains("success")) setStatus(profileStatus, "", "");
  });
  passwordForm.addEventListener("input", () => {
    if (passwordStatus.classList.contains("success")) setStatus(passwordStatus, "", "");
  });

  document.getElementById("logoutLink").addEventListener("click", (event) => {
    event.preventDefault();
    goToLogin();
  });

  // Section 3: Schedule preferences
  const scheduleForm = document.getElementById("scheduleForm");
  const earliestInput = document.getElementById("earliestClassTime");
  const latestInput = document.getElementById("latestClassTime");
  const maxCreditsInput = document.getElementById("maxCredits");
  const minimizeGapsToggle = document.getElementById("minimizeGaps");
  const minimizeCampusDaysToggle = document.getElementById("minimizeCampusDays");
  const scheduleSaveBtn = document.getElementById("scheduleSaveBtn");
  const scheduleStatus = document.getElementById("scheduleStatus");
  const dayBoxes = Array.from(document.querySelectorAll('#scheduleForm input[name="preferredDays"]'));

  const SCHEDULE_DEFAULTS = {
    earliestClassTime: "08:00",
    latestClassTime: "18:00",
    maxCredits: 15,
    preferredDays: ["MON", "TUE", "WED", "THU", "FRI"],
    minimizeGaps: false,
    minimizeCampusDays: false
  };

  function fillSchedulePreferences(saved) {
    const prefs = { ...SCHEDULE_DEFAULTS };
    Object.keys(SCHEDULE_DEFAULTS).forEach((key) => {
      if (saved[key] !== undefined && saved[key] !== null) prefs[key] = saved[key];
    });

    earliestInput.value = String(prefs.earliestClassTime).slice(0, 5);
    latestInput.value = String(prefs.latestClassTime).slice(0, 5);
    maxCreditsInput.value = prefs.maxCredits;
    minimizeGapsToggle.checked = Boolean(prefs.minimizeGaps);
    minimizeCampusDaysToggle.checked = Boolean(prefs.minimizeCampusDays);

    const days = Array.isArray(prefs.preferredDays)
      ? prefs.preferredDays
      : String(prefs.preferredDays).split(",");
    const chosen = days.map((day) => day.trim().toUpperCase());
    dayBoxes.forEach((box) => {
      box.checked = chosen.includes(box.value);
    });
  }

  async function loadSchedulePreferences() {
    fillSchedulePreferences({});
    try {
      const response = await api("/users/me/schedule-preferences");
      if (response.status === 404) return; // keeps defaults without saving
      if (!response.ok) throw new Error(`Status ${response.status}`);
      fillSchedulePreferences(await response.json());
    } catch (error) {
      if (error.name === "AuthError") return;
      console.error("Could not load schedule preferences:", error);
    }
  }

  scheduleForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const earliest = earliestInput.value;
    const latest = latestInput.value;
    const maxCredits = Number(maxCreditsInput.value);

    if (!earliest || !latest) {
      setStatus(scheduleStatus, "Choose an earliest and a latest class time.", "error");
      return;
    }
    if (earliest >= latest) {
      setStatus(scheduleStatus, "The earliest class time must be before the latest.", "error");
      return;
    }
    if (!Number.isInteger(maxCredits) || maxCredits < 3 || maxCredits > 21) {
      setStatus(scheduleStatus, "Maximum credits must be a whole number from 3 to 21.", "error");
      return;
    }

    scheduleSaveBtn.disabled = true;
    setStatus(scheduleStatus, "Saving...", "");

    const body = {
      earliestClassTime: earliest,
      latestClassTime: latest,
      maxCredits,
      preferredDays: dayBoxes.filter((box) => box.checked).map((box) => box.value),
      minimizeGaps: minimizeGapsToggle.checked,
      minimizeCampusDays: minimizeCampusDaysToggle.checked
    };

    try {
      const response = await api("/users/me/schedule-preferences", {
        method: "PUT",
        body: JSON.stringify(body)
      });

      if (!response.ok) {
        setStatus(scheduleStatus, await errorMessage(response, "Could not save your schedule preferences. Try again."), "error");
        return;
      }
      setStatus(scheduleStatus, "Schedule preferences saved.", "success");
    } catch (error) {
      if (error.name === "AuthError") {
        setStatus(scheduleStatus, "", "");
        return;
      }
      setStatus(scheduleStatus, "Could not reach the server. Check your connection and try again.", "error");
      console.error("Could not save schedule preferences:", error);
    } finally {
      scheduleSaveBtn.disabled = false;
    }
  });

  scheduleForm.addEventListener("input", () => {
    if (scheduleStatus.classList.contains("success")) setStatus(scheduleStatus, "", "");
  });

  fillSelect(timezoneSelect, TIMEZONES);
  fillSelect(reminderSelect, REMINDER_OPTIONS);
  loadProfile();
  loadSettings();
  loadSchedulePreferences();
}

document.addEventListener("DOMContentLoaded", initSettingsPage);