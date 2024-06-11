package com.axonivy.market.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.service.ProductService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductController.class)
public class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProductService productService;

  @MockBean
  private ProductModelAssembler assembler;

  @MockBean
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @Test
  public void testSyncInstallationCount() throws Exception {
    // prepare
    when(productService.updateInstallationCountForProduct("google-maps-connector")).thenReturn(1);

    // exercise
    mockMvc
        .perform(MockMvcRequestBuilders.post("/api/product/installationcount/google-maps-connector")
            .contentType(MediaType.APPLICATION_JSON).header("X-Requested-By", "ivy"))
        .andExpect(status().isOk()).andExpect(content().string(String.valueOf(1)));

    // Verify the interaction with the mock
    verify(productService).updateInstallationCountForProduct("google-maps-connector");
  }

}
