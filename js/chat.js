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

function initials(name) {
  const parts = (name || "").trim().split(/\s+/);
  return (((parts[0] || "")[0] || "") + ((parts[1] || "")[0] || "")).toUpperCase();
}

function nameOf(id) {
  if (id === currentUser.id) return currentUser.name;
  const s = students.find((x) => x.id === id);
  return s ? s.name : "Unknown";
}

function formatTime(iso) {
  return new Date(iso).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
}

function sameDay(a, b) {
  return new Date(a).toDateString() === new Date(b).toDateString();
}

function dayLabel(iso) {
  const d = new Date(iso);
  const today = new Date();
  const yesterday = new Date(Date.now() - 86400000);
  if (d.toDateString() === today.toDateString()) return "Today";
  if (d.toDateString() === yesterday.toDateString()) return "Yesterday";
  return d.toLocaleDateString([], { weekday: "short", month: "short", day: "numeric" });
}

function lastMessage(convo) {
  return convo.messages[convo.messages.length - 1];
}

function getConvo(id) {
  return conversations.find((c) => c.id === id);
}

/*  Conversation list */
function visibleConversations() {
  const query = searchInput.value.trim().toLowerCase();
  const course = courseFilter.value;
  return conversations
    .filter((c) => activeType === "all" || c.type === activeType)
    .filter((c) => course === "all" || c.courseCode === course)
    .filter((c) => !query || c.name.toLowerCase().includes(query))
    .sort((a, b) => {
      const ta = lastMessage(a) ? new Date(lastMessage(a).sentAt) : 0;
      const tb = lastMessage(b) ? new Date(lastMessage(b).sentAt) : 0;
      return tb - ta;
    });
}

function renderList() {
  listEl.replaceChildren();
  const items = visibleConversations();
  listEmpty.hidden = items.length > 0;
  items.forEach((convo) => {
    const li = el("li");
    const btn = el("button", "convo-item" + (convo.id === activeId ? " active" : ""));
    btn.type = "button";
    btn.addEventListener("click", () => openConversation(convo.id));
    btn.appendChild(el("span", "convo-avatar" + (convo.type === "group" ? " group" : ""), initials(convo.name)));
    const body = el("div", "convo-body");
    const top = el("div", "convo-top");
    top.appendChild(el("span", "convo-name", convo.name));
    const last = lastMessage(convo);
    if (last) top.appendChild(el("span", "convo-time", formatTime(last.sentAt)));
    body.appendChild(top);
    const bottom = el("div", "convo-bottom");
    let preview = "No messages yet";
    if (last) {
      const who = last.senderId === currentUser.id ? "You: " : (convo.type === "group" ? nameOf(last.senderId).split(" ")[0] + ": " : "");
      preview = who + last.text;
    }
    bottom.appendChild(el("span", "convo-preview", preview));
    if (convo.unread > 0) bottom.appendChild(el("span", "unread-badge", String(convo.unread)));
    body.appendChild(bottom);
    if (convo.courseCode) body.appendChild(el("span", "convo-course", convo.courseCode));

    btn.appendChild(body);
    li.appendChild(btn);
    listEl.appendChild(li);
  });
}

function openConversation(id) {
  const convo = getConvo(id);
  if (!convo) return;
  activeId = id;
  convo.unread = 0;