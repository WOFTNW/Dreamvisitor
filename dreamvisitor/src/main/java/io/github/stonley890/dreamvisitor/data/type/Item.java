package io.github.stonley890.dreamvisitor.data.type;

import java.time.OffsetDateTime;

public class Item {
  private String id;
  private String collectionId;
  private String collectionName;

  private String name;
  private String description;
  private Double price;
  private Double sale_percent;
  private Integer quantity;
  private Boolean gifting_enabled;
  private Boolean enabled;
  private Integer max_allowed;
  private Boolean use_disabled;
  private Boolean use_on_purchase;
  private String on_use_groups_add; // JSON string
  private String on_use_groups_remove; // JSON string

  // JSON array of Discord role IDs to add when item is used
  // Example format: ["123456789012345678", "234567890123456789"]
  // These must be valid Discord role IDs from the server
  private String on_use_roles_add;

  // JSON array of Discord role IDs to remove when item is used
  // Example format: ["123456789012345678", "234567890123456789"]
  // These must be valid Discord role IDs from the server
  private String on_use_roles_remove;

  private String on_use_console_commands; // JSON string

  private OffsetDateTime created;
  private OffsetDateTime updated;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public Double getSale_percent() {
    return sale_percent;
  }

  public void setSale_percent(Double sale_percent) {
    this.sale_percent = sale_percent;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Boolean getGifting_enabled() {
    return gifting_enabled;
  }

  public void setGifting_enabled(Boolean gifting_enabled) {
    this.gifting_enabled = gifting_enabled;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Integer getMax_allowed() {
    return max_allowed;
  }

  public void setMax_allowed(Integer max_allowed) {
    this.max_allowed = max_allowed;
  }

  public Boolean getUse_disabled() {
    return use_disabled;
  }

  public void setUse_disabled(Boolean use_disabled) {
    this.use_disabled = use_disabled;
  }

  public Boolean getUse_on_purchase() {
    return use_on_purchase;
  }

  public void setUse_on_purchase(Boolean use_on_purchase) {
    this.use_on_purchase = use_on_purchase;
  }

  public String getOn_use_groups_add() {
    return on_use_groups_add;
  }

  public void setOn_use_groups_add(String on_use_groups_add) {
    this.on_use_groups_add = on_use_groups_add;
  }

  public String getOn_use_groups_remove() {
    return on_use_groups_remove;
  }

  public void setOn_use_groups_remove(String on_use_groups_remove) {
    this.on_use_groups_remove = on_use_groups_remove;
  }

  public String getOn_use_roles_add() {
    return on_use_roles_add;
  }

  public void setOn_use_roles_add(String on_use_roles_add) {
    this.on_use_roles_add = on_use_roles_add;
  }

  public String getOn_use_roles_remove() {
    return on_use_roles_remove;
  }

  public void setOn_use_roles_remove(String on_use_roles_remove) {
    this.on_use_roles_remove = on_use_roles_remove;
  }

  public String getOn_use_console_commands() {
    return on_use_console_commands;
  }

  public void setOn_use_console_commands(String on_use_console_commands) {
    this.on_use_console_commands = on_use_console_commands;
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
}
