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
  const actions = document.getElementById("csActions");
  const saveBtn = document.getElementById("csSaveBtn");
  const status = document.getElementById("csStatus");
  const switches = {
    notifications: document.getElementById("csNotifications"),
    sound: document.getElementById("csSound"),
    showPreviews: document.getElementById("csPreviews"),
    enterToSend: document.getElementById("csEnterToSend")
  };
  const invites = document.getElementById("csGroupInvites");
  let draft = {};
  function fillControls() {
    Object.keys(switches).forEach((key) => {
      switches[key].checked = Boolean(chatSettings[key]);
    });
    invites.value = chatSettings.groupInvites;
    draft = Object.assign({}, chatSettings);
  }

  function markUnsaved() {
    saveBtn.disabled = false;
    status.textContent = "";
  }

  toggle.addEventListener("click", () => {
    const open = toggle.getAttribute("aria-expanded") === "true";
    toggle.setAttribute("aria-expanded", open ? "false" : "true");
    panel.hidden = open;
    actions.hidden = open;
    if (open) {
      fillControls();
      saveBtn.disabled = true;
      status.textContent = "";
    }
  });

  Object.keys(switches).forEach((key) => {
    switches[key].addEventListener("change", () => {
      draft[key] = switches[key].checked;
      markUnsaved();
      if (key === "sound" && switches[key].checked) playMessageSound();
    });
  });
  invites.addEventListener("change", () => {
    draft.groupInvites = invites.value;
    markUnsaved();
  });

  saveBtn.addEventListener("click", () => {
    chatSettings = Object.assign({}, draft);
    saveChatSettings();
    renderList();
    saveBtn.disabled = true;
    status.textContent = "Settings saved ✓";
  });

  fillControls();
})();
/* Notification Sound for New Messages */
let audioCtx = null;
function unlockAudio() {
  try {
    const Ctx = window.AudioContext || window.webkitAudioContext;
    if (!Ctx) return;
    if (!audioCtx) audioCtx = new Ctx();
    if (audioCtx.state === "suspended") audioCtx.resume();
  } catch (e) {}
}
["pointerdown", "keydown"].forEach((type) => {
  document.addEventListener(type, unlockAudio, { once: true });
});
function playMessageSound() {
  try {
    unlockAudio();
    if (!audioCtx) return;
    const now = audioCtx.currentTime;
    [[880, 0], [1175, 0.12]].forEach(([freq, delay]) => {
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = "sine";
      osc.frequency.value = freq;
      gain.gain.setValueAtTime(0.0001, now + delay);
      gain.gain.exponentialRampToValueAtTime(0.15, now + delay + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + delay + 0.25);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start(now + delay);
      osc.stop(now + delay + 0.26);
    });
  } catch (e) {}
}
function receiveIncomingMessage(chatId, message) {
  const convo = getConvo(chatId);
  if (!convo) return;
  convo.messages.push(message);
  const fromOthers = message.senderId !== currentUser.id;
  const chatIsOpen = chatId === activeId && !document.hidden;
  if (fromOthers && !chatIsOpen) convo.unread = (convo.unread || 0) + 1;

  if (chatId === activeId) renderMessages(convo);
  renderList();
  if (fromOthers && !chatIsOpen && chatSettings.notifications && chatSettings.sound) {
    playMessageSound();
  }
}

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
    bottom.appendChild(el("span", "convo-preview", chatSettings.showPreviews || !last ? preview : "Preview hidden"));
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

const MAX_FILES = 5;
const MAX_FILE_MB = 10;
const IMAGE_EXT = ["png", "jpg", "jpeg", "gif", "webp"];
const DOC_EXT = ["pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "csv"];

const attachBtn = document.getElementById("attachBtn");
const fileInput = document.getElementById("fileInput");
const attachTray = document.getElementById("attachTray");
const attachError = document.getElementById("attachError");

let pendingFiles = [];

function extOf(name) {
  const i = name.lastIndexOf(".");
  return i < 0 ? "" : name.slice(i + 1).toLowerCase();
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1048576) return Math.round(bytes / 1024) + " KB";
  return (bytes / 1048576).toFixed(1) + " MB";
}
function safeUrl(url) {
  return /^(blob:|https?:\/\/|\/(?!\/))/.test(url || "") ? url : "#";
}

function addFiles(fileList) {
  const errors = [];
  Array.from(fileList).forEach((file) => {
    const ext = extOf(file.name);
    if (!IMAGE_EXT.includes(ext) && !DOC_EXT.includes(ext)) {
      errors.push("\u201c" + file.name + "\u201d isn\u2019t a supported file type.");
      return;
    }
    if (file.size > MAX_FILE_MB * 1048576) {
      errors.push("\u201c" + file.name + "\u201d is larger than " + MAX_FILE_MB + " MB.");
      return;
    }
    if (pendingFiles.length >= MAX_FILES) {
      errors.push("You can attach up to " + MAX_FILES + " files per message.");
      return;
    }
    const isImage = IMAGE_EXT.includes(ext);
    pendingFiles.push({
      id: "f" + Date.now() + Math.random().toString(36).slice(2),
      file: file,
      isImage: isImage,
      url: isImage ? URL.createObjectURL(file) : ""
    });
  });
  attachError.textContent = Array.from(new Set(errors)).join(" ");
  renderAttachTray();
  updateSendState();
}

function removePendingFile(id) {
  const f = pendingFiles.find((x) => x.id === id);
  if (f && f.url) URL.revokeObjectURL(f.url);
  pendingFiles = pendingFiles.filter((x) => x.id !== id);
  attachError.textContent = "";
  renderAttachTray();
  updateSendState();
}

function clearPendingFiles() {
  pendingFiles.forEach((f) => { if (f.url) URL.revokeObjectURL(f.url); });
  pendingFiles = [];
  attachError.textContent = "";
  renderAttachTray();
}

function renderAttachTray() {
  attachTray.replaceChildren();
  pendingFiles.forEach((f) => {
    const item = el("div", "attach-item");

    if (f.isImage) {
      const img = el("img", "attach-thumb");
      img.src = f.url;
      img.alt = f.file.name;
      item.appendChild(img);
    } else {
      item.appendChild(el("span", "attach-fileicon", extOf(f.file.name).toUpperCase().slice(0, 4)));
    }

    const info = el("div", "attach-info");
    info.appendChild(el("span", "attach-name", f.file.name));
    info.appendChild(el("span", "attach-size", formatSize(f.file.size)));
    item.appendChild(info);

    const remove = el("button", "attach-remove", "\u2715");
    remove.type = "button";
    remove.setAttribute("aria-label", "Remove " + f.file.name);
    remove.addEventListener("click", () => removePendingFile(f.id));
    item.appendChild(remove);

    attachTray.appendChild(item);
  });
}
function renderMessageFiles(attachments) {
  const wrap = el("div", "msg-files");
  attachments.forEach((a) => {
    if (a.type === "image") {
      const link = el("a", "msg-image");
      link.href = safeUrl(a.url);
      link.target = "_blank";
      link.rel = "noopener noreferrer";
      const img = el("img");
      img.src = safeUrl(a.url);
      img.alt = a.name;
      link.appendChild(img);
      wrap.appendChild(link);
    } else {
      const link = el("a", "msg-file");
      link.href = safeUrl(a.url);
      link.download = a.name;
      link.rel = "noopener noreferrer";
      link.appendChild(el("span", "msg-fileicon", extOf(a.name).toUpperCase().slice(0, 4)));
      const info = el("span");
      info.appendChild(el("span", "msg-file-name", a.name));
      info.appendChild(el("span", "msg-file-size", formatSize(a.size)));
      link.appendChild(info);
      wrap.appendChild(link);
    }
  });
  return wrap;
}

function messagePreview(msg) {
  if (msg.text) return msg.text;
  const files = msg.attachments || [];
  if (files.length === 0) return "";
  if (files.every((a) => a.type === "image")) return files.length === 1 ? "\uD83D\uDCF7 Photo" : "\uD83D\uDCF7 " + files.length + " photos";
  return "\uD83D\uDCCE Attachment";
}

attachBtn.addEventListener("click", () => fileInput.click());
fileInput.addEventListener("change", () => {
  addFiles(fileInput.files);
  fileInput.value = "";
});

function updateSendState() {
  sendBtn.disabled = messageInput.value.trim() === "" && pendingFiles.length === 0;
}

function autoGrow() {
  messageInput.style.height = "auto";
  messageInput.style.height = Math.min(messageInput.scrollHeight, 120) + "px";
}

function sendMessage() {
  const text = messageInput.value.trim();
  const convo = getConvo(activeId);
  if ((!text && pendingFiles.length === 0) || !convo) return;

  const attachments = pendingFiles.map((f) => ({
    name: f.file.name,
    size: f.file.size,
    type: f.isImage ? "image" : "file",
    url: f.url || URL.createObjectURL(f.file)
  }));
  convo.messages.push({
    id: "m" + Date.now(),
    senderId: currentUser.id,
    text: text,
    attachments: attachments,
    sentAt: new Date().toISOString()
  });

  pendingFiles = []; 
  attachError.textContent = "";
  renderAttachTray();
  messageInput.value = "";
  autoGrow();
  updateSendState();
  renderMessages(convo);
  renderList();
}

messageInput.addEventListener("input", () => { updateSendState(); autoGrow(); });
messageInput.addEventListener("keydown", (event) => {
  //Enter sends message, Shift+Enter makes a new line. If "Press Enter to send" toggle is off, Ctrl/Cmd+Enter sends message.
  if (event.key !== "Enter") return;
  const send = chatSettings.enterToSend
    ? !event.shiftKey
    : event.ctrlKey || event.metaKey;
  if (send) {
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
    const code = course.code || course.courseCode || course.course_code;
    const title = course.title || course.name || course.courseName || course.course_name;
    if (!code) return;
    const opt = el("option", "", title ? code + " – " + title : code);
    opt.value = code;
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