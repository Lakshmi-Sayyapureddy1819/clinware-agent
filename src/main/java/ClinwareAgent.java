import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ClinwareAgent {

    interface ResearcherAgent {
        @SystemMessage("""
                    You are the Clinware Intelligence Agent.

                    CRITICAL RULES:
                    1. You have a tool called 'search_news'. You MUST use it for any question about Clinware, news, funding, or people.
                    2. TRUST THE TOOL. If the tool says "No news found" or returns a system message starting with "SYSTEM_MSG:", you MUST say "I could not find any information." or a clear statement that no relevant results were found.
                    3. DO NOT HALLUCINATE. Never invent scandals, stock prices, or events. If the search comes back empty, admit you don't know.
                    4. If the user asks for a specific fact (like a year or name) and the tool doesn't have it, do not guess.
                """)
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        // 1. Load keys (Works for both Local .env AND Docker)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // Important for Docker!
                .load();

        String openAiKey = dotenv.get("OPENAI_API_KEY");
        if (openAiKey == null || openAiKey.isEmpty()) {
            // fallback to system environment (useful when running in Docker with
            // --env-file)
            openAiKey = System.getenv("OPENAI_API_KEY");
        }

        // Masked log for debugging (does NOT expose full key)
        String openAiMasked = (openAiKey != null)
                ? (openAiKey.length() > 6 ? openAiKey.substring(0, 6) + "..." : openAiKey)
                : "missing";
        System.out.println("OPENAI key present: " + openAiMasked);

        // 2. Safety Check
        if (openAiKey == null || openAiKey.isEmpty()) {
            System.err.println("CRITICAL ERROR: OPENAI_API_KEY is missing!");
            System.err.println("Please create a .env file or set environment variables.");
            return;
        }

        // 3. Build Agent
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(openAiKey)
                .modelName("gpt-4o")
                .temperature(0.0) // Strict: no hallucinations
                .build();

        ResearcherAgent agent = AiServices.builder(ResearcherAgent.class)
                .chatLanguageModel(model)
                .tools(new McpClient())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        // 4. Start Web Server (PORT from env, default 7000)
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7000"));
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        });

        // Health endpoint
        app.get("/health", ctx -> ctx.result("OK"));

        app.start(port);

        System.out.println("--- SYSTEM READY ---");
        System.out.println("Web Interface: http://localhost:" + port);

        // 5. API Endpoint
        app.post("/chat", ctx -> {
            String query = ctx.body();
            System.out.println("Web Request: " + query);
            String response = agent.chat(query);
            ctx.result(response);

            // Persist the interaction to responses.jsonl (masked for secrets)
            try {
                appendResponseLog(query, response);
            } catch (Exception e) {
                System.err.println("WARN: failed to write response log: " + e.getMessage());
            }
        });

    }

    // Helper: append a JSONL line to responses.jsonl
    private static void appendResponseLog(String query, String response) {
        String maskedQuery = maskSecrets(query);
        String maskedResponse = maskSecrets(response);
        String logLine = "{" +
                "\"timestamp\":\"" + Instant.now().toString() + "\"," +
                "\"query\":" + jsonEscape(maskedQuery) + "," +
                "\"response\":" + jsonEscape(maskedResponse) + "}" + System.lineSeparator();
        try {
            Files.write(Paths.get("responses.jsonl"), logLine.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            // Do not fail the request flow on logging errors
            System.err.println("ERROR writing responses.jsonl: " + e.getMessage());
        }
    }

    private static String jsonEscape(String s) {
        if (s == null)
            return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    // Mask likely API keys to avoid leaking secrets to disk
    private static String maskSecrets(String s) {
        if (s == null)
            return null;
        // Keep a short prefix and replace the rest with ...
        s = s.replaceAll("(sk-[A-Za-z0-9_\\-]{6})[A-Za-z0-9_\\-]*", "$1...");
        s = s.replaceAll("(tvly-[A-Za-z0-9_\\-]{6})[A-Za-z0-9_\\-]*", "$1...");
        return s;
    }
}