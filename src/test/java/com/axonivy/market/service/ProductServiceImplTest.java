package com.axonivy.market.service;

import static com.axonivy.market.constants.CommonConstants.LOGO_FILE;
import static com.axonivy.market.constants.CommonConstants.META_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.axonivy.market.model.ReadmeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GithubService;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  private static final String SAMPLE_PRODUCT_NAME = "amazon-comprehend";
  private static final long LAST_CHANGE_TIME = 1718096290000l;
  private static final Pageable PAGEABLE = PageRequest.of(0, 20,
          Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
  private String keyword;
  private Page<Product> mockResultReturn;

  @Mock
  PagedIterable<GHTag> listTags;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private GHAxonIvyMarketRepoService marketRepoService;

  @Mock
  private GithubRepoMetaRepository repoMetaRepository;

  @Mock
  private GithubService githubService;

  @Mock
  private GHRepository mockRepository;

  @InjectMocks
  private ProductServiceImpl productService;

  @BeforeEach
  public void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testFindProducts() throws IOException {
    // Start testing by All
    when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);
    mockMarketRepoMetaStatus();
    // Executes
    var result = productService.findProducts(FilterType.ALL.getOption(), keyword, PAGEABLE);
    assertEquals(mockResultReturn, result);

    // Start testing by Other
    // Executes
    result = productService.findProducts(FilterType.DEMOS.getOption(), keyword, PAGEABLE);
    assertEquals(0, result.getSize());
  }

  @Test
  void testFindProductsAsUpdateMetaJSONFromGitHub() throws IOException {
    // Start testing by adding new meta
    mockMarketRepoMetaStatus();
    var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

    var connectorCode = FilterType.CONNECTORS.getCode();
    var connectorProducts = mockResultReturn.filter(product -> product.getType().equals(connectorCode)).toList();
    var mockPagedResult = new PageImpl<Product>(connectorProducts);
    when(productRepository.findByType(connectorCode, PAGEABLE)).thenReturn(mockPagedResult);

    var mockGithubFile = new GitHubFile();
    mockGithubFile.setFileName(META_FILE);
    mockGithubFile.setType(FileType.META);
    mockGithubFile.setStatus(FileStatus.ADDED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
    var mockGHContent = mockGHContentAsMetaJSON();
    when(githubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);

    // Executes
    var result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
    assertEquals(mockPagedResult, result);

    // Start testing by deleting new meta
    mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    mockGithubFile.setStatus(FileStatus.REMOVED);
    // Executes
    result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
    assertEquals(mockPagedResult, result);
  }

  @Test
  void testFindProductsAsUpdateLogoFromGitHub() throws IOException {
    // Start testing by adding new logo
    mockMarketRepoMetaStatus();
    var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

    var connectorCode = FilterType.CONNECTORS.getCode();
    var connectorProducts = mockResultReturn.filter(product -> product.getType().equals(connectorCode)).toList();
    var mockPagedResult = new PageImpl<Product>(connectorProducts);
    when(productRepository.findByType(connectorCode, PAGEABLE)).thenReturn(mockPagedResult);
    var mockGithubFile = mock(GitHubFile.class);
    mockGithubFile = new GitHubFile();
    mockGithubFile.setFileName(LOGO_FILE);
    mockGithubFile.setType(FileType.LOGO);
    mockGithubFile.setStatus(FileStatus.ADDED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
    var mockGHContent = mockGHContentAsMetaJSON();
    when(githubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);

    // Executes
    var result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
    assertEquals(mockPagedResult, result);

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGithubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
    when(githubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);
    when(productRepository.findByLogoUrl(any())).thenReturn(new Product());

    // Executes
    result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
    assertEquals(mockPagedResult, result);
  }
//
//  @Test
//  void testFindAllProductsFirstTime() throws IOException {
//    String allType = FilterType.ALL.getOption();
//    when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);
//
//    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
//    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
//    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);
//
//    var mockContent = mockGHContentAsMetaJSON();
//    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
//    when(mockContent.read()).thenReturn(inputStream);
//
//    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
//    mockGHContentMap.put(SAMPLE_PRODUCT_NAME, List.of(mockContent));
//    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);
//
//    // Executes
//    var result = productService.findProducts(allType, keyword, PAGEABLE);
//    assertEquals(result, mockResultReturn);
//    verify(productRepository).findAll(any(Pageable.class));
//  }
//  @Test
//  void testFindAllProductsFirstTime1() throws IOException {
//    String allType = FilterType.ALL.getOption();
//    when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);
//
//    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
//    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
//    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);
//
//    var mockContent = mockGHContentAsMetaJSON();
//    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
//    when(mockContent.read()).thenReturn(inputStream);
//
//    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
//    mockGHContentMap.put(SAMPLE_PRODUCT_NAME, List.of(mockContent));
//    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);
//
//    var mockTag = mock(GHTag.class);
//    when(mockTag.getName()).thenReturn("v1.0.0");
//    when(listTags.toList()).thenReturn(List.of(mockTag));
//    when(marketRepoService.getRepository()).thenReturn(mockRepository);
//
//    // Executes
//    var result = productService.findProducts(allType, keyword, PAGEABLE);
//    assertEquals(result, mockResultReturn);
//    verify(productRepository).findAll(any(Pageable.class));
//    verify(productRepository).saveAll(anyList());
//
//    // Verify compatibility extraction
//    var savedProducts = ((PageImpl<Product>) result).getContent();
//    for (Product product : savedProducts) {
//      assertEquals("1.0+", product.getCompatibility());
//    }
//  }

  @Test
  void testSearchProducts() {
    var simplePageable = PageRequest.of(0, 20);
    String type = FilterType.ALL.getOption();
    keyword = "on";
    when(productRepository.searchByNameOrShortDescriptionRegex(keyword, simplePageable)).thenReturn(mockResultReturn);

    var result = productService.findProducts(type, keyword, simplePageable);
    assertEquals(result, mockResultReturn);
    verify(productRepository).searchByNameOrShortDescriptionRegex(keyword, simplePageable);
  }

  @Test
  void extractCompatibilityFromOldestTag_shouldNotChangeCompatIfAlreadySet() {
    Product product = new Product();
    product.setCompatibility("1.0");
    productService.extractCompatibilityFromOldestTag(product);
    assertEquals("1.0+", product.getCompatibility());
  }

  @Test
  void extractCompatibilityFromOldestTag_shouldSetCompatibilityBasedOnOldestTag() throws IOException {
    Product product = new Product();

//    var mockTag = mock(GHTag.class);
//    when(mockTag.getName()).thenReturn(DUMMY_TAG);
//    when(listTags.toList()).thenReturn(List.of(mockTag));
//    when(ghRepository.listTags()).thenReturn(listTags);
//    var result = axonivyProductRepoServiceImpl.getAllTagsFromRepoName("");

//    GHTag oldestTag = mock(GHTag.class);
//    when(oldestTag.getName()).thenReturn("v8");
//    when(githubService.getRepository("Docker")).thenReturn(repo);
//    when(repo.listTags()).thenReturn(Arrays.asList(oldestTag));
//    when(oldestTag.getName()).thenReturn("v8");
//    product.setRepositoryName("repoName");
//    service.extractCompatibilityFromOldestTag(product);
//    assertEquals("8.0+", product.getCompatibility());
  }
//
//  @Test
//  void extractCompatibilityFromOldestTag_noTagsInRepo() throws IOException {
//    Product product = new Product();
//    GHRepository repo = mock(GHRepository.class);
//    when(githubService.getRepository("repoName")).thenReturn(repo);
//    when(repo.listTags()).thenReturn(Collections.emptyList());
//    product.setRepositoryName("repoName");
//    service.extractCompatibilityFromOldestTag(product);
//    assertNull(product.getCompatibility());
//  }
//
//  @Test
//  void extractCompatibilityFromOldestTag_repositoryNotFound() throws IOException {
//    Product product = new Product();
//    product.setRepositoryName("repoName");
//    when(githubService.getRepository("repoName")).thenThrow(new IOException());
//    service.extractCompatibilityFromOldestTag(product);
//    assertNull(product.getCompatibility());
//  }
//
//  @Test
//  void extractCompatibilityFromOldestTag_nonNumericTag() throws IOException {
//    Product product = new Product();
//    GHRepository repo = mock(GHRepository.class);
//    GHTag oldestTag = mock(GHTag.class);
//    when(githubService.getRepository("repoName")).thenReturn(repo);
//    when(repo.listTags()).thenReturn(Arrays.asList(oldestTag));
//    when(oldestTag.getName()).thenReturn("release_11.1_special");
//    product.setRepositoryName("repoName");
//
//  }
@Test
public void testFetchProductDetail() {
  Page<Product> mockProductsPage = createPageProductsMock();
  Product mockProduct = mockProductsPage.getContent().get(0);
  // Given
  String id = "amazon-comprehend-connector";
  String type = "connector";
  when(productRepository.findByIdAndType(id, type)).thenReturn(mockProduct);

  Product result = productService.fetchProductDetail(id, type);

  assertEquals(mockProduct, result);
  verify(productRepository, times(1)).findByIdAndType(id, type);
}

//  @Test
//  public void testGetReadmeAndProductContentsFromTag() {
//    Page<Product> mockProductsPage = createPageProductsMock();
//    Product mockProduct = mockProductsPage.getContent().get(0);
//    // Given
//    String productId = "amazon-comprehend-connector";
//    String tag = "v1.0";
//    ReadmeModel mockReadmeModel = new ReadmeModel();
//    when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
//    when(productService.getReadmeAndProductContentsFromTag(mockProduct.getRepositoryName(), tag)).thenReturn(mockReadmeModel);
//
//    // When
//    ReadmeModel result = productService.getReadmeAndProductContentsFromTag(productId, tag);
//
//    // Then
//    assertEquals(mockReadmeModel, result);
//    verify(productRepository, times(1)).findById(productId);
//    verify(productService, times(1)).getReadmeAndProductContentsFromTag(mockProduct.getRepositoryName(), tag);
//  }
  private Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_NAME);
    mockProduct.setName("Amazon Comprehend");
    mockProduct.setType("connector");
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    mockProduct.setName("Swiss phone directory");
    mockProduct.setType("util");
    mockProducts.add(mockProduct);
    return new PageImpl<>(mockProducts);
  }

  private void mockMarketRepoMetaStatus() {
    var mockMartketRepoMeta = new GithubRepoMeta();
    mockMartketRepoMeta.setRepoURL(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMartketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMartketRepoMeta.setLastChange(LAST_CHANGE_TIME);
    mockMartketRepoMeta.setLastSHA1(SHA1_SAMPLE);
    when(repoMetaRepository.findByRepoName(any())).thenReturn(mockMartketRepoMeta);
  }

  private GHCommit mockGHCommitHasSHA1(String sha1) {
    var mockCommit = mock(GHCommit.class);
    when(mockCommit.getSHA1()).thenReturn(sha1);
    return mockCommit;
  }

  private GHContent mockGHContentAsMetaJSON() {
    var mockGHContent = mock(GHContent.class);
    when(mockGHContent.getName()).thenReturn(META_FILE);
    return mockGHContent;
  }
}