package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.PRODUCT;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(PRODUCT)
public class Product implements Serializable {

  private static final long serialVersionUID = -8770801877877277258L;
  @Id
  private String key;
  private String marketDirectory;
  private String name;
  private String version;
  private String shortDescription;
  private String logoUrl;
  private Boolean listed;
  private String type;
  private List<String> tags;
  private String vendor;
  private String vendorImage;
  private String vendorUrl;
  private String platformReview;
  private String cost;
  private String repositoryName;
  private String sourceUrl;
  private String statusBadgeUrl;
  private String language;
  private String industry;
  private String compatibility;
  private Boolean validate;
  private Boolean contactUs;
  private int installationCount;
  private Date newestPublishDate;
  private String newestReleaseVersion;

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(key);
    return builder.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    EqualsBuilder builder = new EqualsBuilder();
    builder.append(key, ((Product) obj).getKey());
    return builder.isEquals();
  }

}