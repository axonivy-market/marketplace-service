package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(PRODUCT)
public class ProductController {

  private final ProductService service;
  private final ProductModelAssembler assembler;
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  public ProductController(ProductService service, ProductModelAssembler assembler,
      PagedResourcesAssembler<Product> pagedResourcesAssembler) {
    this.service = service;
    this.assembler = assembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @Operation(summary = "Find all products by type", description = "Be default system will finds product by type as 'all'")
  @GetMapping()
  public ResponseEntity<PagedModel<ProductModel>> findProducts(@RequestParam(required = false) String type,
      @RequestParam(required = false) String keyword, Pageable pageable) {
    var results = service.findProducts(type, keyword, pageable);
    var pageResources = pagedResourcesAssembler.toModel(results, assembler);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<ProductModel>> generateEmptyPagedModel() {
    PagedModel<ProductModel> emptyPagedModel = (PagedModel<ProductModel>) pagedResourcesAssembler
        .toEmptyModel(Page.empty(), ProductModel.class);
    return new ResponseEntity<>(emptyPagedModel, HttpStatus.OK);
  }

  @Operation(summary = "increase installation count by 1", description = "increase installation count by 1")
  @PostMapping("/installationcount/{key}")
  public ResponseEntity<Integer> syncInstallationCount(@PathVariable String key) {
    int result = service.updateInstallationCountForProduct(key);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
