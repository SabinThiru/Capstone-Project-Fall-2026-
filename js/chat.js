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
/* Chat Settings */
const SETTINGS_KEY = "campuspath-chat-settings";
const DEFAULT_CHAT_SETTINGS = {
  notifications: true,
  sound: true,
  showPreviews: true,
  enterToSend: true,
  groupInvites: "classmates" 
};
function loadChatSettings() {
  try {
    const saved = JSON.parse(localStorage.getItem(SETTINGS_KEY) || "{}");
    return Object.assign({}, DEFAULT_CHAT_SETTINGS, saved);
  } catch (e) {
    return Object.assign({}, DEFAULT_CHAT_SETTINGS);
  }
}
function saveChatSettings() {
  try {
    localStorage.setItem(SETTINGS_KEY, JSON.stringify(chatSettings));
  } catch (e) {}
}
let chatSettings = loadChatSettings();
(function initChatSettings() {
  const toggle = document.getElementById("chatSettingsToggle");
  const panel = document.getElementById("chatSettingsPanel");

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
  chatEmpty.hidden = true;
  chatPanel.hidden = false;
  layout.classList.add("chat-open");

  chatAvatar.textContent = initials(convo.name);
  chatAvatar.className = "convo-avatar" + (convo.type === "group" ? " group" : "");
  chatName.textContent = convo.name;
  chatMeta.textContent =
    convo.type === "group"
      ? convo.memberIds.length + " members · " + convo.memberIds.map(nameOf).join(", ")
      : "Direct message";

  chatCourse.hidden = !convo.courseCode;
  chatCourse.textContent = convo.courseCode;
  renderMessages(convo);
  renderList();
  messageInput.focus();
}

function renderMessages(convo) {
  messageList.replaceChildren();
  if (convo.messages.length === 0) {
    const empty = el("p", "empty-message", "No messages yet. Say hi 👋");
    empty.style.alignSelf = "center";
    empty.style.marginTop = "24px";
    messageList.appendChild(empty);
    return;
  }

  let prev = null;
  convo.messages.forEach((msg) => {
    if (!prev || !sameDay(prev.sentAt, msg.sentAt)) {
      messageList.appendChild(el("div", "day-divider", dayLabel(msg.sentAt)));
    }
    const mine = msg.senderId === currentUser.id;
    const newSender = !prev || prev.senderId !== msg.senderId || !sameDay(prev.sentAt, msg.sentAt);

    const wrap = el("div", "message" + (mine ? " mine" : "") + (newSender ? " new-sender" : ""));
    if (!mine && convo.type === "group" && newSender) {
      wrap.appendChild(el("span", "message-sender", nameOf(msg.senderId)));
    }
    wrap.appendChild(el("div", "bubble", msg.text));
    wrap.appendChild(el("span", "message-time", formatTime(msg.sentAt)));
    messageList.appendChild(wrap);
    prev = msg;
  });

  messageList.scrollTop = messageList.scrollHeight;
}

function updateSendState() {
  sendBtn.disabled = messageInput.value.trim() === "";
}

function autoGrow() {
  messageInput.style.height = "auto";
  messageInput.style.height = Math.min(messageInput.scrollHeight, 120) + "px";
}

function sendMessage() {
  const text = messageInput.value.trim();
  const convo = getConvo(activeId);
  if (!text || !convo) return;
  convo.messages.push({
    id: "m" + Date.now(),
    senderId: currentUser.id,
    text: text,
    sentAt: new Date().toISOString()
  });

  messageInput.value = "";
  autoGrow();
  updateSendState();
  renderMessages(convo);
  renderList();
}

messageInput.addEventListener("input", () => { updateSendState(); autoGrow(); });
messageInput.addEventListener("keydown", (event) => {
  //Enter sends message, Shift+Enter makes a new line
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
});
messageForm.addEventListener("submit", (event) => {
  event.preventDefault();
  sendMessage();
});

function fillCourseSelect(select, firstLabel, firstValue) {
  select.replaceChildren();
  const first = el("option", "", firstLabel);
  first.value = firstValue;
  select.appendChild(first);
  myCourses.forEach((course) => {
    const opt = el("option", "", course.code + " – " + course.title);
    opt.value = course.code;
    select.appendChild(opt);
  });
}
searchInput.addEventListener("input", renderList);
courseFilter.addEventListener("change", renderList);

tabs.forEach((tab) => {
  tab.addEventListener("click", () => {
    activeType = tab.dataset.type;
    tabs.forEach((t) => {
      const on = t === tab;
      t.classList.toggle("active", on);
      t.setAttribute("aria-selected", on ? "true" : "false");
    });
    renderList();
  });
});

backBtn.addEventListener("click", () => {
  layout.classList.remove("chat-open");
});

function renderMemberPicker() {
  const course = groupCourse.value;
  memberPicker.replaceChildren();
  const checkedBefore = new Set(selectedMembers());
  const people = students.filter((s) => !course || s.courses.includes(course));
  if (people.length === 0) {
    memberPicker.appendChild(el("p", "member-empty", "No students found for this course."));
    return;
  }
  people.forEach((s) => {
    const row = el("label", "member-option");
    const box = el("input");
    box.type = "checkbox";
    box.value = s.id;
    box.checked = checkedBefore.has(s.id);
    row.appendChild(box);
    row.appendChild(el("span", "", s.name));
    row.appendChild(el("small", "", s.courses.join(", ")));
    memberPicker.appendChild(row);
  });
}
function selectedMembers() {
  return Array.from(memberPicker.querySelectorAll("input:checked")).map((i) => i.value);
}

function openGroupModal() {
  groupForm.reset();
  groupError.textContent = "";
  renderMemberPicker();
  groupModal.showModal();
  groupName.focus();
}
document.getElementById("newGroupBtn").addEventListener("click", openGroupModal);
document.getElementById("cancelGroupBtn").addEventListener("click", () => groupModal.close());
groupCourse.addEventListener("change", renderMemberPicker);
groupModal.addEventListener("click", (event) => {
  if (event.target === groupModal) groupModal.close();
});

groupForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const name = groupName.value.trim();
  const members = selectedMembers();

  if (!name) {
    groupError.textContent = "Please give your group a name.";
    groupName.focus();
    return;
  }
  if (members.length === 0) {
    groupError.textContent = "Add at least one student to the group.";
    return;
  }

  const convo = {
    id: "c" + Date.now(),
    type: "group",
    name: name,
    courseCode: groupCourse.value,
    memberIds: [currentUser.id].concat(members),
    unread: 0,
    messages: [
      { id: "m" + Date.now(), senderId: currentUser.id, text: "Created the group “" + name + "”", sentAt: new Date().toISOString() }
    ]
  };
  conversations.push(convo);
  groupModal.close();
  searchInput.value = "";
  courseFilter.value = "all";
  tabs[0].click();
  openConversation(convo.id);
});
document.getElementById("navUserName").textContent = currentUser.name;
document.getElementById("userAvatar").textContent = currentUser.name.charAt(0).toUpperCase();

fillCourseSelect(courseFilter, "All my courses", "all");
fillCourseSelect(groupCourse, "No specific course", "");
renderList();