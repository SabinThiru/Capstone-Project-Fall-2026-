let currentUser = { id: "", name: "Student" };
let myCourses = [];
let students = [];
let conversations = [];

const layout = document.getElementById("chatLayout");
const listEl = document.getElementById("conversationList");
const listEmpty = document.getElementById("listEmpty");
const searchInput = document.getElementById("chatSearch");
const courseFilter = document.getElementById("courseFilter");
const tabs = document.querySelectorAll(".type-tabs .tab");
const chatEmpty = document.getElementById("chatEmpty");
const chatPanel = document.getElementById("chatPanel");
const chatAvatar = document.getElementById("chatAvatar");
const chatName = document.getElementById("chatName");
const chatMeta = document.getElementById("chatMeta");
const chatCourse = document.getElementById("chatCourse");
const messageList = document.getElementById("messageList");
const messageForm = document.getElementById("messageForm");
const messageInput = document.getElementById("messageInput");
const sendBtn = document.getElementById("sendBtn");
const backBtn = document.getElementById("backBtn");
const groupModal = document.getElementById("groupModal");
const groupForm = document.getElementById("groupForm");
const groupModalTitle = document.getElementById("groupModalTitle");
const groupNameField = document.getElementById("groupNameField");
const groupName = document.getElementById("groupName");
const groupCourseLabel = document.getElementById("groupCourseLabel");
const groupCourse = document.getElementById("groupCourse");
const membersLabel = document.getElementById("membersLabel");
const memberPicker = document.getElementById("memberPicker");
const groupError = document.getElementById("groupError");
const groupSubmitBtn = document.getElementById("groupSubmitBtn");

let activeId = null;
let activeType = "all";
let modalMode = "group";


/* Helpers */
function el(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

