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

