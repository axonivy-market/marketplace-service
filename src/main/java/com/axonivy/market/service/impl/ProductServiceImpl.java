package com.axonivy.market.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository repo;
  private final GHAxonIvyMarketRepoService githubService;
  private final GithubRepoMetaRepository repoMetaRepository;

  public ProductServiceImpl(ProductRepository repo, GHAxonIvyMarketRepoService githubService, GithubRepoMetaRepository repoMetaRepository) {
    this.repo = repo;
    this.githubService = githubService;
    this.repoMetaRepository = repoMetaRepository;
  }

  /**
    Find in DB first, if no call GH API
    TODO When we must refresh data
  **/
  @Override
  public List<Product> fetchAll(String type, String sort, int page, int pageSize) {
    boolean hasChanged = false;
    List<Product> products = new ArrayList<Product>();
    switch (FilterType.of(type)) {
    case ALL -> products.addAll(repo.findAll());
    case CONNECTORS, UTILITIES, SOLUTIONS -> {
      products.addAll(repo.findByType(type));
    }
    default -> throw new IllegalArgumentException("Unexpected value: " + type);
    }

    if (CollectionUtils.isEmpty(products) || !checkGithubLastCommit()) {
      // Find on GH
      products = findProductsFromGithubRepo();
      hasChanged = true;
    }
    // TODO Sync to DB
    if (hasChanged) {
     syncGHDataToDB(products);
    }
    return products;
  }

  private boolean checkGithubLastCommit() {
    // TODO check last commit
    boolean isLastCommitCovered;
    long lastCommitTime = 0l;
    var lastCommit = githubService.getLastCommit();
    if (lastCommit != null) {
      try {
        lastCommitTime = lastCommit.getCommitDate().getTime();
      } catch (IOException e) {
        log.error("Check last commit failed", e);
      }
    }

    var repoMeta = repoMetaRepository.findByRepoName("market");
    if (repoMeta != null && repoMeta.getLastChange() == lastCommitTime) {
      isLastCommitCovered = true;
    } else {
      isLastCommitCovered = false;
      repoMeta = new GithubRepoMeta();
      repoMeta.setRepoName("market");
      repoMeta.setLastChange(lastCommitTime);
      repoMetaRepository.save(repoMeta);
    }
    return isLastCommitCovered;
  }

  private List<Product> findProductsFromGithubRepo() {
    var githubContentMap = githubService.fetchAllMarketItems();
    List<Product> products = new ArrayList<Product>();
    for (var contentKey : githubContentMap.keySet()) {
      Product product = new Product();
      for (var content : githubContentMap.get(contentKey)) {
        ProductFactory.mappingByGHContent(product, content);
      }
      products.add(product);
    }
    return products;
  }

  private void syncGHDataToDB(List<Product> products) {
    // TODO Store to MongoDB
    repo.saveAll(products);
  }

  @Override
  public Product findByKey(String key) {
    // TODO Auto-generated method stub
    return null;
  }

}
