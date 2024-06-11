package com.axonivy.market.controller;

import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

@Log4j2
@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
    private final VersionService service;

    public ProductDetailsController(VersionService service) {
        this.service = service;
    }

    @GetMapping("/{productId}/versions")
    public ResponseEntity<Map<String, List<MavenArtifactModel>>> fecthAllVersionFromProduct(@PathVariable(required = true) String productId,
                                                                                            @RequestParam(required = false, value = "showDevVersion") Boolean isDevVersionsDisplayed,
                                                                                            @RequestParam(required = false, value = "designerVersion") String designerVersion) throws IOException {
        log.warn("controller layer");
        log.warn(productId);
        log.warn(isDevVersionsDisplayed);
        log.warn(designerVersion);

        return new ResponseEntity<>(service.getArtifactsAndVersionToDisplay(productId, isDevVersionsDisplayed, designerVersion), HttpStatus.OK);
    }

    @GetMapping("/{key}")
    public Object findProduct(@PathVariable("key") String key,
                              @RequestParam(name = "type", required = false) String type) {
        return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
    }
}
