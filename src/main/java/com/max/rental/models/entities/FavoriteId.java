package com.max.rental.models.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for favorites table.
 * Combination of renter_id + listing_id
 */
public class FavoriteId implements Serializable {

    private Long renterId;
    private Long listingId;

    public FavoriteId() {
    }

    public FavoriteId(Long renterId, Long listingId) {
        this.renterId = renterId;
        this.listingId = listingId;
    }

    public Long getRenterId() {
        return renterId;
    }

    public void setRenterId(Long renterId) {
        this.renterId = renterId;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteId that = (FavoriteId) o;
        return Objects.equals(renterId, that.renterId) &&
               Objects.equals(listingId, that.listingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(renterId, listingId);
    }
}
