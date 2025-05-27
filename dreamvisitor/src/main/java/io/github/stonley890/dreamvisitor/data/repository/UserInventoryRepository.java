package io.github.stonley890.dreamvisitor.data.repository;


import java.util.List;
import java.util.Optional;

import io.github.stonley890.dreamvisitor.data.type.UserInventory;

/**
 * Repository interface for UserInventory data operations
 */
public interface UserInventoryRepository {
  /**
   * Find an inventory entry by its PocketBase ID
   *
   * @param id PocketBase record ID
   * @return Optional containing the inventory entry if found
   */
  Optional<UserInventory> findById(String id);

  /**
   * Find all inventory entries for a user
   *
   * @param userId PocketBase user ID
   * @return List of inventory entries for the user
   */
  List<UserInventory> findByUser(String userId);

  /**
   * Find inventory entry for a specific user and item
   *
   * @param userId PocketBase user ID
   * @param itemId PocketBase item ID
   * @return Optional containing the inventory entry if found
   */
  Optional<UserInventory> findByUserAndItem(String userId, String itemId);

  /**
   * Get all inventory entries
   *
   * @return List of all inventory entries
   */
  List<UserInventory> findAll();

  /**
   * Save an inventory entry (create or update)
   *
   * @param userInventory UserInventory to save
   * @return Saved inventory entry
   */
  UserInventory save(UserInventory userInventory);

  /**
   * Delete an inventory entry
   *
   * @param userInventory UserInventory to delete
   */
  void delete(UserInventory userInventory);

  /**
   * Delete an inventory entry by ID
   *
   * @param id PocketBase ID of inventory entry to delete
   */
  void deleteById(String id);

  /**
   * Get all inventory entries matching a filter
   *
   * @param filter PocketBase filter expression
   * @return List of inventory entries matching the filter
   */
  List<UserInventory> getAllWhere(String filter);

  /**
   * Load related item data for all inventory entries
   *
   * @param inventoryEntries List of inventory entries
   * @return List of inventory entries with loaded item data
   */
  List<UserInventory> loadRelatedItems(List<UserInventory> inventoryEntries);

  /**
   * Load related user data for all inventory entries
   *
   * @param inventoryEntries List of inventory entries
   * @return List of inventory entries with loaded user data
   */
  List<UserInventory> loadRelatedUsers(List<UserInventory> inventoryEntries);
}
