// Automatically select the correct backend
const API_URL =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "https://artvault-production-428cc489.up.railway.app";

// Get registration form
const registerForm = document.getElementById("registerForm");

if (registerForm) {

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        // Get form values
        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const message = document.getElementById("formMessage");

        // Basic frontend validation
        if (!username || !email || !password) {
            message.textContent = "Please fill in all fields.";
            return;
        }

        message.textContent = "Creating your account...";

        try {

            // Send registration request to backend
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

            // Read server response
            const responseText = await response.text();

            let data = {};

            try {
                data = JSON.parse(responseText);
            } catch (parseError) {
                console.error("Could not parse server response:", responseText);
            }

            // Handle server errors
            if (!response.ok) {

                throw new Error(
                    data.detail ||
                    data.message ||
                    data.error ||
                    `Registration failed. Server returned ${response.status}.`
                );

            }

            // Registration successful
            message.textContent =
                "Registration successful! Redirecting to login...";

            // Redirect to login page
            setTimeout(function () {
                window.location.href = "login.html";
            }, 1500);

        } catch (error) {

            console.error("Registration error:", error);

            message.textContent =
                error.message || "Cannot connect to the server.";

        }

    });

}