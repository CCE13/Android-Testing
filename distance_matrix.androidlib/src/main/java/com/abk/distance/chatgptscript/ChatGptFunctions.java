package com.abk.distance.chatgptscript;

import com.abk.distance.chatgptscript.chatgptrequirements.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ChatGptFunctions {

    public List<ChatMessage> messagesPrompt = new ArrayList<>();
    public OpenAiService service = new OpenAiService("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", Duration.ofSeconds(600));

    public ChatMessage responseMessage;


    public ChatGptFunctions(){
        String systemMessageContent = "The assistant provides recommendations for improvement based on the analysis of the run.";
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessageContent);
        messagesPrompt.add(systemMessage);
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "analyse this run: Minutes: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26 | Steps: 112, 124, 123, 134, 151, 164, 171, 199, 189, 179, 175, 150, 141, 123, 122, 109, 102, 122, 136, 128, 145, 159, 159, 176, 172, 182 | Cadence: 154, 155, 165, 167, 175, 171, 191, 197, 190, 192, 183, 178, 168, 149, 156, 153, 137, 159, 163, 165, 174, 175, 168, 175, 182, 186 | Stride_Length: 1.23, 1.66, 1.86, 1.86, 2.2, 2.25, 2.54, 2.66, 2.63, 2.61, 2.34, 1.92, 2.06, 1.81, 1.4, 1.32, 1.5, 1.66, 1.68, 1.7, 1.84, 2.14, 2.36, 2.44, 2.39, 2.57 | Pace: 3.98, 4.32, 4.52, 5.04, 5.49, 5.78, 6.31, 6.45, 6.43, 6.22, 5.54, 5.21, 4.85, 4.55, 4.01, 3.89, 3.63, 3.94, 4.43, 4.54, 4.98, 5.32, 5.74, 5.87, 6.31, 6.7");
        messagesPrompt.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("ft:gpt-3.5-turbo-0613:personal:runanalyserv0-1:83H9ckFq")
                .messages(messagesPrompt)
                .maxTokens(500)
                .build();

        responseMessage = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();


    }



}
