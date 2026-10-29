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

//Backend API integration

const API_BASE_URL = "http://localhost:8080";
const EVENTS_URL = `${API_BASE_URL}/api/schedule/events`;
const COMMITMENTS_URL = `${API_BASE_URL}/api/schedule/commitments`;
const TERMS_URL = `${API_BASE_URL}/api/schedule/terms`;
const GENERATED_URL = `${API_BASE_URL}/api/schedule/generated`;

// HTML Elements

const refs = {
  userAvatar: document.getElementById("userAvatar"),
  navUserName: document.getElementById("navUserName"),
  pageStatus: document.getElementById("pageStatus"),
  weekGrid: document.getElementById("weekGrid"),
  weekRange: document.getElementById("weekRange"),
  upcomingList: document.getElementById("upcomingList"),
  upcomingCount: document.getElementById("upcomingCount"),
  dialog: document.getElementById("eventDialog"),
  form: document.getElementById("eventForm"),
  dialogTitle: document.getElementById("dialogTitle"),
  title: document.getElementById("eventTitle"),
  type: document.getElementById("eventType"),
  date: document.getElementById("eventDate"),
  start: document.getElementById("startTime"),
  end: document.getElementById("endTime"),
  location: document.getElementById("eventLocation"),
  notes: document.getElementById("eventNotes"),
  formError: document.getElementById("formError"),
  save: document.getElementById("saveEventButton"),
  remove: document.getElementById("deleteEventButton")
};
