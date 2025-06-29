package io.github.stonley890.dreamvisitor.data.repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Alt account data operations
 */
public interface AltRepository {
  /**
   * Find an alt by its PocketBase ID
   *
   * @param id PocketBase record ID
   * @return Optional containing the alt if found
   */
  Optional<Alt> findById(String id);

  /**
   * Find all alts linked to a parent user
   *
   * @param parentId Parent user's PocketBase ID
   * @return List of alt accounts for this parent
   */
  List<Alt> findByParentId(String parentId);

  /**
   * Find an alt by Discord ID
   *
   * @param discordId Discord ID (string)
   * @return Optional containing the alt if found
   */
  Optional<Alt> findByDiscordId(String discordId);

  /**
   * Find an alt by Discord Snowflake ID
   *
   * @param snowflakeId Discord Snowflake ID (numeric)
   * @return Optional containing the alt if found
   */
  Optional<Alt> findBySnowflakeId(Long snowflakeId);

  /**
   * Find an alt by Discord username
   *
   * @param discordName Discord username
   * @return Optional containing the alt if found
   */
  Optional<Alt> findByDiscordName(String discordName);

  /**
   * Get all alts
   *
   * @return List of all alt accounts
   */
  List<Alt> findAll();

  /**
   * Save an alt (create or update)
   *
   * @param alt Alt to save
   * @return Saved alt
   */
  Alt save(Alt alt);

  /**
   * Delete an alt
   *
   * @param alt Alt to delete
   */
  void delete(Alt alt);

  /**
   * Delete an alt by ID
   *
   * @param id PocketBase ID of alt to delete
   */
  void deleteById(String id);

  /**
   * Get all alts matching a filter expression
   *
   * @param filter PocketBase filter expression
   * @return List of matching alts
   */
  List<Alt> getAllWhere(String filter);
}
