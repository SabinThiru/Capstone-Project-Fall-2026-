//CampusPath student dashboard UI
//Temp data replace later with backend data
const student = {
  name: "Student",
  major: "",
  completedCredits: 0,
  requiredCredits: 0,
  graduationDate: "",
  gpa: "",
  academicStanding: "",
  schedule: [],
  alerts: [],
  assignments: []
};

const welcomeName = document.getElementById("welcomeName");
const navUserName = document.getElementById("navUserName");
const userAvatar = document.getElementById("userAvatar");
const studentInitials = document.getElementById("studentInitials");

const degreePercentage = document.getElementById("degreePercentage");
const creditProgress = document.getElementById("creditProgress");
const progressFill = document.getElementById("progressFill");
const studentMajor = document.getElementById("studentMajor");
const graduationDate = document.getElementById("graduationDate");

const studentGpa = document.getElementById("studentGpa");
const academicStanding = document.getElementById("academicStanding");
const scheduleList = document.getElementById("scheduleList");
const alertsList = document.getElementById("alertsList");
const assignmentsList = document.getElementById("assignmentsList");
const requirementsBtn = document.getElementById("requirementsBtn");

//Display user information

function displayStudentInfo() {
  welcomeName.textContent = student.name;
  navUserName.textContent = student.name;
  const initial = student.name.charAt(0).toUpperCase();
  userAvatar.textContent = initial;
  studentInitials.textContent = initial;

  studentMajor.textContent = student.major || "Not applicable";
  graduationDate.textContent = student.graduationDate || "Not applicable";
  studentGpa.textContent = student.gpa || "--";
  academicStanding.textContent = student.academicStanding || "Academic standing is unavailable";
}

//Displays degree progression

function displayDegreeProgress() {
  let percentage = 0;

  if (student.requiredCredits > 0) {
    percentage = Math.round(
      (student.completedCredits / student.requiredCredits) * 100);
  }
  degreePercentage.textContent = percentage + "%";
  creditProgress.textContent = student.completedCredits + " of " + student.requiredCredits + " credits";
  progressFill.style.width = percentage + "%";
}

//Displays today's schedule

function displaySchedule() {
  if (student.schedule.length == 0) {
    scheduleList.innerHTML = '<p class="empty-message">No events are scheduled for today.</p>';
    return;
  }
  
  scheduleList.innerHTML = "";
  student.schedule.forEach((event) => {
    const scheduleItem = document.createElement("div");
    scheduleItem.className = "schedule-item " + event.type + "-event";

    scheduleItem.innerHTML = `
      <h3>${event.title}</h3>
      <p>${event.time}</p>
      <p>${event.location}</p>
  `;
    scheduleList.appendChild(scheduleItem);
});
  
}

//Displays alerts
function displayAlerts() {
  if (student.alerts.length == 0) {
    alertsList.innerHTML = '<p class="empty-message">No new alerts.</p>';
    return;
  }
  alertsList.innerHTML = "";
  student.alerts.forEach((alert) => {
    const alertItem = document.createElement("div");
    alertItem.className = "alert-item";
    alertItem.innerHTML =`
    <h3>${alert.title}</h3>
    <p>${alert.date}</p>
    `;
    alertsList.appendChild(alertItem);
  });
}

//Displays assignments

function displayAssignments() {
  if (student.assignments.length == 0) {
    assignmentsList.innerHTML = '<p class="empty-message">No upcoming assignments.</p>';
    return;
  }
  assignmentsList.innerHTML = "";
  student.assignments.forEach((assignment) => {
    const assignmentItem = document.createElement("article");
    assignmentItem.className = "assignment";

    assignmentItem.innerHTML = `
    <div>
    <h3>${assignment.title}</h3>
    <p>${assignment.course}</p>
    </div>
    <span>${assignment.dueDate}</span>
    `;
    assignmentsList.appendChild(assignmentItem);
  });
}

//Function to open degree requirements

requirementsBtn.addEventListener("click", () => {
  window.location.href = "degree-progress.html";
});

function displayGreeting() {
  const hour = new Date().getHours();
  const greeting = document.getElementById("greeting");

  greeting.textContent =
    hour < 12 ? "Good morning" :
    hour < 18 ? "Good afternoon" :
    "Good evening";
}
// Dashboard information

async function loadDashboard() {
  const idToken = sessionStorage.getItem("idToken");

  if (!idToken) {
    window.location.replace("login.html");
    return;
  }

  try {
    const response = await fetch(
      "http://localhost:8080/api/users/me",
      {
        headers: {
          Authorization: `Bearer ${idToken}`
        }
      }
    );

    if (response.status === 401) {
      sessionStorage.removeItem("idToken");
      window.location.replace("login.html");
      return;
    }

    if (!response.ok) {
      throw new Error(
        `Could not load your profile (${response.status}).`
      );
    }

    const profile = await response.json();
    student.name = profile.fullName;

    displayGreeting();
    displayStudentInfo();
    displayDegreeProgress();
    displaySchedule();
    displayAlerts();
    displayAssignments();
  } catch (error) {
    const message = document.createElement("p");
    message.setAttribute("role", "alert");
    message.textContent =
      "Unable to load your dashboard. Check that the backend is running, then reload.";

    document.body.prepend(message);
    console.error("Dashboard loading failed:", error);
  }
}

loadDashboard();

document.getElementById("logoutLink").addEventListener("click", (event) => {
  event.preventDefault();
  sessionStorage.removeItem("idToken");
  window.location.replace("login.html");
});
                           
    
                                      
