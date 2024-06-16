package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.ReadmeModel;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.axonivy.market.github.service.GithubService;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl implements GHAxonIvyProductRepoService {
    private GHOrganization organization;
    private final GithubService githubService;
    public static final String IMAGES_FOLDER = "images";
    public static final String PRODUCT_FOLDER_SUFFIX = "-product";
    public static final String README_FILE = "README.md";

    public GHAxonIvyProductRepoServiceImpl(GithubService githubService) {
        this.githubService = githubService;
    }

    @Override
    public GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion) {
        try {
            return getOrganization().getRepository(repoName).getFileContent(filePath, tagVersion);
        } catch (IOException e) {
            log.error("Cannot Get Content From File Directory", e);
            return null;
        }
    }

    private GHOrganization getOrganization() throws IOException {
        if (organization == null) {
            organization = githubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
        }
        return organization;
    }

    @Override
    public List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException {
        return getOrganization().getRepository(repoName).listTags().toList();
    }

    @Override
    public ReadmeModel getReadmeContentsFromTag(String repoName, String tag) {
        String readmeContents = "";
        try {
            List<GHContent> contents = getRepoContents(repoName, tag);
            Optional<GHContent> readmeFile = contents.stream()
                    .filter(GHContent::isFile)
                    .filter(content -> README_FILE.equals(content.getName()))
                    .findFirst();
            if (readmeFile.isPresent()) {
                readmeContents = new String(readmeFile.get().read().readAllBytes());
                if (containsImageDirectives(readmeContents)) {
                    readmeContents = updateImagesWithDownloadUrl(contents, readmeContents);
                    getExtractedPartsOfReadme(readmeContents);
                } else {
                    getExtractedPartsOfReadme(readmeContents);
                }
            }
        } catch (Exception e) {
            log.error("Cannot get README file's content {}", e);
        }
        return getExtractedPartsOfReadme(readmeContents);
    }

    private String updateImagesWithDownloadUrl(List<GHContent> contents, String readmeContents) throws IOException {
        Map<String, String> imageUrls = new HashMap<>();
        Optional<GHContent> productImage = contents.stream()
                .filter(GHContent::isFile)
                .filter(content -> content.getName().matches(".+\\.(jpeg|jpg|png)"))
                .findAny();
        if (productImage.isPresent()) {
            imageUrls.put(productImage.get().getName(), productImage.get().getDownloadUrl());
        } else {
            Optional<GHContent> imageFolder = contents.stream()
                    .filter(GHContent::isDirectory)
                    .filter(content -> IMAGES_FOLDER.equals(content.getName()))
                    .findFirst();
            if (imageFolder.isPresent()) {
                for (GHContent imageContent : imageFolder.get().listDirectoryContent()) {
                    imageUrls.put(imageContent.getName(), imageContent.getDownloadUrl());
                }
            }
        }
        for (Map.Entry<String, String> entry : imageUrls.entrySet()) {
            readmeContents = readmeContents.replaceAll("images/" + Pattern.quote(entry.getKey()), entry.getValue());
        }
        return readmeContents;
    }

    private ReadmeModel getExtractedPartsOfReadme(String readmeContents) {
        String description = "";
        String setup = "";
        String demo = "";
        String[] parts = readmeContents.split("(?i)## Demo|## Setup");
        if (parts.length > 0) {
            description = removeFirstLine(parts[0].trim());
        }
        if (readmeContents.contains("## Demo") && readmeContents.contains("## Setup")) {
            if (readmeContents.indexOf("## Demo") < readmeContents.indexOf("## Setup")) {
                if (parts.length >= 2) {
                    demo = parts[1].trim();
                }
                if (parts.length >= 3) {
                    setup = parts[2].trim();
                }
            } else {
                if (parts.length >= 2) {
                    setup = parts[1].trim();
                }
                if (parts.length >= 3) {
                    demo = parts[2].trim();
                }
            }
        } else if (readmeContents.contains("## Demo")) {
            if (parts.length >= 2) {
                demo = parts[1].trim();
            }
        } else if (readmeContents.contains("## Setup")) {
            if (parts.length >= 2) {
                setup = parts[1].trim();
            }
        }
        return new ReadmeModel(description, setup, demo);
    }

    private List<GHContent> getRepoContents(String repoName, String tag) throws IOException {
        return githubService.getRepository(repoName)
                .getDirectoryContent("/", tag)
                .stream()
                .filter(GHContent::isDirectory)
                .filter(content -> content.getName().endsWith(PRODUCT_FOLDER_SUFFIX))
                .flatMap(content -> {
                    try {
                        return content.listDirectoryContent().toList().stream();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    private boolean containsImageDirectives(String readmeContents) {
        Pattern pattern = Pattern.compile("images/(.*?)");
        Matcher matcher = pattern.matcher(readmeContents);
        return matcher.find();
    }

    private String removeFirstLine(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        int index = text.indexOf("\n");
        if (index != -1) {
            return text.substring(index + 1).trim();
        } else {
            return "";
        }
    }
}
