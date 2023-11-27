package com.siryus.swisscon.api.base;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class FailFastExtension implements AfterTestExecutionCallback, ExecutionCondition {
    private String failedTest = null;

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getExecutionException().isPresent()) {
            failedTest = extensionContext.getDisplayName();
        }
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        return failedTest != null ? ConditionEvaluationResult.disabled("Test " + failedTest + " failed") : ConditionEvaluationResult.enabled("");
    }
}
