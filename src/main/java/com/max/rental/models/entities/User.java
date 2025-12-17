package com.max.rental.models.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String phone;

    @Column(name = "owner_rating")
    private Double ownerRating = 0.0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    public User() {
    }

    public User(String email, String passwordHash, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isOwner() {
        return roles.stream()
                .anyMatch(r -> r.getName().name().equals("OWNER"));
    }

    public boolean isAdmin() {
        return roles.stream()
                .anyMatch(r -> r.getName().name().equals("ADMIN"));
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getOwnerRating() {
        return ownerRating;
    }

    public void setOwnerRating(Double ownerRating) {
        this.ownerRating = ownerRating;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId()) &&
               Objects.equals(getCreatedAt(), user.getCreatedAt()) &&
               Objects.equals(email, user.email) &&
               Objects.equals(passwordHash, user.passwordHash) &&
               Objects.equals(emailVerified, user.emailVerified) &&
               Objects.equals(firstName, user.firstName) &&
               Objects.equals(lastName, user.lastName) &&
               Objects.equals(phone, user.phone) &&
               Objects.equals(ownerRating, user.ownerRating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreatedAt(), email, passwordHash, emailVerified,
                firstName, lastName, phone, ownerRating);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + getId() +
               ", createdAt=" + getCreatedAt() +
               ", email='" + email + '\'' +
               ", emailVerified=" + emailVerified +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", phone='" + phone + '\'' +
               ", ownerRating=" + ownerRating +
               '}';
    }
}
