package com.axonivy.market.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {
  @InjectMocks
  private ProductServiceImpl productService;

  @Mock
  private ProductRepository productRepository;

  @Captor
  ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);

  @Test
  public void testUpdateInstallationCount() {
    // prepare
    Mockito.when(productRepository.findById("google-maps-connector")).thenReturn(Optional.of(mockProduct()));

    // exercise
    productService.updateInstallationCountForProduct("google-maps-connector");

    // Verify
    verify(productRepository).save(argumentCaptor.capture());
    int updatedInstallationCount = argumentCaptor.getValue().getInstallationCount();

    Assertions.assertEquals(11, updatedInstallationCount);
    verify(productRepository, times(1)).findById(Mockito.anyString());
    verify(productRepository, times(1)).save(Mockito.any());
  }

  private Product mockProduct() {
    return Product.builder().key("google-maps-connector").name("Google Maps").installationCount(10).language("English")
        .build();
  }
}
