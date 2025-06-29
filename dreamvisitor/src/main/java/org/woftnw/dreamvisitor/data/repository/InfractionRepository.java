package org.woftnw.dreamvisitor.data.repository;


import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Infraction data operations
 */
public interface InfractionRepository {
  /**
   * Find an infraction by its PocketBase ID
   *
   * @param id PocketBase record ID
   * @return Optional containing the infraction if found
   */
  Optional<Infraction> findById(String id);

  /**
   * Find all infractions for a user
   *
   * @param userId PocketBase user ID
   * @return List of infractions for the user
   */
  List<Infraction> findByUser(String userId);

  /**
   * Find all active (non-expired) infractions for a user
   *
   * @param userId PocketBase user ID
   * @return List of active infractions for the user
   */
  List<Infraction> findActiveByUser(String userId);

  /**
   * Get all infractions
   *
   * @return List of all infractions
   */
  List<Infraction> findAll();

  /**
   * Save an infraction (create or update)
   *
   * @param infraction Infraction to save
   * @return Saved infraction
   */
  Infraction save(Infraction infraction);

  /**
   * Delete an infraction
   *
   * @param infraction Infraction to delete
   */
  void delete(Infraction infraction);

  /**
   * Delete an infraction by ID
   *
   * @param id PocketBase ID of infraction to delete
   */
  void deleteById(String id);

  /**
   * Get all infractions matching a filter
   *
   * @param filter PocketBase filter expression
   * @return List of infractions matching the filter
   */
  List<Infraction> getAllWhere(String filter);

  /**
   * Load related user data for all infractions
   *
   * @param infractions List of infractions
   * @return List of infractions with loaded user data
   */
  List<Infraction> loadRelatedUsers(List<Infraction> infractions);
}
