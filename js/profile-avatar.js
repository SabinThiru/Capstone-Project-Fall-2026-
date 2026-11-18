(() => {
  "use strict";

  // Switch to true when the backend API and login are ready
  const USE_PROFILE_BACKEND = false;


  const PROFILE_PICTURE_URL = "http://localhost:8080/api/users/me/profile-picture";
  const STORAGE_KEY = "campuspath-profile-picture-demo-v1";
  const MAX_FILE_BYTES = 5 * 1024 * 1024;
  const ACCEPTED_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

  const byId = id => document.getElementById(id);
  const trigger = byId("profileAvatarTrigger");
  const menu = byId("profileAvatarMenu");
  const input = byId("profilePictureInput");
  const uploadButton = byId("uploadProfilePictureBtn");
  const removeButton = byId("removeProfilePictureBtn");
  const status = byId("profilePictureStatus");

  function getPhoto() {
    try {
      const value = localStorage.getItem(STORAGE_KEY);
      return value && value.startsWith("data:image/jpeg;base64,") ? value : null;
    } catch (_) {
      return null;
    }
  }

  //  in backend, the picture is located from the signed in user’s ccount.
  // in local mode, picture is only in browser storage
  let currentPhotoSrc = USE_PROFILE_BACKEND ? null : getPhoto();
  let activeObjectUrl = null;

  function setPhotoSource(src, isObjectUrl = false) {
    if (activeObjectUrl) URL.revokeObjectURL(activeObjectUrl);
    activeObjectUrl = isObjectUrl ? src : null;
    currentPhotoSrc = src;
    refreshPhoto();
  }

// Backend API requests
  async function pictureApi(method, body) {
    // Replace idToken if authentication uses access tokens or cookies.
    const token = sessionStorage.getItem("idToken");

    if (!token) {
      throw new Error("Please sign in to manage your profile picture.");
    }

    const response = await fetch(PROFILE_PICTURE_URL, {
      method,
      headers: {
        Authorization: `Bearer ${token}`
      },
      ...(body ? { body } : {}),
      cache: "no-store"
    });

    if (method === "GET" && response.status === 204) {
      return response;
    }

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        throw new Error(
          "Your session cannot access profile pictures. Please sign in again."
        );
      }

      throw new Error(
        `Could not ${
          method === "GET" ? "load" : "save or remove"
        } your picture (HTTP ${response.status}).`
      );
    }

    return response;
  }

  // Retrieve picture from backend
  async function loadPhotoFromBackend() {
    const response = await pictureApi("GET");

    if (response.status === 204) {
      setPhotoSource(null);
      return;
    }

    const blob = await response.blob();

    if (!blob.type.startsWith("image/")) {
      throw new Error("The server did not return an image.");
    }

    setPhotoSource(URL.createObjectURL(blob), true);
  }

  // Display student's initial when no photo exists
  function getInitial() {
    const name = (byId("navUserName")?.textContent || "Student").trim();
    return (name[0] || "S").toUpperCase();
  }

  // Show success or error messages
  function setStatus(message, isError = false) {
    if (!status) return;

    status.textContent = message;
    status.className =
      "form-status picture-status " + (isError ? "error" : "success");
  }

  // Update avatar displays throughout the current page
  function refreshPhoto() {
    const dataUrl = currentPhotoSrc;
    const initial = getInitial();

    [
      byId("userAvatar"),
      byId("profilePicturePreview"),
      byId("studentInitials")
    ]
      .filter(Boolean)
      .forEach(element => {
        element.textContent = initial;
        element.classList.toggle("has-picture", Boolean(dataUrl));

        element.style.backgroundImage = dataUrl
          ? `url("${dataUrl}")`
          : "";

        if (element.id === "profilePicturePreview") {
          element.setAttribute(
            "aria-label",
            dataUrl
              ? "Current profile photo"
              : "Current profile initial " + initial
          );
        }
      });

    if (removeButton) {
      removeButton.disabled = !dataUrl;
    }
  }
// Open and close avatar dropdown
  function setMenu(open) {
    if (!menu || !trigger) return;

    menu.hidden = !open;
    trigger.setAttribute("aria-expanded", String(open));
  }

  if (trigger && menu) {
    trigger.addEventListener("click", event => {
      event.stopPropagation();
      setMenu(menu.hidden);
    });

    document.addEventListener("click", event => {
      if (!event.target.closest(".profile-avatar-widget")) {
        setMenu(false);
      }
    });

    document.addEventListener("keydown", event => {
      if (event.key === "Escape" && !menu.hidden) {
        setMenu(false);
        trigger.focus();
      }
    });
  }
