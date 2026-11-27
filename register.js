// Automatically select the correct backend
const API_URL =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "https://artvault-production-428cc489.up.railway.app";

const registerForm = document.getElementById("registerForm");

if (registerForm) {
    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const message = document.getElementById("formMessage");

        message.textContent = "Creating your account...";

        try {
            const response = await fetch(`${API_URL}/api/auth/register`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    username: username,
                    email: email,
                    password: password
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.detail ||
                    data.message ||
                    "Registration failed."
                );
            }

            message.textContent =
                "Registration successful! Redirecting to login...";

            setTimeout(() => {
                window.location.href = "login.html";
            }, 1500);

        } catch (error) {
            console.error("Registration error:", error);

            message.textContent =
                error.message || "Cannot connect to the server.";
        }
    });
}