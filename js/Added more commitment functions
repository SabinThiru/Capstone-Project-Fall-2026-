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

    throw new Error("Your login has expired. Please log in again.");
  }

  if (!response.ok) {
    let detail = "";

    try {
      const problem = await response.json();
      detail = problem.detail || problem.message || "";
    } catch (error) {

    }

    throw new Error(
      detail || `Request failed (${response.status}).`
    );
  }

  return response.status === 204
    ? null
    : response.json();
}

// Sync theme with student settings

async function syncScheduleTheme() {
  try {
    const settings = await apiRequest(
      `${API_BASE_URL}/api/users/me/settings`
    );

    if (
      settings &&
      (settings.theme === "light" || settings.theme === "dark")
    ) {
      applyScheduleTheme(settings.theme);
    }
  } catch (error) {
    console.warn("Could not sync schedule theme:", error);
  }
}

// Recover events

async function loadEvents() {
  const result = await apiRequest(EVENTS_URL);

  if (!Array.isArray(result)) {
    throw new Error("Could not load schedule data.");
  }

  events = result;

  events.sort((a, b) =>
    a.eventDate.localeCompare(b.eventDate) ||
    a.startTime.localeCompare(b.startTime)
  );

  render();
}

// Initialize schedule

async function initialize() {
  if (!requireToken()) return;

  try {
    const [
      profile,
      loadedEvents,
      loadedCommitments,
      loadedTerms
    ] = await Promise.all([
      apiRequest(`${API_BASE_URL}/api/users/me`),
      apiRequest(EVENTS_URL),
      apiRequest(COMMITMENTS_URL),
      apiRequest(TERMS_URL)
    ]);

    if (
      ![
        loadedEvents,
        loadedCommitments,
        loadedTerms
      ].every(Array.isArray)
    ) {
      throw new Error("Could not load schedule data.");
    }

// Apply student's theme independently
    void syncScheduleTheme();

    const name = profile.fullName || "Student";

    refs.navUserName.textContent = name;
    refs.userAvatar.textContent = name.charAt(0).toUpperCase();

    events = loadedEvents;
    commitments = loadedCommitments;
    academicTerms = loadedTerms;

    renderTermSelect();
    await loadOptions();

    render();
    renderCommitments();

    scheduleReady = true;
    showStatus();
  } catch (error) {
    scheduleReady = false;

    showStatus(
      `Unable to load your schedule. ${error.message}`,
      true
    );

    console.error("Schedule loading failed:", error);
  }
}

// Add event dialog

function openAddDialog() {
  if (!scheduleReady) {
    showStatus(
      "Your schedule is not available yet. Check the backend connection and refresh.",
      true
    );
    return;
  }

  editingId = null;
  refs.form.reset();

  refs.dialogTitle.textContent = "Add event";
  refs.save.textContent = "Save event";
  refs.remove.hidden = true;

  refs.date.value = dateKey(new Date());
  refs.start.value = "09:00";
  refs.end.value = "10:00";

  refs.formError.textContent = "";

  refs.dialog.showModal();
  refs.title.focus();
}

// Edit event dialog

function openEditDialog(id) {
  const event = events.find(item => item.id === id);

  if (!event) return;

  editingId = event.id;

  refs.dialogTitle.textContent = "Edit event";
  refs.save.textContent = "Save changes";

  refs.title.value = event.title;
  refs.type.value = event.eventType;
  refs.date.value = event.eventDate;
  refs.start.value = event.startTime.slice(0, 5);
  refs.end.value = event.endTime.slice(0, 5);
  refs.location.value = event.location || "";
  refs.notes.value = event.notes || "";

  refs.remove.hidden = false;
  refs.formError.textContent = "";

  refs.dialog.showModal();
  refs.title.focus();
}

function setFormBusy(busy) {
  refs.save.disabled = busy;
  refs.remove.disabled = busy;

  refs.save.textContent = busy
    ? "Saving…"
    : (editingId === null ? "Save event" : "Save changes");
}

// save events

refs.form.addEventListener("submit", async event => {
  event.preventDefault();

  if (!refs.form.reportValidity()) return;

  refs.formError.textContent = "";

  if (refs.end.value <= refs.start.value) {
    refs.formError.textContent =
      "End time must be after start time on the same day.";
    return;
  }

  const data = {
    title: refs.title.value.trim(),
    eventType: refs.type.value,
    eventDate: refs.date.value,
    startTime: refs.start.value,
    endTime: refs.end.value,
    location: refs.location.value.trim(),
    notes: refs.notes.value.trim()
  };

  if (!data.title) {
    refs.formError.textContent =
      "Please enter an event name.";
    return;
  }

  const isEditing = editingId !== null;
  const url = isEditing
    ? `${EVENTS_URL}/${editingId}`
    : EVENTS_URL;

  setFormBusy(true);

  try {
    await apiRequest(url, {
      method: isEditing ? "PUT" : "POST",
      body: JSON.stringify(data)
    });

    shownWeek = startOfWeek(parseDate(data.eventDate));

    await loadEvents();
    refs.dialog.close();

    showStatus(
      isEditing
        ? "Event updated successfully."
        : "Event added successfully."
    );
  } catch (error) {
    refs.formError.textContent = error.message;
  } finally {
    setFormBusy(false);
  }
});

// Delete events

refs.remove.addEventListener("click", async () => {
  if (editingId === null) return;

  const event = events.find(item => item.id === editingId);

  if (
    !event ||
    !window.confirm(
      `Delete “${event.title}”? This cannot be undone.`
    )
  ) {
    return;
  }

  refs.formError.textContent = "";
  setFormBusy(true);

  try {
    await apiRequest(
      `${EVENTS_URL}/${editingId}`,
      { method: "DELETE" }
    );

    await loadEvents();
    refs.dialog.close();

    showStatus("Event deleted successfully.");
  } catch (error) {
    refs.formError.textContent = error.message;
  } finally {
    setFormBusy(false);
  }
});

// Event dialog buttons

refs.dialog.addEventListener("close", () => {
  refs.formError.textContent = "";
});

document
  .getElementById("closeDialogButton")
  .addEventListener("click", () => refs.dialog.close());

document
  .getElementById("cancelEventButton")
  .addEventListener("click", () => refs.dialog.close());

document
  .getElementById("addEventButton")
  .addEventListener("click", openAddDialog);

document
  .getElementById("quickAddButton")
  .addEventListener("click", openAddDialog);

// Weekly navigation

document
  .getElementById("previousWeek")
  .addEventListener("click", () => {
    shownWeek = addDays(shownWeek, -7);
    renderWeek();
  });

document
  .getElementById("nextWeek")
  .addEventListener("click", () => {
    shownWeek = addDays(shownWeek, 7);
    renderWeek();
  });

document
  .getElementById("todayButton")
  .addEventListener("click", () => {
    shownWeek = startOfWeek(new Date());
    renderWeek();
  });

// Event filters

document.querySelectorAll(".filter-button").forEach(button => {
  button.addEventListener("click", () => {
    activeFilter = button.dataset.filter;

    document.querySelectorAll(".filter-button").forEach(item => {
      const selected = item === button;

      item.classList.toggle("selected", selected);
      item.setAttribute("aria-pressed", String(selected));
    });

    render();
  });
});

// Logout

document
  .getElementById("logoutLink")
  .addEventListener("click", event => {
    event.preventDefault();
    sessionStorage.removeItem("idToken");
    window.location.replace("login.html");
  });

// Start schedule page

render();
initialize();

// Weekly commitments

const byId = id => document.getElementById(id);

const commitmentDialog = byId("commitmentDialog");
const commitmentForm = byId("commitmentForm");

const days = [
  "Monday",
  "Tuesday",
  "Wednesday",
  "Thursday",
  "Friday",
  "Saturday",
  "Sunday"
];

const typeColors = {
  class: "#66b366",
  work: "#88a9c2",
  personal: "#e8b66e"
};

// Display commitments

function renderCommitments() {
  const list = byId("commitmentList");

  list.replaceChildren();

  if (!commitments.length) {
    list.appendChild(
      makeElement(
        "p",
        "empty-records",
        "No repeating commitments added yet."
      )
    );
    return;
  }

  for (const c of commitments) {
    const row = makeElement("div", "record-row");

    const stripe = makeElement("span", "commitment-swatch");

    stripe.style.backgroundColor = c.color;
    stripe.setAttribute("aria-hidden", "true");

    const content = makeElement("div", "record-content");

    content.append(
      makeElement("p", "record-title", c.title),
      makeElement(
        "p",
        "record-meta",
        `${days[c.dayOfWeek - 1]} · ${formatTime(c.startTime)} – ${formatTime(c.endTime)}`
      ),
      makeElement(
        "p",
        "record-meta",
        `${c.startDate} to ${c.endDate} · ${c.commitmentType}`
      )
    );

    const actions = makeElement("div", "record-actions");

    const edit = makeElement("button", "", "Edit");
    edit.type = "button";

    edit.addEventListener("click", () => {
      openCommitmentDialog(c.id);
    });

    actions.append(edit);
    row.append(stripe, content, actions);
    list.append(row);
  }
}

// Add or edit commitment

function openCommitmentDialog(id = null) {
  if (!scheduleReady) {
    return showStatus(
      "Schedule is unavailable; try refreshing.",
      true
    );
  }

  editingCommitmentId = id;
  commitmentForm.reset();

  byId("commitmentError").textContent = "";

  byId("commitmentDialogTitle").textContent =
    id === null
      ? "Add weekly commitment"
      : "Edit weekly commitment";

  byId("deleteCommitmentButton").hidden = id === null;

  if (id !== null) {
    const c = commitments.find(item => item.id === id);

    if (!c) return;

    byId("commitmentTitle").value = c.title;
    byId("commitmentType").value = c.commitmentType;
    byId("commitmentDay").value = c.dayOfWeek;

    byId("commitmentStartTime").value =
      c.startTime.slice(0, 5);

    byId("commitmentEndTime").value =
      c.endTime.slice(0, 5);

    byId("commitmentStartDate").value = c.startDate;
    byId("commitmentEndDate").value = c.endDate;
    byId("commitmentColor").value = c.color;
  } else {
    byId("commitmentDay").value =
      ((new Date().getDay() + 6) % 7) + 1;

    byId("commitmentStartTime").value = "09:00";
    byId("commitmentEndTime").value = "10:00";

    byId("commitmentStartDate").value = dateKey(new Date());

    byId("commitmentEndDate").value =
      dateKey(addDays(new Date(), 90));

    byId("commitmentColor").value = typeColors.class;
  }

  commitmentDialog.showModal();
  byId("commitmentTitle").focus();
}

// Update calendar color when category changes

byId("commitmentType").addEventListener("change", () => {
  if (editingCommitmentId === null) {
    byId("commitmentColor").value =
      typeColors[byId("commitmentType").value];
  }
});

// Save commitments

commitmentForm.addEventListener("submit", async e => {
  e.preventDefault();

  const data = {
    title: byId("commitmentTitle").value.trim(),
    commitmentType: byId("commitmentType").value,
    dayOfWeek: Number(byId("commitmentDay").value),
    startTime: byId("commitmentStartTime").value,
    endTime: byId("commitmentEndTime").value,
    startDate: byId("commitmentStartDate").value,
    endDate: byId("commitmentEndDate").value,
    color: byId("commitmentColor").value
  };

  const error = byId("commitmentError");
  error.textContent = "";

  if (data.endTime <= data.startTime) {
    error.textContent =
      "End time must be later than start time.";
    return;
  }

  if (data.endDate < data.startDate) {
    error.textContent =
      "End date must be on or after start date.";
    return;
  }

  const id = editingCommitmentId;
  const save = byId("saveCommitmentButton");

  save.disabled = true;

  try {
    await apiRequest(
      id === null
        ? COMMITMENTS_URL
        : `${COMMITMENTS_URL}/${id}`,
      {
        method: id === null ? "POST" : "PUT",
        body: JSON.stringify(data)
      }
    );

    commitments = await apiRequest(COMMITMENTS_URL);

    commitmentDialog.close();

    render();
    renderCommitments();

    showStatus(
      id === null
        ? "Commitment added."
        : "Commitment updated."
    );
  } catch (ex) {
    error.textContent = ex.message;
  } finally {
    save.disabled = false;
  }
});

// Delete commitments

byId("deleteCommitmentButton").addEventListener(
  "click",
  async () => {
    const id = editingCommitmentId;

    if (
      id === null ||
      !window.confirm("Delete this recurring commitment?")
    ) {
      return;
    }

    const button = byId("deleteCommitmentButton");
    button.disabled = true;

    try {
      await apiRequest(
        `${COMMITMENTS_URL}/${id}`,
        { method: "DELETE" }
      );

      commitments = await apiRequest(COMMITMENTS_URL);

      commitmentDialog.close();

      render();
      renderCommitments();

      showStatus("Commitment deleted.");
    } catch (ex) {
      byId("commitmentError").textContent = ex.message;
    } finally {
      button.disabled = false;
    }
  }
);

// Commitment buttons

byId("addCommitmentButton").addEventListener(
  "click",
  () => openCommitmentDialog()
);

byId("quickCommitmentButton").addEventListener(
  "click",
  () => openCommitmentDialog()
);
  
