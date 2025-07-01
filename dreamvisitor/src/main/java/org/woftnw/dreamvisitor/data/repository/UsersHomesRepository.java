package org.woftnw.dreamvisitor.data.repository;


import org.bukkit.World;
import org.bukkit.entity.Player;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.data.type.UserHome;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User Home data operations
 */
public interface UsersHomesRepository extends Repository {
    /**
     * Find a user home by their PocketBase ID
     *
     * @param id PocketBase record ID
     * @return Optional containing the home if found
     */
    Optional<UserHome> findById(String id);

    List<UserHome> getHomesOfPlayer(UUID uuid);

    List<UserHome> getHomesOfPlayer(Player player);

    List<UserHome> getHomesOfWorld(World world);

    /**
     * Get all user homes
     *
     * @return List of all homes
     */
    List<UserHome> findAll();

    /**
     * Save a user home (create or update)
     *
     * @param home Home to save
     * @return Saved UserHome
     */
    UserHome save(UserHome home);

    /**
     * Delete a home
     *
     * @param home Home to delete
     */
    void delete(UserHome home);

    /**
     * Delete a home by ID
     *
     * @param id PocketBase ID of home to delete
     */
    void deleteById(String id);

}
