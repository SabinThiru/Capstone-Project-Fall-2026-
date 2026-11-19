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

  //  in backend, the picture is located from the signed in user’s account
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

  // Resize uploaded image to a square jpeg
  async function resizeToJpeg(file) {
    const objectUrl = URL.createObjectURL(file);

    try {
      const image = new Image();

      await new Promise((resolve, reject) => {
        image.onload = resolve;
        image.onerror = () => {
          reject(new Error("This image could not be opened."));
        };
        image.src = objectUrl;
      });

      if (!image.naturalWidth || !image.naturalHeight) {
        throw new Error("The image is empty.");
      }

      const canvas = document.createElement("canvas");
      canvas.width = canvas.height = 320;

      const context = canvas.getContext("2d");

      if (!context) {
        throw new Error("Could not process this image.");
      }

      const size = Math.min(
        image.naturalWidth,
        image.naturalHeight
      );

      const cropX = (image.naturalWidth - size) / 2;
      const cropY = (image.naturalHeight - size) / 2;

      context.drawImage(
        image,
        cropX,
        cropY,
        size,
        size,
        0,
        0,
        320,
        320
      );

      return canvas.toDataURL("image/jpeg", 0.80);
    } finally {
      URL.revokeObjectURL(objectUrl);
    }
  }

  // Upload or change profile picture
  if (uploadButton && input) {
    uploadButton.addEventListener("click", () => input.click());

    input.addEventListener("change", async () => {
      const file = input.files?.[0];

      if (!file) return;

      if (!ACCEPTED_TYPES.has(file.type)) {
        setStatus("Choose a JPG, PNG, or WebP picture.", true);
        input.value = "";
        return;
      }

      if (file.size > MAX_FILE_BYTES) {
        setStatus("The picture must be 5 MB or smaller.", true);
        input.value = "";
        return;
      }

      uploadButton.disabled = true;

      try {
        const newPhoto = await resizeToJpeg(file);

        if (USE_PROFILE_BACKEND) {
          const formData = new FormData();

         
          const jpegBlob = await (await fetch(newPhoto)).blob();

          formData.append(
            "photo",
            jpegBlob,
            "profile-picture.jpg"
          );

          await pictureApi("PUT", formData);
          await loadPhotoFromBackend();

          setStatus("Profile picture saved to your account.");
        } else {
          localStorage.setItem(STORAGE_KEY, newPhoto);
          setPhotoSource(newPhoto);

          setStatus(
            "Profile picture updated in your browser."
          );
        }
      } catch (error) {
        const message = error?.name === "QuotaExceededError"
          ? "Browser storage is full; this picture was not saved."
          : (
              error?.message ||
              "Couldn't save the picture. Please try again."
            );

        setStatus(message, true);
      } finally {
        uploadButton.disabled = false;
        input.value = "";
      }
    });
  }

  // Remove profile picture
  if (removeButton) {
    removeButton.addEventListener("click", async () => {
      removeButton.disabled = true;

      try {
        if (USE_PROFILE_BACKEND) {
          await pictureApi("DELETE");
        } else {
          localStorage.removeItem(STORAGE_KEY);
        }

        setPhotoSource(null);

        setStatus(
          "Profile picture removed. Your initial is showing again."
        );
      } catch (error) {
        setStatus(
          error?.message || "Could not remove the profile picture.",
          true
        );

        refreshPhoto();
      }
    });
  }

  // Notification bell functionality for settings page
  if (document.querySelector(".settings-page")) {
    const bell = byId("bellButton");
    const notice = byId("notificationPanel");

    if (bell && notice) {
      const setOpen = open => {
        notice.hidden = !open;
        bell.setAttribute("aria-expanded", String(open));
      };

      bell.addEventListener("click", event => {
        event.stopPropagation();
        setOpen(notice.hidden);
      });

      document.addEventListener("click", event => {
        if (!event.target.closest(".notifications")) {
          setOpen(false);
        }
      });

      document.addEventListener("keydown", event => {
        if (event.key === "Escape" && !notice.hidden) {
          setOpen(false);
          bell.focus();
        }
      });
    }
  }



// Open profile picture section when on another page
  function jumpToPictureSection() {
    if (window.location.hash !== "#profilePictureSettings") {
      return;
    }

    const section = byId("accountSection");
    const pictureRow = byId("profilePictureSettings");

    if (section) {
      section.open = true;
    }

    if (pictureRow) {
      requestAnimationFrame(() => {
        pictureRow.scrollIntoView({ block: "center" });
      });
    }
  }

  window.addEventListener("hashchange", jumpToPictureSection);

  // Update picture when browser storage changes in another tab
  window.addEventListener("storage", event => {
    if (
      !USE_PROFILE_BACKEND &&
      (event.key === STORAGE_KEY || event.key === null)
    ) {
      setPhotoSource(getPhoto());
    }
  });

  // Refresh picture when returning to a page
  window.addEventListener("pageshow", () => {
    if (USE_PROFILE_BACKEND) {
      void loadPhotoFromBackend().catch(error => {
        setStatus(error.message, true);
      });
    } else {
      setPhotoSource(getPhoto());
    }
  });

  // Clean up temporary image URL
  window.addEventListener("pagehide", () => {
    if (activeObjectUrl) {
      URL.revokeObjectURL(activeObjectUrl);
    }

    activeObjectUrl = null;
  });

  // Update initial if user’s name changes
  const name = byId("navUserName");

  if (name) {
    new MutationObserver(refreshPhoto).observe(name, {
      childList: true,
      characterData: true,
      subtree: true
    });
  }

  refreshPhoto();
  jumpToPictureSection();
})();
