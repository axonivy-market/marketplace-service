package com.axonivy.market.github.service;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHTag;

import java.io.IOException;
import java.util.List;

public interface GHAxonIvyProductRepoService {

    GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion);

    List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException;
}