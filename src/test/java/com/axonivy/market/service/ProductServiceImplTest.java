package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {
  @InjectMocks
  private ProductServiceImpl productService;

  @Mock
  private ProductRepository productRepository;

  @Captor
  ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);

  @Mock
  private ObjectMapper mapper;

  @Test
  public void testUpdateInstallationCount() {
    // prepare
    Mockito.when(productRepository.findById("google-maps-connector")).thenReturn(Optional.of(mockProduct()));

    // exercise
    productService.updateInstallationCountForProduct("google-maps-connector");

    // Verify
    verify(productRepository).save(argumentCaptor.capture());
    int updatedInstallationCount = argumentCaptor.getValue().getInstallationCount();

    assertEquals(1, updatedInstallationCount);
    verify(productRepository, times(1)).findById(Mockito.anyString());
    verify(productRepository, times(1)).save(Mockito.any());
  }

  @Test
  void testSyncInstallationCountWithProduct() throws Exception {
    // Mock data
    ReflectionTestUtils.setField(productService, "installationCountPath", "path/to/installationCount.json");
    Product product = mockProduct();
    product.setSynchronizedInstallationCount(false);
    Mockito.when(productRepository.findById("google-maps-connector")).thenReturn(Optional.of(product));
    Mockito.when(productRepository.save(any())).thenReturn(product);
    // Mock the behavior of Files.readString and ObjectMapper.readValue
    String installationCounts = "{\"google-maps-connector\": 10}";
    mockStatic(Files.class);
    when(Files.readString(Paths.get("path/to/installationCount.json"))).thenReturn(installationCounts);

    // Call the method
    int result = productService.updateInstallationCountForProduct("google-maps-connector");

    // Verify the results
    assertEquals(11, result);
    assertEquals(true, product.getSynchronizedInstallationCount());
    assertTrue(product.getSynchronizedInstallationCount());
  }

  private Product mockProduct() {
    return Product.builder().key("google-maps-connector").name("Google Maps").language("English")
        .synchronizedInstallationCount(true).build();
  }
}
