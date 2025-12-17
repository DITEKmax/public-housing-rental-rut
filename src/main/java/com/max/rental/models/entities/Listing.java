package com.max.rental.models.entities;

import com.max.rental.models.enums.EnumListingStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "listings")
public class Listing extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_type_id", nullable = false)
    private PropertyType propertyType;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_per_night", precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "room_count")
    private Integer roomCount;

    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Column(name = "construction_year")
    private Integer constructionYear;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumListingStatus status;

    @OneToMany(mappedBy = "listing", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "listing", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(Integer constructionYear) {
        this.constructionYear = constructionYear;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public EnumListingStatus getStatus() {
        return status;
    }

    public void setStatus(EnumListingStatus status) {
        this.status = status;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Listing listing = (Listing) o;
        return Objects.equals(getId(), listing.getId()) &&
               Objects.equals(getCreatedAt(), listing.getCreatedAt()) &&
               Objects.equals(title, listing.title) &&
               Objects.equals(description, listing.description) &&
               Objects.equals(pricePerNight, listing.pricePerNight) &&
               Objects.equals(averageRating, listing.averageRating) &&
               Objects.equals(roomCount, listing.roomCount) &&
               Objects.equals(floor, listing.floor) &&
               Objects.equals(totalFloors, listing.totalFloors) &&
               Objects.equals(constructionYear, listing.constructionYear) &&
               Objects.equals(rules, listing.rules) &&
               status == listing.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreatedAt(), title, description, pricePerNight,
                averageRating, roomCount, floor, totalFloors, constructionYear, rules, status);
    }

    @Override
    public String toString() {
        return "Listing{" +
               "id=" + getId() +
               ", createdAt=" + getCreatedAt() +
               ", title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", pricePerNight=" + pricePerNight +
               ", averageRating=" + averageRating +
               ", roomCount=" + roomCount +
               ", floor=" + floor +
               ", totalFloors=" + totalFloors +
               ", constructionYear=" + constructionYear +
               ", rules='" + rules + '\'' +
               ", status=" + status +
               '}';
    }
}
