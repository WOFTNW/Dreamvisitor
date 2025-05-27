package io.github.stonley890.dreamvisitor.data.type;

import java.time.OffsetDateTime;

/**
 * Represents an alternate Discord account linked to a main account
 */
public class Alt {
  private String id;
  private String collectionId;
  private String collectionName;

  private String parent; // Parent (main) user record ID
  private String discord_name;
  private String discord_id; // Discord snowflake ID as string
  private Long snowflakeId; // Cached numeric version of discord_id

  private OffsetDateTime created;
  private OffsetDateTime updated;

  // Cached related objects (not stored in PocketBase directly)
  private transient DVUser cachedParent;

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

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getDiscord_name() {
    return discord_name;
  }

  public void setDiscord_name(String discord_name) {
    this.discord_name = discord_name;
  }

  public String getDiscord_id() {
    return discord_id;
  }

  public void setDiscord_id(String discord_id) {
    this.discord_id = discord_id;
    // Update snowflakeId when discord_id is set
    if (discord_id != null) {
      try {
        this.snowflakeId = Long.parseLong(discord_id);
      } catch (NumberFormatException e) {
        this.snowflakeId = null;
      }
    } else {
      this.snowflakeId = null;
    }
  }

  public Long getSnowflakeId() {
    return snowflakeId;
  }

  public void setSnowflakeId(Long snowflakeId) {
    this.snowflakeId = snowflakeId;
    if (snowflakeId != null) {
      this.discord_id = snowflakeId.toString();
    }
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

  public DVUser getCachedParent() {
    return cachedParent;
  }

  public void setCachedParent(DVUser cachedParent) {
    this.cachedParent = cachedParent;
  }
}
