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
