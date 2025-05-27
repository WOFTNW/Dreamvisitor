package io.github.stonley890.dreamvisitor.data.repository;

import java.util.List;
import java.util.Optional;

import io.github.stonley890.dreamvisitor.data.type.Item;

/**
 * Repository interface for Item data operations
 */
public interface ItemRepository {
  /**
   * Find an item by its PocketBase ID
   *
   * @param id PocketBase record ID
   * @return Optional containing the item if found
   */
  Optional<Item> findById(String id);

  /**
   * Find an item by its name
   *
   * @param name Item name
   * @return Optional containing the item if found
   */
  Optional<Item> findByName(String name);

  /**
   * Get all items
   *
   * @return List of all items
   */
  List<Item> findAll();

  /**
   * Get all enabled items
   *
   * @return List of all enabled items
   */
  List<Item> findAllEnabled();

  /**
   * Save an item (create or update)
   *
   * @param item Item to save
   * @return Saved item
   */
  Item save(Item item);

  /**
   * Delete an item
   *
   * @param item Item to delete
   */
  void delete(Item item);

  /**
   * Delete an item by ID
   *
   * @param id PocketBase ID of item to delete
   */
  void deleteById(String id);

  /**
   * Get all items matching a filter
   *
   * @param filter PocketBase filter expression
   * @return List of items matching the filter
   */
  List<Item> getAllWhere(String filter);
}
