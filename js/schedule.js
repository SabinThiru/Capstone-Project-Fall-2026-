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

// Schedule data

let events = [];
let commitments = [];
let academicTerms = [];
let scheduleOptions = [];
let editingCommitmentId = null;
let shownWeek =startOfWeek(new Date());
let activeFilter = "all";
let editingId = null;
let scheduleReady = false;

// Date helpers

function startOfWeek(date) {
  const monday = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  );

  monday.setDate(
    monday.getDate() - ((monday.getDay() + 6) % 7)
  );

  return monday;
}

function addDays(date, days) {
  const next = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  );

  next.setDate(next.getDate() + days);
  return next;
}

function dateKey(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function parseDate(key) {
  const [year, month, day] = key.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function formatDate(date, options) {
  return new Intl.DateTimeFormat(undefined, options).format(date);
}

function formatTime(time) {
  const [hours, minutes] = time.slice(0, 5).split(":").map(Number);
  const sample = new Date(2000, 0, 1, hours, minutes);

  return formatDate(sample, {
    hour: "numeric",
    minute: "2-digit"
  });
}

// Display status messages

function showStatus(message = "", isError = false) {
  refs.pageStatus.textContent = message;
  refs.pageStatus.classList.toggle("error", isError);
}
