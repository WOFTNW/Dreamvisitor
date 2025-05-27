package io.github.stonley890.dreamvisitor.data.type;

import java.time.OffsetDateTime;

public class Infraction {
  private String id;
  private String collectionId;
  private String collectionName;

  private String reason;
  private Boolean send_warning;
  private Boolean expired;
  private Integer value;
  private String user; // Relation record ID

  private OffsetDateTime created;
  private OffsetDateTime updated;

  // Cached related object (not stored in PocketBase directly)
  private transient DVUser cachedUser;

  // Getters and setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCollectionId() {
    return collectionId;
  }

  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Boolean getSend_warning() {
    return send_warning;
  }

  public void setSend_warning(Boolean send_warning) {
    this.send_warning = send_warning;
  }

  public Boolean getExpired() {
    return expired;
  }

  public void setExpired(Boolean expired) {
    this.expired = expired;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public OffsetDateTime getCreated() {
    return created;
  }

  public void setCreated(OffsetDateTime created) {
    this.created = created;
  }

  public OffsetDateTime getUpdated() {
    return updated;
  }

  public void setUpdated(OffsetDateTime updated) {
    this.updated = updated;
  }

  public DVUser getCachedUser() {
    return cachedUser;
  }

  public void setCachedUser(DVUser cachedUser) {
    this.cachedUser = cachedUser;
  }
}
