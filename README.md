# ✨ StudySpark – AI Study Chatbot

**StudySpark** is an AI-powered study assistant designed to help students understand academic concepts, ask questions, and learn interactively.

The application uses Google Gemini AI to generate educational responses and MySQL to store conversations, allowing students to revisit previous discussions.

## 🚀 Features

- **AI-Powered Chatbot:** Ask questions and receive responses powered by Google Gemini.
- **Conversation Memory:** Maintains context within conversations.
- **Recent Chats:** Access and reopen previous conversations.
- **New Study Chat:** Start a fresh conversation anytime.
- **Persistent Chat History:** Stores messages in a MySQL database.
- **Interactive Interface:** User-friendly design using HTML, CSS, and JavaScript.
- **Error Handling:** Displays helpful messages when requests fail.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | Backend framework and REST APIs |
| Google Gemini API | AI-generated responses |
| MySQL | Database for conversations |
| HTML | Webpage structure |
| CSS | User interface design |
| JavaScript | Frontend interactions |
| Maven | Project build and dependency management |
| Git and GitHub | Version control |

## 📁 Project Structure

```text
ai-study-chatbot/
├── src/
│   ├── main/
│   │   ├── java/com/example/ai_study_chatbot/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── style.css
│   │       │   └── script.js
│   │       └── application.properties
│   └── test/
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## ⚙️ How to Run the Project

### Prerequisites

- Java JDK compatible with the project's Spring Boot version
- MySQL Server
- Maven (or the included Maven wrapper)
- Google Gemini API key

### Setup

1. Clone the repository:

```bash
git clone https://github.com/ksainivriti-design/StudySpark-AI-Study-Chatbot.git
```

2. Open the project directory containing `pom.xml`.

3. Create the MySQL database:

```sql
CREATE DATABASE ai_study_chatbot;
```

4. Configure `application.properties` with the correct database connection and Gemini settings.

5. Set the environment variables `GEMINI_API_KEY` and `DB_PASSWORD` in your local environment. Never commit actual credentials.

6. Run the application on Windows:

```powershell
.\mvnw spring-boot:run
```

7. Open the application:

```text
http://localhost:8080/
```

## 🔌 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/chat/conversation` | Create a conversation |
| POST | `/api/chat` | Send a message to the chatbot |
| GET | `/api/chat/conversation/{conversationId}` | Retrieve conversation messages |
| GET | `/api/chat/conversations` | Retrieve recent conversations |

## 🔮 Future Enhancements

- User login and authentication
- Chat deletion and renaming
- Personalized study recommendations
- Study notes and summaries
- Quiz generation
- Deployment to a cloud platform

## 👩‍💻 Project Repository

[StudySpark AI Study Chatbot](https://github.com/ksainivriti-design/StudySpark-AI-Study-Chatbot)

**Built to make learning more interactive and accessible.**
