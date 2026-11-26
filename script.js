// Welcome screen interaction for ArtVault.
"use strict";

const enterButton = document.getElementById("enterButton");
const welcomeScreen = document.getElementById("welcomeScreen");
const enteredScreen = document.getElementById("enteredScreen");

if (enterButton && welcomeScreen && enteredScreen) {
  enterButton.addEventListener("click", () => {
    welcomeScreen.classList.add("hidden");
    enteredScreen.classList.remove("hidden");
    enteredScreen.classList.add("show");
  });
}
