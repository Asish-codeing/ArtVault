const API_URL = "https://artvault-production-428cc489.up.railway.app";
// =====================================
// ELEMENTS
// =====================================

// USER ELEMENTS
const usersTable = document.getElementById("usersTable");
const totalUsers = document.getElementById("totalUsers");
const userSearch = document.getElementById("userSearch");

// DRAWING ELEMENTS
const drawingsTable = document.getElementById("drawingsTable");
const totalDrawings = document.getElementById("totalDrawings");
const drawingSearch = document.getElementById("drawingSearch");
const refreshDrawingsButton = document.getElementById("refreshDrawings");

// ACTIVITY ELEMENTS
const activitiesTable = document.getElementById("activitiesTable");
const refreshActivitiesButton = document.getElementById("refreshActivities");

// LOGOUT
const logoutButton = document.getElementById("logoutButton");

let allUsers = [];
let allDrawings = [];
let allActivities = [];


// =====================================
// 1. LOAD ADMIN USERS
// =====================================

async function loadUsers() {

    try {

        const response = await fetch(`${API_URL}/api/admin/users`, {
            method: "GET",
            credentials: "include"
        });

        if (response.status === 401) {
            alert("Please log in first.");
            window.location.href = "login.html";
            return;
        }

        if (response.status === 403) {
            alert("Admin access required!");
            window.location.href = "dashboard.html";
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load users.");
        }

        allUsers = await response.json();

        if (totalUsers) {
            totalUsers.textContent = allUsers.length;
        }

        displayUsers(allUsers);

    } catch (error) {

        console.error("User loading error:", error);

        if (usersTable) {
            usersTable.innerHTML = `
                <tr>
                    <td colspan="4">
                        Unable to connect to ArtVault backend.
                    </td>
                </tr>
            `;
        }
    }
}


// =====================================
// 2. DISPLAY USERS
// =====================================

function displayUsers(users) {

    if (!usersTable) return;

    usersTable.innerHTML = "";

    if (users.length === 0) {

        usersTable.innerHTML = `
            <tr>
                <td colspan="4">No users found.</td>
            </tr>
        `;

        return;
    }

    users.forEach(user => {

        const row = document.createElement("tr");

        const idCell = document.createElement("td");
        idCell.textContent = user.id;

        const usernameCell = document.createElement("td");
        usernameCell.textContent = user.username;

        const emailCell = document.createElement("td");
        emailCell.textContent = user.email;

        const roleCell = document.createElement("td");

        const roleSelect = document.createElement("select");

        ["USER", "ADMIN"].forEach(role => {

            const option = document.createElement("option");

            option.value = role;
            option.textContent = role;

            if (user.role === role) {
                option.selected = true;
            }

            roleSelect.appendChild(option);

        });

        roleSelect.style.padding = "7px";
        roleSelect.style.borderRadius = "6px";
        roleSelect.style.marginRight = "8px";

        // CHANGE ROLE
        roleSelect.addEventListener("change", async function () {

            const oldRole = user.role;
            const newRole = roleSelect.value;

            const confirmed = confirm(
                `Change ${user.username}'s role from ${oldRole} to ${newRole}?`
            );

            if (!confirmed) {
                roleSelect.value = oldRole;
                return;
            }

            roleSelect.disabled = true;

            try {

                const response = await fetch(
                    `${API_URL}/api/admin/users/${user.id}/role`,
                    {
                        method: "PUT",
                        credentials: "include",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            role: newRole
                        })
                    }
                );

                const result = await response.json().catch(() => ({}));

                if (!response.ok) {
                    throw new Error(
                        result.detail ||
                        result.message ||
                        "Unable to update user role."
                    );
                }

                user.role = newRole;

                alert("User role updated successfully! 🎉");

            } catch (error) {

                console.error("Role update error:", error);

                alert(error.message);

                roleSelect.value = oldRole;

            } finally {

                roleSelect.disabled = false;

            }

        });


        // DELETE USER BUTTON
        const deleteButton = document.createElement("button");

        deleteButton.textContent = "🗑️ Delete";
        deleteButton.type = "button";

        deleteButton.style.padding = "7px 10px";
        deleteButton.style.border = "none";
        deleteButton.style.borderRadius = "6px";
        deleteButton.style.cursor = "pointer";

        // DELETE USER
        deleteButton.addEventListener("click", async function () {

            const confirmed = confirm(
                `Are you sure you want to permanently delete the account "${user.username}"?`
            );

            if (!confirmed) return;

            deleteButton.disabled = true;
            deleteButton.textContent = "Deleting...";

            try {

                const response = await fetch(
                    `${API_URL}/api/admin/users/${user.id}`,
                    {
                        method: "DELETE",
                        credentials: "include"
                    }
                );

                const result = await response.json().catch(() => ({}));

                if (!response.ok) {
                    throw new Error(
                        result.detail ||
                        result.message ||
                        "Unable to delete user."
                    );
                }

                alert("User deleted successfully! 🎉");

                await loadUsers();

            } catch (error) {

                console.error("User deletion error:", error);

                alert(error.message);

                deleteButton.disabled = false;
                deleteButton.textContent = "🗑️ Delete";

            }

        });

        roleCell.appendChild(roleSelect);
        roleCell.appendChild(deleteButton);

        row.append(
            idCell,
            usernameCell,
            emailCell,
            roleCell
        );

        usersTable.appendChild(row);

    });

}


// =====================================
// 3. SEARCH USERS
// =====================================

if (userSearch) {

    userSearch.addEventListener("input", function () {

        const searchText = this.value.toLowerCase().trim();

        const filteredUsers = allUsers.filter(user =>
            (user.username || "").toLowerCase().includes(searchText)
        );

        displayUsers(filteredUsers);

    });

}


// =====================================
// 4. LOAD ALL DRAWINGS
// =====================================

async function loadDrawings() {

    if (!drawingsTable) return;

    drawingsTable.innerHTML = `
        <tr>
            <td colspan="5">Loading drawings...</td>
        </tr>
    `;

    try {

        const response = await fetch(`${API_URL}/api/drawings`, {
            method: "GET",
            credentials: "include"
        });

        if (response.status === 401) {
            alert("Please log in first.");
            window.location.href = "login.html";
            return;
        }

        if (response.status === 403) {
            alert("Admin access required!");
            window.location.href = "dashboard.html";
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load drawings.");
        }

        allDrawings = await response.json();

        if (totalDrawings) {
            totalDrawings.textContent = allDrawings.length;
        }

        displayDrawings(allDrawings);

    } catch (error) {

        console.error("Drawing loading error:", error);

        drawingsTable.innerHTML = `
            <tr>
                <td colspan="5">
                    Unable to load drawings. Please try again.
                </td>
            </tr>
        `;

    }
}


// =====================================
// 5. DISPLAY DRAWINGS
// =====================================

function displayDrawings(drawings) {

    if (!drawingsTable) return;

    drawingsTable.innerHTML = "";

    if (drawings.length === 0) {

        drawingsTable.innerHTML = `
            <tr>
                <td colspan="5">No drawings found.</td>
            </tr>
        `;

        return;
    }

    drawings.forEach(drawing => {

        const row = document.createElement("tr");

        const idCell = document.createElement("td");
        idCell.textContent = drawing.id;

        const imageCell = document.createElement("td");

        const image = document.createElement("img");

        image.src = drawing.imageUrl;
        image.alt = drawing.title || "Artwork";
        image.style.width = "75px";
        image.style.height = "75px";
        image.style.objectFit = "cover";
        image.style.borderRadius = "8px";

        imageCell.appendChild(image);

        const titleCell = document.createElement("td");
        titleCell.textContent = drawing.title || "Untitled";

        const artistCell = document.createElement("td");
        artistCell.textContent =
            drawing.artistUsername || "Unknown Artist";

        const actionCell = document.createElement("td");

        const deleteButton = document.createElement("button");

        deleteButton.textContent = "🗑️ Delete";
        deleteButton.type = "button";
        deleteButton.style.cursor = "pointer";

        deleteButton.addEventListener("click", async function () {

            const confirmed = confirm(
                `Are you sure you want to delete "${drawing.title}"?`
            );

            if (!confirmed) return;

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
                    alert("Please log in again.");
                    window.location.href = "login.html";
                    return;
                }

                if (response.status === 403) {
                    throw new Error("Admin permission required.");
                }

                if (!response.ok) {
                    throw new Error(
                        result.detail ||
                        result.message ||
                        "Unable to delete drawing."
                    );
                }

                alert("Drawing deleted successfully! 🎉");

                await loadDrawings();
                await loadActivities();

            } catch (error) {

                console.error("Drawing deletion error:", error);

                alert(error.message);

                deleteButton.disabled = false;
                deleteButton.textContent = "🗑️ Delete";

            }

        });

        actionCell.appendChild(deleteButton);

        row.append(
            idCell,
            imageCell,
            titleCell,
            artistCell,
            actionCell
        );

        drawingsTable.appendChild(row);

    });

}


// =====================================
// 6. SEARCH DRAWINGS
// =====================================

if (drawingSearch) {

    drawingSearch.addEventListener("input", function () {

        const searchText = this.value.toLowerCase().trim();

        const filteredDrawings = allDrawings.filter(drawing => {

            const title = (drawing.title || "").toLowerCase();

            const artist = (drawing.artistUsername || "").toLowerCase();

            return title.includes(searchText) ||
                   artist.includes(searchText);

        });

        displayDrawings(filteredDrawings);

    });

}


// =====================================
// 7. REFRESH DRAWINGS
// =====================================

if (refreshDrawingsButton) {

    refreshDrawingsButton.addEventListener("click", function () {
        loadDrawings();
    });

}


// =====================================
// 8. LOAD ACTIVITY RECORDS
// =====================================

async function loadActivities() {

    if (!activitiesTable) return;

    activitiesTable.innerHTML = `
        <tr>
            <td colspan="5">Loading activities...</td>
        </tr>
    `;

    try {

        const response = await fetch(
            `${API_URL}/api/admin/activities`,
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (response.status === 401) {
            alert("Please log in again.");
            window.location.href = "login.html";
            return;
        }

        if (response.status === 403) {
            alert("Admin access required!");
            window.location.href = "dashboard.html";
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load activity records.");
        }

        allActivities = await response.json();

        displayActivities(allActivities);

    } catch (error) {

        console.error("Activity loading error:", error);

        activitiesTable.innerHTML = `
            <tr>
                <td colspan="5">
                    Unable to load activities. Please try again.
                </td>
            </tr>
        `;

    }

}


// =====================================
// 9. DISPLAY ACTIVITY RECORDS
// =====================================

function displayActivities(activities) {

    if (!activitiesTable) return;

    activitiesTable.innerHTML = "";

    if (activities.length === 0) {

        activitiesTable.innerHTML = `
            <tr>
                <td colspan="5">No activities recorded yet.</td>
            </tr>
        `;

        return;
    }

    activities.forEach(activity => {

        const row = document.createElement("tr");

        const idCell = document.createElement("td");
        idCell.textContent = activity.id ?? "-";

        const usernameCell = document.createElement("td");
        usernameCell.textContent = activity.username || "Unknown User";

        const actionCell = document.createElement("td");
        actionCell.textContent = activity.action || "Unknown Activity";

        const descriptionCell = document.createElement("td");
        descriptionCell.textContent = activity.description || "-";

        const dateCell = document.createElement("td");

        if (activity.createdAt) {

            const date = new Date(activity.createdAt);

            dateCell.textContent = isNaN(date.getTime())
                ? activity.createdAt
                : date.toLocaleString();

        } else {

            dateCell.textContent = "-";

        }

        row.append(
            idCell,
            usernameCell,
            actionCell,
            descriptionCell,
            dateCell
        );

        activitiesTable.appendChild(row);

    });

}


// =====================================
// 10. REFRESH ACTIVITIES
// =====================================

if (refreshActivitiesButton) {

    refreshActivitiesButton.addEventListener("click", function () {
        loadActivities();
    });

}


// =====================================
// 11. ADMIN LOGOUT
// =====================================

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

            console.error("Logout error:", error);

            alert("Unable to log out. Please try again.");

        }

    });

}


// =====================================
// 12. START ADMIN DASHBOARD
// =====================================

loadUsers();
loadDrawings();
loadActivities();