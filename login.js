const API_URL = "https://artvault-production-428cc489.up.railway.app";

const loginForm = document.getElementById("loginForm");
const formMessage = document.getElementById("formMessage");

loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const identity = document.getElementById("identity").value.trim();
    const password = document.getElementById("password").value;

    if (!identity || !password) {
        showMessage("Please enter your username/email and password.", false);
        return;
    }

    const submitButton = loginForm.querySelector("button");

    submitButton.disabled = true;
    submitButton.textContent = "LOGGING IN...";

    try {
        const response = await fetch(`${API_URL}/api/auth/login`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                identity: identity,
                password: password
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Invalid username or password.");
        }

        showMessage("Welcome back, " + data.username + "!", true);

        setTimeout(function () {
            if (data.role === "ADMIN") {
                window.location.href = "admin.html";
            } else {
                window.location.href = "dashboard.html";
            }
        }, 1000);

    } catch (error) {
        showMessage(
            error.message || "Unable to connect to the server.",
            false
        );

    } finally {
        submitButton.disabled = false;
        submitButton.textContent = "LOG IN";
    }
});

function showMessage(message, success) {
    formMessage.textContent = message;
    formMessage.style.color = success ? "#00e676" : "#ff4f81";
}