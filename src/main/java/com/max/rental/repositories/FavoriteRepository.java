package com.max.rental.repositories;

import com.max.rental.models.entities.Favorite;
import com.max.rental.models.entities.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    List<Favorite> findAllByRenterId(Long renterId);

    boolean existsByRenterIdAndListingId(Long renterId, Long listingId);

    void deleteByRenterIdAndListingId(Long renterId, Long listingId);

    long countByRenterId(Long renterId);
}