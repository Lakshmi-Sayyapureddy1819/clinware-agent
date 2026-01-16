[!IMPORTANT]
**Note to the Hiring Team:**
This agent is currently running on a limited-tier OpenAI API key with a restricted token quota. To ensure you can complete your evaluation successfully, please:
1. **Test purposefully:** Use 1-2 specific queries (e.g., from the UX Testing Suite below) rather than multiple repetitive prompts.
2. **Avoid long conversations:** Large chat histories consume tokens quickly.
3. **Live Link:** You can test the live agent here: [https://clinware-agent.onrender.com/](https://clinware-agent.onrender.com/)

Clinware Market Intelligence Agent
Framework: LangChain4j | Model: OpenAI GPT-4o
🚀 Project Overview
This project is a Java-based AI Agent that acts as a "Market Intelligence Researcher." It is specifically designed to identify news, funding rounds, and product launches for Clinware (a post-acute care AI company). The agent automatically decides when to fetch live internet data using a custom MCP News Tool powered by the Tavily API.

Live Deployment URL: https://clinware-agent.onrender.com/

🛠️ Implementation Details
AI Framework: LangChain4j (v0.31.0) for high-level agent orchestration.

LLM: OpenAI gpt-4o for reasoning and precision tool-calling.

MCP Integration: A custom Java McpClient bridging the agent to a Node.js-based search environment (tavily-server.js) via standard I/O.

Web Server: Javalin (Port 7000) providing a clean, real-time chat interface.

📋 Prerequisites
Java: Version 17 or higher.

Build Tool: Maven.

Node.js: Version 18+ (Required for the search tool).

API Keys:

OPENAI_API_KEY: From OpenAI Platform.

TAVILY_API_KEY: From Tavily AI.

⚙️ Configuration & Setup
Before running the project, create a .env file in the root directory:

Properties

OPENAI_API_KEY=sk-your-openai-key-here
TAVILY_API_KEY=tvly-your-tavily-key-here
PORT=7000
🏃 Running the Project (Step-by-Step)
1. Locally (Maven)
Open your terminal in the project folder and run:

PowerShell

# Step A: Compile the project and download dependencies
mvn clean package -DskipTests

# Step B: Run the Java application
mvn exec:java

# Step C: Access the UI
# Open your browser to http://localhost:7000
2. Locally (Docker)
If you have Docker installed:

PowerShell

# Build the image
docker build -t clinware-agent .

# Run the container with your .env file
docker run -p 7000:7000 --env-file .env clinware-agent
🧪 User Experience (UX) Testing Suite
Use these 20 questions to test the agent's performance, accuracy, and user experience.

A. Functional Tool-Calling Tests
"What is the latest news about Clinware's funding as of 2025?"

"Has Clinware launched any new products recently for Skilled Nursing Facilities?"

"Who are the main competitors of Clinware in the post-acute care AI space?"

"Search for Clinware's recent market positioning updates."

"Can you find any recent partnership announcements involving Clinware?"

B. Accuracy & Grounding Tests
"Provide a summary of Clinware's Seed funding round."

"What specific AI features does Clinware offer for patient admissions?"

"Is there any news about Clinware expanding into home health care?"

"Verify if Clinware has any recent mentions on LinkedIn or industry blogs."

"Tell me about Clinware's leadership team based on recent news."

C. Conversation & UX Flow Tests
"Hello! Can you help me research a healthcare company?"

"What can you do? List your capabilities."

"Summarize all the news you found about Clinware into three bullet points."

"I missed the last part, can you explain the funding details again?"

"Can you provide the sources or snippets for the news you just found?"

D. Edge Cases & Error Handling
"What is the latest news about [Fake Company Name]?" (Tests 'No data found' logic)

"Search for news about Clinware but ignore anything from before 2024."

"Explain Clinware's tech stack." (Tests if agent admits limit of info vs hallucinating)

"What is the current stock price of Clinware?" (Tests handling of private company data)

"Give me a summary of Clinware news using only one single sentence."
