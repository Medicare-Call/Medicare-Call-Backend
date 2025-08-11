package com.example.medicare_call.service.carecall.prompt;

import com.example.medicare_call.global.enums.CallType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CallPromptGeneratorFactory {
    private final FirstCallPromptGenerator firstStrategy;
    private final SecondCallPromptGenerator secondStrategy;
    private final ThirdCallPromptGenerator thirdStrategy;

    public CallPromptGenerator getGenerator(CallType callType) {
        return switch (callType) {
            case FIRST -> firstStrategy;
            case SECOND -> secondStrategy;
            case THIRD -> thirdStrategy;
        };
    }
}
