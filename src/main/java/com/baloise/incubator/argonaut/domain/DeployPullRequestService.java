package com.baloise.incubator.argonaut.domain;

public interface DeployPullRequestService {

    void deploy(PullRequest pullRequest, boolean isProd);
}
