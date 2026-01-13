package com.onboarding.platform.core.subject;

import com.onboarding.platform.core.type.OnboardingType;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Version;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.generator.EventType;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents the subject (entity) being onboarded.
 * Contains identity and contact information.
 */
@Entity
@Table(name = "onboarding_subjects")
@Serdeable
public class OnboardingSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OnboardingType type;

    // Identity fields
    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    // Business/Individual specific
    @Column(length = 100)
    private String businessName;

    @Column(length = 50)
    private String taxId;

    @Column(length = 100)
    private String registrationNumber;

    // Address
    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    // Metadata
    @Column(nullable = false)
    private String createdBy;

    @DateCreated
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @DateUpdated
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    @Generated(event = EventType.INSERT)
    private Long version;

    public OnboardingSubject() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OnboardingType getType() {
        return type;
    }

    public void setType(OnboardingType type) {
        this.type = type;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Business methods
    public boolean isBusinessEntity() {
        return type == OnboardingType.BUSINESS ||
                type == OnboardingType.PARTNER ||
                type == OnboardingType.VENDOR;
    }

    public String getDisplayName() {
        return isBusinessEntity() && businessName != null ? businessName : fullName;
    }
}
