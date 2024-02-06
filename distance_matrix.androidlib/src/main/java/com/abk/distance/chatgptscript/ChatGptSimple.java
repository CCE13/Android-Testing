package com.abk.distance.chatgptscript;

import android.os.Build;

import com.abk.distance.chatgptscript.chatgptrequirements.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ChatGptSimple {

    public List<com.theokanning.openai.completion.CompletionChoice> choices = new ArrayList<>();
    public ChatGptSimple(OpenAiService service){

        RunGPT(service);


    }

    public void RunGPT(OpenAiService service) {
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("analyse this run: Minutes: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26 | Steps: 148, 108, 97, 140, 89, 96, 194, 96, 105, 123, 88, 101, 153, 92, 90, 168, 88, 91, 112, 89, 97, 145, 103, 97, 117, 103 | Cadence: 146, 124, 122, 170, 129, 137, 152, 133, 122, 175, 121, 139, 181, 142, 142, 187, 124, 127, 146, 139, 141, 180, 129, 134, 171, 130 | Stride_Length: 2.19, 1.14, 1.33, 1.94, 1.38, 1.33, 1.99, 1.14, 1.35, 1.89, 1.08, 1.36, 2.11, 1.09, 1.39, 1.74, 1.28, 1.1, 1.95, 1.26, 1.1, 1.99, 1.26, 1.38, 2.44, 1.1 | Pace: 5.27, 7.35, 6.59, 5.08, 6.65, 7.29, 3.88, 6.81, 6.93, 4.18, 6.92, 6.59, 4.04, 6.91, 6.88, 5.29, 7.14, 6.64, 6.34, 6.97, 6.85, 5.11, 6.8, 7.36, 4.15, 7.16 ")
                .model("gpt-3.5-turbo")
                .build();

        choices = service.createCompletion(completionRequest).getChoices();
    }
}
