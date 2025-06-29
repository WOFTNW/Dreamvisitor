package org.woftnw.dreamvisitor.data.repository;



import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.woftnw.dreamvisitor.data.type.DVUser;

/**
 * Repository interface for User data operations
 */
public interface UserRepository {
  /**
   * Find a user by their PocketBase ID
   *
   * @param id PocketBase record ID
   * @return Optional containing the user if found
   */
  Optional<DVUser> findById(String id);

  /**
   * Find a user by their Minecraft UUID
   *
   * @param uuid Minecraft UUID
   * @return Optional containing the user if found
   */
  Optional<DVUser> findByUuid(UUID uuid);

  /**
   * Find a user by their Discord ID
   *
   * @param discordId Discord ID
   * @return Optional containing the user if found
   */
  Optional<DVUser> findByDiscordId(String discordId);

  /**
   * Find a user by their Discord Snowflake ID
   *
   * @param snowflakeId Discord Snowflake ID
   * @return Optional containing the user if found
   */
  Optional<DVUser> findBySnowflakeId(Long snowflakeId);

  /**
   * Find a user by their Minecraft username
   *
   * @param mcUsername Minecraft username
   * @return Optional containing the user if found
   */
  Optional<DVUser> findByMcUsername(String mcUsername);

  /**
   * Get all users
   *
   * @return List of all users
   */
  List<DVUser> findAll();

  /**
   * Save a user (create or update)
   *
   * @param user User to save
   * @return Saved user
   */
  DVUser save(DVUser user);

  /**
   * Delete a user
   *
   * @param user User to delete
   */
  void delete(DVUser user);

  /**
   * Delete a user by ID
   *
   * @param id PocketBase ID of user to delete
   */
  void deleteById(String id);

  /**
   * Get all users that match a given condition
   *
   * @param predicate Condition to filter users
   * @return List of users matching the condition
   */
  List<DVUser> getAllWhere(String filter);
}
