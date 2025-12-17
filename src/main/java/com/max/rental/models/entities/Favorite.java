package com.max.rental.models.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@IdClass(FavoriteId.class)
public class Favorite {

    @Id
    @Column(name = "renter_id")
    private Long renterId;

    @Id
    @Column(name = "listing_id")
    private Long listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", insertable = false, updatable = false)
    private User renter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Favorite() {
    }

    public Favorite(Long renterId, Long listingId) {
        this.renterId = renterId;
        this.listingId = listingId;
    }

    public Favorite(Long renterId, Long listingId, User renter, Listing listing, LocalDateTime createdAt) {
        this.renterId = renterId;
        this.listingId = listingId;
        this.renter = renter;
        this.listing = listing;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public User getRenter() {
        return renter;
    }

    public void setRenter(User renter) {
        this.renter = renter;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
