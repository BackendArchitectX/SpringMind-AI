import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionTool;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.openai.core.ObjectMappers.jsonMapper;

public class Main {
    public static void main(String[] args) throws Exception {
        String prompt = null;
        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i]) && i + 1 < args.length) {
                prompt = args[i + 1];
            }
        }
        if (prompt == null || prompt.isEmpty()) {
            throw new RuntimeException("error: -p flag is required");
        }

        String apiKey = System.getenv("OPENROUTER_API_KEY");
        String baseUrl = System.getenv("OPENROUTER_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) baseUrl = "https://openrouter.ai/api/v1";
        if (apiKey == null || apiKey.isEmpty()) throw new RuntimeException("OPENROUTER_API_KEY is not set");

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        // --------------------------
        // Read tool schema (Java 8-safe)
        // --------------------------
        Map<String, Object> readFilePathSchema = new HashMap<String, Object>();
        readFilePathSchema.put("type", "string");
        readFilePathSchema.put("description", "The path to the file to read");

        Map<String, Object> readProperties = new HashMap<String, Object>();
        readProperties.put("file_path", readFilePathSchema);

        List<String> readRequired = Arrays.asList("file_path");

        Map<String, Object> readParameters = new HashMap<String, Object>();
        readParameters.put("type", "object");
        readParameters.put("properties", readProperties);
        readParameters.put("required", readRequired);

        ChatCompletionTool readTool = ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(
                        FunctionDefinition.builder()
                                .name("Read")
                                .description("Read and return the contents of a file")
                                .parameters(JsonValue.from(readParameters))
                                .build()
                )
                .build();

        // --------------------------
        // Write tool schema (Java 8-safe)
        // --------------------------
        Map<String, Object> writeFilePathSchema = new HashMap<String, Object>();
        writeFilePathSchema.put("type", "string");
        writeFilePathSchema.put("description", "The path of the file to write to");

        Map<String, Object> writeContentSchema = new HashMap<String, Object>();
        writeContentSchema.put("type", "string");
        writeContentSchema.put("description", "The content to write to the file");

        Map<String, Object> writeProperties = new HashMap<String, Object>();
        writeProperties.put("file_path", writeFilePathSchema);
        writeProperties.put("content", writeContentSchema);

        List<String> writeRequired = Arrays.asList("file_path", "content");

        Map<String, Object> writeParameters = new HashMap<String, Object>();
        writeParameters.put("type", "object");
        writeParameters.put("properties", writeProperties);
        writeParameters.put("required", writeRequired);

        ChatCompletionTool writeTool = ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(
                        FunctionDefinition.builder()
                                .name("Write")
                                .description("Write content to a file")
                                .parameters(JsonValue.from(writeParameters))
                                .build()
                )
                .build();

        // Persist conversation across iterations
        ChatCompletionCreateParams.Builder convo = ChatCompletionCreateParams.builder()
                .model("anthropic/claude-haiku-4.5")
                .addTool(readTool)
                .addTool(writeTool)
                .addUserMessage(prompt);

        System.err.println("Logs from your program will appear here!");

        final int MAX_ITERS = 25;

        for (int iter = 0; iter < MAX_ITERS; iter++) {
            ChatCompletion response = client.chat().completions().create(convo.build());
            if (response.choices().isEmpty()) throw new RuntimeException("no choices in response");

            ChatCompletionMessage msg = response.choices().get(0).message();

            // Always append assistant message
            convo.addMessage(msg);

            // If there are tool calls, run them and append tool results; DO NOT print/exit yet.
            if (msg.toolCalls().isPresent() && !msg.toolCalls().get().isEmpty()) {
                for (Object toolCallObj : msg.toolCalls().get()) {
                    JsonNode toolCallNode = jsonMapper().valueToTree(toolCallObj);

                    String toolCallId = toolCallNode.path("id").asText(null);
                    String toolName = toolCallNode.path("function").path("name").asText(null);
                    String argsJson = toolCallNode.path("function").path("arguments").asText(null);

                    if (toolCallId == null) throw new RuntimeException("tool call missing id");
                    if (toolName == null) throw new RuntimeException("tool call missing function.name");
                    if (argsJson == null) throw new RuntimeException("tool call missing function.arguments");

                    JsonNode argsNode = jsonMapper().readTree(argsJson);

                    if ("Read".equals(toolName)) {
                        JsonNode filePathNode = argsNode.get("file_path");
                        if (filePathNode == null || filePathNode.isNull()) {
                            throw new RuntimeException("Read tool call missing file_path");
                        }

                        String filePath = filePathNode.asText();
                        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                        .content("OK")

                                .build());

                    } else if ("Write".equals(toolName)) {
                        JsonNode filePathNode = argsNode.get("file_path");
                        JsonNode contentNode = argsNode.get("content");
                        if (filePathNode == null || filePathNode.isNull()) {
                            throw new RuntimeException("Write tool call missing file_path");
                        }
                        if (contentNode == null || contentNode.isNull()) {
                            throw new RuntimeException("Write tool call missing content");
                        }

                        String filePath = filePathNode.asText();
                        String content = contentNode.asText();

                        Path path = Paths.get(filePath);
                        Path parent = path.getParent();
                        if (parent != null) {
                            Files.createDirectories(parent);
                        }

                        // Overwrites if exists, creates if missing (default behavior of Files.write)
                        Files.write(path, content.getBytes(StandardCharsets.UTF_8));

                        String toolResult = "WROTE " + content.getBytes(StandardCharsets.UTF_8).length + " bytes to " + filePath;

                        convo.addMessage(ChatCompletionToolMessageParam.builder()
                                .toolCallId(toolCallId)
                                .content(toolResult)
                                .build());

                    } else {
                        throw new RuntimeException("unsupported tool: " + toolName);
                    }
                }
                continue; // ask the model again with tool outputs in the conversation
            }

            // No tool calls => final response: print and exit (previous behavior).
            System.out.print(msg.content().orElse(""));
            System.out.flush();
            return;
        }

        throw new RuntimeException("agent loop exceeded max iterations (" + MAX_ITERS + ")");
    }
}
