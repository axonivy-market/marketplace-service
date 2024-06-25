package com.axonivy.market.service;

import com.axonivy.market.model.MavenArtifactVersionModel;

import java.util.List;

public interface VersionService {

  List<String> getVersionsToDisplay(Boolean isShowDevVersion, String designerVersion);

  List<String> getVersionsFromArtifactDetails(String repoUrl, String groupId, String artifactID);

  String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID);

  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion, String designerVersion);
}
