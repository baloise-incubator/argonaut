package com.baloise.incubator.argonaut.infrastructure.git;

import com.baloise.incubator.argonaut.domain.DeployPullRequestService;
import com.baloise.incubator.argonaut.domain.PRCommentBranchNameService;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class GitDeployPullRequestService implements DeployPullRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitDeployPullRequestService.class);

    @Value("${argonaut.githubtoken}")
    private String apiToken;

    @Value("${argonaut.tempfolder}")
    private FileSystemResource tempFolder;

    @Autowired
    private PRCommentBranchNameService prCommentBranchNameService;

    @Override
    public void deploy(String url, String fullName, String applicationName, String newImageTag, String commentApiUrl) {
        LOGGER.info("Deploying with url: {}, fullOrgRepoName: {}, appName: {}, newImageTag: {}, commentApiUrl: {}", url, fullName, applicationName, newImageTag, commentApiUrl);
        String sanitizedBranchName = prCommentBranchNameService.getBranchNameForPrCommentUrl(commentApiUrl).replace("/", "-");
        LOGGER.info("Sanitized branchname: {}", sanitizedBranchName);
        File tempRootDirectory = tempFolder.getFile();
        boolean succeeded = tempRootDirectory.mkdir();
        LOGGER.info("temp folder created: {}", succeeded);
        File uuidWorkingDir = new File(tempRootDirectory, UUID.randomUUID().toString());
        LOGGER.info("Using subfolder with UUID: {}", uuidWorkingDir.getName());
        String branchSpecificFolderName = applicationName;
        File masterFolder = new File(uuidWorkingDir, branchSpecificFolderName);
        File branchSpecificFolder = masterFolder;

        try {
            Git git = Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(uuidWorkingDir)
                    .call();
            if (!"master".equals(sanitizedBranchName)) {
                branchSpecificFolderName += "-" + sanitizedBranchName;
                branchSpecificFolder = new File(uuidWorkingDir, branchSpecificFolderName);
                FileUtils.copyDirectory(masterFolder, branchSpecificFolder);
            }
            for (File subTempFolderFile : branchSpecificFolder.listFiles()) {
                if ("values.yaml".equals(subTempFolderFile.getName())) {
                    YamlFile yamlFile = new YamlFile(subTempFolderFile);
                    yamlFile.load();
                    LOGGER.info("New image tag: {}", yamlFile.get("backend.image.tag"));
                    yamlFile.set("backend.image.tag", newImageTag);
                    yamlFile.save();
                }
                if ("Chart.yaml".equals(subTempFolderFile.getName())) {
                    YamlFile yamlFile = new YamlFile(subTempFolderFile);
                    yamlFile.load();
                    yamlFile.set("name", branchSpecificFolderName);
                    yamlFile.save();
                }
            }
            git
                    .add()
                    .addFilepattern(".")
                    .call();
            git
                    .commit()
                    .setMessage("Redeploy")
                    .call();
            git
                    .push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("ttt-travis-bot", apiToken))
                    .call();
            LOGGER.info("Pushed changes.");
        } catch (
                GitAPIException | InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        // TODO: Activate later
/*        try {
            FileUtils.deleteDirectory(uuidWorkingDir);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}