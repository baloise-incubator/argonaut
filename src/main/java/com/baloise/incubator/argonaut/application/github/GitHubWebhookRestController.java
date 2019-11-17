package com.baloise.incubator.argonaut.application.github;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
public class GitHubWebhookRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubWebhookRestController.class);

    @PostMapping(path = "webhook/github")
    public void handleGitHubWebhookEvent(@RequestBody String data, @RequestHeader("X-GitHub-Event") String githubEvent) {
        Optional<GitHubEventType> gitHubEventType = GitHubEventType.fromEventName(githubEvent);
        if (gitHubEventType.isPresent()) {
            JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
            System.out.println(LocalDateTime.now() + " " + jsonObject.get("action"));
            switch (gitHubEventType.get()) {
                case PULL_REQUEST: {

                }
                case PUSH: {

                }
                default: {
                    LOGGER.info("Unhandled GitHub Event Type: {}", githubEvent);
                }
            }
        } else {
            LOGGER.info("Unhandled GitHub Event Type: {}", githubEvent);
        }
    }
}
