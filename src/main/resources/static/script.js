const messageInput = document.getElementById("messageInput");
const chatMessages = document.getElementById("chatMessages");
const sendButton = document.getElementById("sendButton");

let conversationId =
    localStorage.getItem("studySparkConversationId");


// -----------------------------------------
// PAGE LOAD
// -----------------------------------------

window.addEventListener("load", async () => {

    if (!conversationId) {

        await createNewConversation();

    } else {

        await loadConversation();
    }
    await loadRecentChats();
});


// -----------------------------------------
// CREATE NEW CONVERSATION
// -----------------------------------------

async function createNewConversation() {

    try {

        const response = await fetch(
            "/api/chat/conversation",
            {
                method: "POST"
            }
        );

        if (!response.ok) {
            throw new Error("Could not create conversation");
        }

        const data = await response.json();

        conversationId = data.conversationId;

        localStorage.setItem(
            "studySparkConversationId",
            conversationId
        );

    } catch (error) {

        console.error(error);

    }
}


// -----------------------------------------
// LOAD OLD CONVERSATION
// -----------------------------------------

async function loadConversation() {

    try {

        const response = await fetch(
            `/api/chat/conversation/${conversationId}`
        );

        if (!response.ok) {

            await createNewConversation();
            return;
        }

        const messages = await response.json();

        // Don't remove the welcome card,
        // but add saved messages underneath it.

        messages.forEach(message => {

            const isUser =
                message.role === "USER";

            addMessage(
                message.content,
                isUser
            );

        });

    } catch (error) {

        console.error(
            "Could not load conversation:",
            error
        );

    }
}


// -----------------------------------------
// SEND MESSAGE
// -----------------------------------------

async function sendMessage() {

    const message =
        messageInput.value.trim();

    if (!message) {
        return;
    }

    // Show student's message
    addMessage(message, true);

    // Clear input
    messageInput.value = "";

    // Disable button while waiting
    sendButton.disabled = true;

    sendButton.querySelector("span").textContent =
        "Thinking...";

    try {

        const response = await fetch(
            "/api/chat",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body: JSON.stringify({

                    conversationId:
                        conversationId,

                    message:
                        message

                })
            }
        );

        if (!response.ok) {

            throw new Error(
                "Server returned an error"
            );

        }

        const data =
            await response.json();

        // Backend may create a conversation
        // automatically if needed.
        conversationId =
            data.conversationId;

        localStorage.setItem(
            "studySparkConversationId",
            conversationId
        );

        // Show AI response
        addMessage(
            data.response,
            false
        );
        await loadRecentChats();

    } catch (error) {

        console.error(error);

        addMessage(
            "Sorry 😅 Something went wrong. Please try again.",
            false
        );

    } finally {

        sendButton.disabled = false;

        sendButton.querySelector("span").textContent =
            "Send";

        messageInput.focus();
    }
}


// -----------------------------------------
// ADD MESSAGE TO CHAT
// -----------------------------------------

function addMessage(text, isUser) {

    const messageDiv =
        document.createElement("div");

    messageDiv.className =
        isUser
            ? "message user-message"
            : "message assistant-message";

    if (isUser) {

        messageDiv.innerHTML = `

            <div class="message-content">

                <div class="message-name">
                    You
                </div>

                <div class="bubble user-bubble">
                    <p>${escapeHtml(text)}</p>
                </div>

            </div>

            <div class="avatar user-avatar">
                🎓
            </div>
        `;

    } else {

        messageDiv.innerHTML = `

            <div class="avatar assistant-avatar">
                🤖
            </div>

            <div class="message-content">

                <div class="message-name">
                    StudySpark
                </div>

                <div class="bubble assistant-bubble">
                    <p>${escapeHtml(text)}</p>
                </div>

            </div>
        `;
    }

    chatMessages.appendChild(messageDiv);

    chatMessages.scrollTop =
        chatMessages.scrollHeight;
}


// -----------------------------------------
// SUGGESTION BUTTONS
// -----------------------------------------

function useSuggestion(text) {

    messageInput.value = text;

    sendMessage();
}


// -----------------------------------------
// ENTER TO SEND
// -----------------------------------------

function handleKeyDown(event) {

    if (
        event.key === "Enter" &&
        !event.shiftKey
    ) {

        event.preventDefault();

        sendMessage();
    }
}


// -----------------------------------------
// NEW CHAT
// -----------------------------------------

async function newChat() {

    try {

        const response = await fetch(
            "/api/chat/conversation",
            {
                method: "POST"
            }
        );

        if (!response.ok) {
            throw new Error(
                "Could not create new chat"
            );
        }

        const data =
            await response.json();

        conversationId =
            data.conversationId;

        localStorage.setItem(
            "studySparkConversationId",
            conversationId
        );

        // Clear current messages
        chatMessages.innerHTML = `

            <div class="welcome-card">

                <div class="welcome-icon">
                    ✨
                </div>

                <h2>
                    Hey there, future genius! 👋
                </h2>

                <p>
                    I'm StudySpark, your AI study buddy.
                    Ask me anything and let's learn
                    something amazing together!
                </p>

                <div class="suggestions">

                    <button
                        onclick="useSuggestion('Explain photosynthesis in simple words')">
                        🌱 Explain photosynthesis
                    </button>

                    <button
                        onclick="useSuggestion('Help me understand algebra')">
                        🔢 Help with algebra
                    </button>

                    <button
                        onclick="useSuggestion('What are the main parts of a cell?')">
                        🔬 Learn about cells
                    </button>

                </div>

            </div>

            <div class="message assistant-message">

                <div class="avatar assistant-avatar">
                    🤖
                </div>

                <div class="message-content">

                    <div class="message-name">
                        StudySpark
                    </div>

                    <div class="bubble assistant-bubble">

                        <p>
                            Hello! 🌟 I'm ready to help you
                            learn. What would you like to
                            study today?
                        </p>

                    </div>

                </div>

            </div>
        `;

        messageInput.value = "";

        messageInput.focus();
        await loadRecentChats();

    } catch (error) {

        console.error(error);

        alert(
            "Could not start a new chat."
        );
    }
}


// -----------------------------------------
// SECURITY: ESCAPE HTML
// -----------------------------------------

function escapeHtml(text) {

    const div =
        document.createElement("div");

    div.textContent = text;

    return div.innerHTML;
}


// -----------------------------------------
// RECENT CHATS SIDEBAR
// -----------------------------------------

async function loadRecentChats() {
    const container = document.getElementById("recentChats");

    if (!container) return;

    try {
        const response = await fetch("/api/chat/conversations");

        if (!response.ok) {
            throw new Error("Could not load recent chats");
        }

        const conversations = await response.json();

        container.innerHTML = "";

        if (conversations.length === 0) {
            container.textContent = "No conversations yet";
            return;
        }

        conversations.forEach(chat => {
            const button = document.createElement("button");

            button.type = "button";
            button.className = "recent-chat-item";

            if (chat.conversationId === conversationId) {
                button.classList.add("active");
            }

            button.textContent = "💬 " +
                (chat.title || "New Study Chat");

            button.addEventListener("click", () => {
                openPreviousChat(chat.conversationId);
            });

            container.appendChild(button);
        });

    } catch (error) {
        console.error("Recent chats error:", error);
        container.textContent = "Could not load chats";
    }
}

// -----------------------------------------
// OPEN A PREVIOUS CHAT
// -----------------------------------------

async function openPreviousChat(selectedId) {
    if (sendButton.disabled) return;

    try {
        const response = await fetch(
            `/api/chat/conversation/${encodeURIComponent(selectedId)}`
        );

        if (!response.ok) {
            throw new Error("Could not open conversation");
        }

        const messages = await response.json();

        conversationId = selectedId;

        localStorage.setItem(
            "studySparkConversationId",
            conversationId
        );

        // Clear old messages before loading the selected chat.
        chatMessages.innerHTML = "";

        messages.forEach(message => {
            addMessage(
                message.content,
                message.role === "USER"
            );
        });

        messageInput.value = "";

        await loadRecentChats();

    } catch (error) {
        console.error(error);
        alert("Could not open this conversation.");
    }
}
