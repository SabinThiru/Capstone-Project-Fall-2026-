//CampusPath student dashboard UI
//Temp data replace later with backend data
const student = {
  name: Student",
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
const userAvatar = document.getElementId("userAvatar");
const studentInitials = document.getElementById("studentInitials");

const degreePercentage = document.getElementById("degreePercentage");
const creditProgress = document.getElementById("creditProgress");
const progressFill = document.getElementById("progressFill");
const studentMajor = document.getElementById("studentMajor");
const graduationDate = document.getElementById("graduationDate");

const studentGpa = document.getElementByID("studentGpa");
const academicStanding = document.getElementByID("academicStanding");
const scheduleList = document.getElementById("scheduleList");
const alertsList = document.getElementById("alertsList");
const assignmentsList = document.getElementById("assignmentList");
const requirementsBtn = document.getElementById("requirementsBtn");

//Display user information

function displayStudentInfo() {
  welcomeName.textContent = student.name;
  navUserName.textContent = student.name;
  const initial = student.name.charAt(0).toUpperCase();
  userAvatar.textContent = inital;
  studentInitials.textContent = initial;

  studentMajor.textContent = student.major || "Not applicable";
  graduationDate.textContent = student.graduationDate || "Not applicable";
  studentGpa.textContent = student.gpa || "--";
  academicStanding || "Academic standing is unavailable";
}

//Displays degree progression

function displayDegreeProgress() {
  let percentage = 0;

  if (student.requiredCredits > 0) {
    percentage = Math.round(
      (student.completedCredits / student.requiredCredits) * 100);
  }
  degreePercentage.textContent = percentage + "%";
  creditProgress.textContent = student.completedCredits + " of " + student.requredCredits + " credits";
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
function displaysAlerts() {
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

function displaysAssignments() {
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

requirementsBtn.addEventListner("click", () => {
  window.location.href = "degree-progress.html";
});

// Dashboard information

displayStudentInfo();
displayDegreeProgress();
displaySchedule();
displayAlerts();
displayAssignments();

    
    
    
                           
                           
    
                                              
  
