(function () {
  var KEY = "campuspath-theme";
  var root = document.documentElement;

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

  // Show a theme when user selects it, but don't save it yet, so they can preview before saving
  function preview(value) {
    root.setAttribute("data-theme", clean(value));
  }

  // Show a theme and remember it for every page
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


// ===== Settings page =====
function initSettingsPage() {
  const API_BASE = "http://localhost:8080/api/users/me";

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

  const form = document.getElementById("settingsForm");
  if (!form) return; // not the settings page
  const themeSelect = document.getElementById("theme");
  const timezoneSelect = document.getElementById("timezone");
  const notificationsToggle = document.getElementById("notificationsEnabled");
  const emailToggle = document.getElementById("emailRemindersEnabled");
  const reminderSelect = document.getElementById("defaultReminderMinutes");
  const saveBtn = document.getElementById("saveBtn");
  const formStatus = document.getElementById("formStatus");

  const navUserName = document.getElementById("navUserName");
  const userAvatar = document.getElementById("userAvatar");

  // show saved theme, but preview changes before saving
  themeSelect.value = CampusPathTheme.saved();
  themeSelect.addEventListener("change", () => {
    CampusPathTheme.preview(themeSelect.value);
  });

  // Fill a dropdown with [value, label] pairs
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

  function showStatus(message, type) {
    formStatus.textContent = message;
    formStatus.className = "form-status " + (type || "");
  }
  function goToLogin() {
    sessionStorage.removeItem("idToken");
    window.location.replace("login.html");
  }

  function authError(message) {
    const error = new Error(message);
    error.name = "AuthError";
    return error;
  }

  // Call the backend with the saved login token
  async function api(path, options = {}) {
    const idToken = sessionStorage.getItem("idToken");
    if (!idToken) {
      throw authError("Not logged in");
    }

    const response = await fetch(API_BASE + path, {
      ...options,
      headers: {
        Authorization: `Bearer ${idToken}`,
        "Content-Type": "application/json",
        ...(options.headers || {})
      }
    });

    if (response.status === 401) {
      throw authError("Session expired");
    }
    return response;
  }

  async function loadProfile() {
    try {
      const response = await api("");
      if (!response.ok) return;
      const profile = await response.json();
      navUserName.textContent = profile.fullName;
      userAvatar.textContent = profile.fullName.charAt(0).toUpperCase();
    } catch (error) {
      console.error("Could not load profile:", error);
    }
  }

  //Fills the form with the saved settings
  async function loadSettings() {
    try {
      const response = await api("/settings");
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
      showStatus("Could not load your settings. Check that the backend is running, then reload.", "error");
      console.error("Could not load settings:", error);
    }
  }

  // Saves the form
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    saveBtn.disabled = true;
    showStatus("Saving...", "");

    const body = {
      theme: themeSelect.value,
      timezone: timezoneSelect.value,
      notificationsEnabled: notificationsToggle.checked,
      emailRemindersEnabled: emailToggle.checked,
      defaultReminderMinutes: Number(reminderSelect.value)
    };

    //Remembers the theme
    CampusPathTheme.save(body.theme);

    try {
      const response = await api("/settings", {
        method: "PUT",
        body: JSON.stringify(body)
      });

      if (!response.ok) {
        const problem = await response.json().catch(() => ({}));
        showStatus(problem.detail || "Could not save your settings. Try again.", "error");
        return;
      }
      showStatus("Settings saved.", "success");
    } catch (error) {
      if (error.name === "AuthError") {
        showStatus("", "");
        return;
      }
      showStatus("Could not reach the server. Check your connection and try again.", "error");
      console.error("Could not save settings:", error);
    } finally {
      saveBtn.disabled = false;
    }
  });

  form.addEventListener("input", () => {
    if (formStatus.classList.contains("success")) showStatus("", "");
  });

  document.getElementById("logoutLink").addEventListener("click", (event) => {
    event.preventDefault();
    goToLogin();
  });

  // Start
  fillSelect(timezoneSelect, TIMEZONES);
  fillSelect(reminderSelect, REMINDER_OPTIONS);
  loadProfile();
  loadSettings();
}

document.addEventListener("DOMContentLoaded", initSettingsPage);