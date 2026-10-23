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

