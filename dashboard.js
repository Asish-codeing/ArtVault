document.addEventListener("DOMContentLoaded", async function () {

   const API_URL = "https://artvault-production-428cc489.up.railway.app";

    const usernameElement = document.getElementById("username");
    const logoutButton = document.getElementById("logoutButton");
    const adminPanelButton = document.getElementById("adminPanelButton");

    const uploadForm = document.getElementById("uploadForm");
    const uploadButton = document.getElementById("uploadButton");
    const uploadMessage = document.getElementById("uploadMessage");

    const exploreGallery = document.getElementById("exploreGallery");
    const myGallery = document.getElementById("myGallery");
    const galleryCount = document.getElementById("galleryCount");

    let currentUserId = null;


    // 1. CHECK LOGIN SESSION

    async function checkLogin() {

        try {

            const response = await fetch(`${API_URL}/api/auth/me`, {
                method: "GET",
                credentials: "include"
            });

            if (response.status === 401) {
                window.location.href = "login.html";
                return false;
            }

            if (!response.ok) {
                throw new Error("Unable to load user information.");
            }

            const user = await response.json();

            currentUserId = user.userId;

            if (usernameElement) {
                usernameElement.textContent = user.username;
            }

            const profileUsername = document.getElementById("profileUsername");

            if (profileUsername) {
                profileUsername.textContent = user.username;
            }

            return true;

        } catch (error) {

            console.error(error);
            alert("Cannot connect to ArtVault server. Please try again.");

            return false;
        }
    }


    // 1A. SHOW ADMIN PANEL BUTTON ONLY FOR ADMIN USERS

    async function checkAdminStatus() {

        if (!adminPanelButton) {
            return;
        }

        try {

            const response = await fetch(`${API_URL}/api/admin/status`, {
                method: "GET",
                credentials: "include"
            });

            if (response.ok) {

                adminPanelButton.style.display = "block";

                adminPanelButton.addEventListener("click", function () {
                    window.location.href = "admin.html";
                });

            } else {

                // Keep the button hidden for normal users.
                adminPanelButton.style.display = "none";

            }

        } catch (error) {

            console.error("Admin status check failed:", error);
            adminPanelButton.style.display = "none";

        }
    }


    // 2. DISPLAY DRAWINGS

    function displayDrawings(drawings, container, allowDelete = false) {

        if (!container) return;

        container.replaceChildren();

        if (drawings.length === 0) {

            const message = document.createElement("p");

            message.textContent = "No drawings available yet. Be the first artist!";

            container.appendChild(message);

            return;
        }

        drawings.forEach(function (drawing) {

            const card = document.createElement("div");
            card.className = "art-card";

            const image = document.createElement("img");

            image.src = drawing.imageUrl;
            image.alt = drawing.title;
            image.loading = "lazy";

            const content = document.createElement("div");
            content.className = "art-card-content";

            const title = document.createElement("h3");
            title.textContent = drawing.title;

            const description = document.createElement("p");

            description.textContent =
                drawing.description || "No description provided.";

            const artist = document.createElement("p");
            artist.className = "artist-name";

            artist.textContent =
                Number(drawing.ownerUserId) === Number(currentUserId)
                    ? "Uploaded by You"
                    : `Artist: ${drawing.artistUsername || "ArtVault Artist"}`;

            content.appendChild(title);
            content.appendChild(description);
            content.appendChild(artist);


            // DELETE BUTTON — ONLY IN MY GALLERY

            if (
                allowDelete &&
                Number(drawing.ownerUserId) === Number(currentUserId)
            ) {

                const deleteButton = document.createElement("button");

                deleteButton.textContent = "🗑️ Delete Drawing";
                deleteButton.className = "delete-drawing-button";

                deleteButton.addEventListener("click", async function () {

                    const confirmDelete = confirm(
                        "Are you sure you want to permanently delete this drawing?"
                    );

                    if (!confirmDelete) return;

                    deleteButton.disabled = true;
                    deleteButton.textContent = "Deleting...";

                    try {

                        const response = await fetch(
                            `${API_URL}/api/drawings/${drawing.id}`,
                            {
                                method: "DELETE",
                                credentials: "include"
                            }
                        );

                        const result = await response.json().catch(() => ({}));

                        if (response.status === 401) {
                            window.location.href = "login.html";
                            return;
                        }

                        if (!response.ok) {
                            throw new Error(
                                result.message ||
                                "Unable to delete drawing."
                            );
                        }

                        alert("Drawing deleted successfully! 🎉");

                        await loadDrawings();

                    } catch (error) {

                        console.error(error);
                        alert(error.message);

                        deleteButton.disabled = false;
                        deleteButton.textContent = "🗑️ Delete Drawing";

                    }

                });

                content.appendChild(deleteButton);
            }


            // COMMENTS SECTION

            const commentsSection = document.createElement("div");
            commentsSection.className = "comments-section";

            const commentsHeading = document.createElement("h4");
            commentsHeading.textContent = "💬 Comments";

            const commentsList = document.createElement("div");
            commentsList.className = "comments-list";

            const commentForm = document.createElement("form");
            commentForm.className = "comment-form";

            const commentInput = document.createElement("textarea");
            commentInput.placeholder = "Write your comment...";
            commentInput.maxLength = 1000;
            commentInput.required = true;

            const commentButton = document.createElement("button");
            commentButton.type = "submit";
            commentButton.textContent = "Post Comment";

            commentForm.appendChild(commentInput);
            commentForm.appendChild(commentButton);

            commentsSection.appendChild(commentsHeading);
            commentsSection.appendChild(commentsList);
            commentsSection.appendChild(commentForm);


            // LOAD COMMENTS

            async function loadComments() {

                try {

                    const response = await fetch(
                        `${API_URL}/api/comments/${drawing.id}`,
                        {
                            method: "GET",
                            credentials: "include"
                        }
                    );

                    if (!response.ok) {
                        throw new Error("Unable to load comments.");
                    }

                    const comments = await response.json();

                    commentsList.replaceChildren();

                    if (comments.length === 0) {
                        commentsList.textContent = "No comments yet. Be the first!";
                    }

                    comments.forEach(function (comment) {

                        const commentItem = document.createElement("div");
                        commentItem.className = "comment-item";

                        const commentAuthor = document.createElement("strong");
                        commentAuthor.textContent = `Artist #${comment.userId}`;

                        const commentText = document.createElement("p");
                        commentText.textContent = comment.commentText;

                        const commentDate = document.createElement("small");
                        commentDate.textContent =
                            new Date(comment.createdAt).toLocaleString();

                        commentItem.appendChild(commentAuthor);
                        commentItem.appendChild(commentText);
                        commentItem.appendChild(commentDate);

                        commentsList.appendChild(commentItem);

                    });

                } catch (error) {

                    console.error(error);
                    commentsList.textContent = "Unable to load comments.";

                }
            }


            // POST COMMENT

            commentForm.addEventListener("submit", async function (event) {

                event.preventDefault();

                const commentText = commentInput.value.trim();

                if (!commentText) return;

                commentButton.disabled = true;
                commentButton.textContent = "Posting...";

                try {

                    const response = await fetch(
                        `${API_URL}/api/comments/${drawing.id}`,
                        {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            credentials: "include",
                            body: JSON.stringify({
                                commentText: commentText
                            })
                        }
                    );

                    const result = await response.json().catch(() => ({}));

                    if (!response.ok) {
                        throw new Error(
                            result.message || "Unable to post comment."
                        );
                    }

                    commentInput.value = "";

                    await loadComments();

                } catch (error) {

                    console.error(error);
                    alert(error.message);

                } finally {

                    commentButton.disabled = false;
                    commentButton.textContent = "Post Comment";

                }

            });


            card.appendChild(image);
            card.appendChild(content);
            card.appendChild(commentsSection);

            container.appendChild(card);

            loadComments();

        });
    }


    // 3. LOAD ALL DRAWINGS

    async function loadDrawings() {

        try {

            const response = await fetch(`${API_URL}/api/drawings`, {
                method: "GET",
                credentials: "include"
            });

            if (response.status === 401) {
                window.location.href = "login.html";
                return;
            }

            if (!response.ok) {
                throw new Error("Unable to load drawings.");
            }

            const drawings = await response.json();

            // Explore Art: show everybody's drawings without delete buttons

            displayDrawings(drawings, exploreGallery, false);

            // My Gallery: show only the logged-in user's drawings

            const userDrawings = drawings.filter(function (drawing) {

                return Number(drawing.ownerUserId) === Number(currentUserId);

            });

            displayDrawings(userDrawings, myGallery, true);

            if (galleryCount) {
                galleryCount.textContent = `${userDrawings.length} Drawings`;
            }

        } catch (error) {

            console.error(error);

            if (exploreGallery) {
                exploreGallery.textContent = "Unable to load artwork.";
            }

            if (myGallery) {
                myGallery.textContent = "Unable to load your gallery.";
            }
        }
    }


    // 4. UPLOAD DRAWING

    if (uploadForm) {

        uploadForm.addEventListener("submit", async function (event) {

            event.preventDefault();

            const title = document.getElementById("drawingTitle").value.trim();

            const description =
                document.getElementById("drawingDescription").value.trim();

            const file =
                document.getElementById("drawingFile").files[0];


            if (!title || !file) {

                uploadMessage.textContent =
                    "Please enter a title and select an image.";

                uploadMessage.style.color = "#ff4f6d";

                return;
            }


            if (file.size > 10 * 1024 * 1024) {

                uploadMessage.textContent =
                    "Image must be smaller than 10 MB.";

                uploadMessage.style.color = "#ff4f6d";

                return;
            }


            const allowedTypes = [
                "image/jpeg",
                "image/png",
                "image/webp"
            ];

            if (!allowedTypes.includes(file.type)) {

                uploadMessage.textContent =
                    "Only JPG, PNG and WEBP images are allowed.";

                uploadMessage.style.color = "#ff4f6d";

                return;
            }


            const formData = new FormData();

            formData.append("title", title);
            formData.append("description", description);
            formData.append("file", file);


            uploadButton.disabled = true;
            uploadButton.textContent = "Uploading...";

            uploadMessage.textContent =
                "Uploading your artwork. Please wait...";

            uploadMessage.style.color = "#00d9ff";


            try {

                const response = await fetch(
                    `${API_URL}/api/drawings/upload`,
                    {
                        method: "POST",
                        credentials: "include",
                        body: formData
                    }
                );

                const result = await response.json().catch(() => ({}));


                if (response.status === 401) {

                    uploadMessage.textContent = "Please log in again.";

                    uploadMessage.style.color = "#ff4f6d";

                    setTimeout(function () {
                        window.location.href = "login.html";
                    }, 1500);

                    return;
                }


                if (!response.ok) {

                    throw new Error(
                        result.message ||
                        "Drawing upload failed."
                    );
                }


                uploadMessage.textContent =
                    "Artwork uploaded successfully! 🎉";

                uploadMessage.style.color = "#00ff99";

                uploadForm.reset();

                await loadDrawings();

            } catch (error) {

                console.error(error);

                uploadMessage.textContent = error.message;
                uploadMessage.style.color = "#ff4f6d";

            } finally {

                uploadButton.disabled = false;
                uploadButton.textContent = "Upload Artwork 🚀";

            }

        });
    }


    // 5. LOGOUT

    if (logoutButton) {

        logoutButton.addEventListener("click", async function () {

            try {

                const response = await fetch(`${API_URL}/api/auth/logout`, {
                    method: "POST",
                    credentials: "include"
                });

                if (!response.ok) {
                    throw new Error("Logout failed.");
                }

                window.location.href = "login.html";

            } catch (error) {

                console.error(error);

                alert("Cannot connect to the server. Please try again.");

            }
        });
    }


    // 6. START DASHBOARD

    const loggedIn = await checkLogin();

    if (loggedIn) {
        await checkAdminStatus();
        await loadDrawings();
    }

});