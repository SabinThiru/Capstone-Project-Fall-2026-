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

// Combine one time events and weekly commitments

function occurrences(start, end) {
  const first = dateKey(start);
  const last = dateKey(end);

  const result = events
    .filter(item =>
      item.eventDate >= first && item.eventDate <= last
    )
    .map(item => ({ ...item, source: "event" }));

  for (
    let date = new Date(start);
    date <= end;
    date = addDays(date, 1)
  ) {
    const key = dateKey(date);
    const dayOfWeek = ((date.getDay() + 6) % 7) + 1;

    for (const commitment of commitments) {
      if (
        commitment.dayOfWeek === dayOfWeek &&
        commitment.startDate <= key &&
        commitment.endDate >= key
      ) {
        result.push({
          id: commitment.id,
          source: "commitment",
          title: commitment.title,
          eventType: commitment.commitmentType,
          eventDate: key,
          startTime: commitment.startTime,
          endTime: commitment.endTime,
          color: commitment.color,
          location: ""
        });
      }
    }
  }

  return result
    .filter(item =>
      activeFilter === "all" ||
      item.eventType === activeFilter
    )
    .sort((a, b) =>
      a.eventDate.localeCompare(b.eventDate) ||
      a.startTime.localeCompare(b.startTime)
    );
}

function openScheduleItem(item) {
  if (item.source === "commitment") {
    openCommitmentDialog(item.id);
  } else {
    openEditDialog(item.id);
  }
}

// Create HTML elements

function makeElement(tag, className, text) {
  const el = document.createElement(tag);

  if (className) {
    el.className = className;
  }

  if (text !== undefined) {
    el.textContent = String(text);
  }

  return el;
}


// Weekly calendar

function renderWeek() {
  const weekEnd = addDays(shownWeek, 6);

  const first = formatDate(shownWeek, {
    month: "short",
    day: "numeric"
  });

  const last = formatDate(weekEnd, {
    month: "short",
    day: "numeric",
    year: "numeric"
  });

  refs.weekRange.textContent = `${first} – ${last}`;
  refs.weekGrid.replaceChildren();

  const today = dateKey(new Date());
  const visible = occurrences(shownWeek, weekEnd);

  for (let i = 0; i < 7; i += 1) {
    const date = addDays(shownWeek, i);
    const key = dateKey(date);

    const column = makeElement(
      "div",
      "day-column" + (key === today ? " is-today" : "")
    );

    const heading = makeElement("div", "day-header");

    heading.append(
      makeElement(
        "span",
        "day-name",
        formatDate(date, { weekday: "short" })
      ),
      makeElement(
        "span",
        "day-number",
        date.getDate()
      )
    );

    const list = makeElement("div", "day-events");

    const dayEvents = visible.filter(
      event => event.eventDate === key
    );

    if (dayEvents.length === 0) {
      list.appendChild(
        makeElement("p", "day-empty", "No events")
      );
    } else {
      dayEvents.forEach(event => {
        const button = makeElement(
          "button",
          `calendar-event ${event.eventType}${
            event.source === "commitment" ? " recurring" : ""
          }`
        );

        button.type = "button";

        if (event.color) {
          button.style.borderLeftColor = event.color;
        }

        button.setAttribute(
          "aria-label",
          `Edit ${event.title} on ${key}`
        );

        button.appendChild(
          makeElement(
            "span",
            "event-time",
            formatTime(event.startTime)
          )
        );

        button.appendChild(
          makeElement(
            "span",
            "event-title",
            event.title
          )
        );

        if (event.location) {
          button.appendChild(
            makeElement(
              "span",
              "event-location",
              event.location
            )
          );
        }

        button.addEventListener("click", () => {
          openScheduleItem(event);
        });

        list.appendChild(button);
      });
    }

    column.append(heading, list);
    refs.weekGrid.appendChild(column);
  }
}

// Upcoming events

function renderUpcoming() {
  refs.upcomingList.replaceChildren();

  const now = new Date();
  const today = dateKey(now);

  const timeNow =
    String(now.getHours()).padStart(2, "0") +
    ":" +
    String(now.getMinutes()).padStart(2, "0");

  const next60 = addDays(now, 60);

  const repeated = occurrences(startOfWeek(now), next60)
    .filter(item => item.source === "commitment");

  const future = [
    ...events.map(item => ({ ...item, source: "event" })),
    ...repeated
  ]
    .filter(item =>
      activeFilter === "all" ||
      item.eventType === activeFilter
    )
    .filter(item =>
      item.eventDate > today ||
      (
        item.eventDate === today &&
        item.endTime.slice(0, 5) > timeNow
      )
    )
    .sort((a, b) =>
      a.eventDate.localeCompare(b.eventDate) ||
      a.startTime.localeCompare(b.startTime)
    );

  refs.upcomingCount.textContent = String(future.length);

  if (!future.length) {
    refs.upcomingList.appendChild(
      makeElement(
        "p",
        "empty-upcoming",
        "Nothing on the calendar yet. Add an event to get started."
      )
    );
    return;
  }

  future.slice(0, 6).forEach(event => {
    const row = makeElement("div", "upcoming-entry");

    const stripe = makeElement(
      "span",
      `upcoming-icon ${event.eventType}`
    );

    stripe.setAttribute("aria-hidden", "true");

    if (event.color) {
      stripe.style.backgroundColor = event.color;
    }

    const body = makeElement("div", "upcoming-body");

    body.appendChild(
      makeElement(
        "span",
        "upcoming-date",
        formatDate(parseDate(event.eventDate), {
          weekday: "short",
          month: "short",
          day: "numeric"
        })
      )
    );

    const title = makeElement(
      "button",
      "upcoming-title",
      event.title
    );

    title.type = "button";

    title.addEventListener("click", () => {
      openScheduleItem(event);
    });

    body.appendChild(title);

    const timeText =
      `${formatTime(event.startTime)} – ${formatTime(event.endTime)}`;

    const locationText = event.location
      ? ` · ${event.location}`
      : "";

    body.appendChild(
      makeElement(
        "p",
        "upcoming-meta",
        `${timeText}${locationText}`
      )
    );

    row.append(stripe, body);
    refs.upcomingList.appendChild(row);
  });
}

function render() {
  renderWeek();
  renderUpcoming();
}

// Authentication

function requireToken() {
  const token = sessionStorage.getItem("idToken");

  if (!token) {
    window.location.replace("login.html");
    return null;
  }

  return token;
}

// API Requests

async function apiRequest(url, options = {}) {
  const token = requireToken();

  if (!token) {
    throw new Error("Please log in again.");
  }

  const response = await fetch(url, {
    ...options,
    headers: {
      Authorization: `Bearer ${token}`,
      ...(options.body
        ? { "Content-Type": "application/json" }
        : {})
    }
  });

  if (response.status === 401) {
    sessionStorage.removeItem("idToken");
    window.location.replace("login.html");

    throw new Error(
      "Your login has expired. Please log in again."
    );
  }

  if (!response.ok) {
    let detail = "";

    try {
      const problem = await response.json();
      detail = problem.detail || problem.message || "";
    } catch (error) {
      // Some errors may not return JSON.
    }

    throw new Error(
      detail || `Request failed (${response.status}).`
    );
  }

  return response.status === 204
    ? null
    : response.json();
}

