package com.baloise.incubator.argonaut.application.github;

import java.util.Arrays;
import java.util.Optional;

public enum GitHubPullRequestEventAction {

    OPENED("opened"),
    CLOSED("closed");

    private String action;

    GitHubPullRequestEventAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public static Optional<GitHubPullRequestEventAction> fromActionName(String actionName) {
        return Arrays.stream(values())
                .filter(val -> val.getAction().equals(actionName))
                .findFirst();
    }
}