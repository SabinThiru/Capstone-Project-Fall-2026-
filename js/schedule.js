//Theme supports settings page

const THEME_KEY="campuspath-theme";
function applyScheduleTheme
  const chosen = theme == "light" ? "light: : "dark";
  document.documentElement.setAttribute("data-theme", chosen);

try {
  localStorage.setItem(THEME_KEY, chosen);
}
catch (error) {
}
}

function applySavedScheduleTheme() {
  try {
    applyScheduleTheme(localStorage.getItem(THEME_KEY));
  }
  catch (error) {
    document.documentElement.setAttribute("data-theme", "dark");
  }
}

applySavedScheduleTheme();
