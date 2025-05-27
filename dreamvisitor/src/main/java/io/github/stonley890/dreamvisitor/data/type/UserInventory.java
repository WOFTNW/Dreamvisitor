package io.github.stonley890.dreamvisitor.data.type;

import java.time.OffsetDateTime;

public class UserInventory {
  private String id;
  private String collectionId;
  private String collectionName;

  private String user; // Relation record ID
  private String item; // Relation record ID
  private Integer quantity;

  private OffsetDateTime created;
  private OffsetDateTime updated;

  // Cached related objects (not stored in PocketBase directly)
  private transient DVUser cachedUser;
  private transient Item cachedItem;

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

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getItem() {
    return item;
  }

  public void setItem(String item) {
    this.item = item;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
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

  public Item getCachedItem() {
    return cachedItem;
  }

  public void setCachedItem(Item cachedItem) {
    this.cachedItem = cachedItem;
  }
}
