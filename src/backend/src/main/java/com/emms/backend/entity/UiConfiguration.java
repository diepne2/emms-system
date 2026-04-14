package com.emms.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ui_configurations")
public class UiConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requests", nullable = false)
    private boolean requests = true;

    @Column(name = "locations", nullable = false)
    private boolean locations = true;

    @Column(name = "meters", nullable = false)
    private boolean meters = true;

    @Column(name = "vendors_and_customers", nullable = false)
    private boolean vendorsAndCustomers = true;

    public UiConfiguration() {
    }

    public UiConfiguration(boolean requests, boolean locations, boolean meters, boolean vendorsAndCustomers) {
        this.requests = requests;
        this.locations = locations;
        this.meters = meters;
        this.vendorsAndCustomers = vendorsAndCustomers;
    }

    public Long getId() {
        return id;
    }

    public boolean isRequests() {
        return requests;
    }

    public void setRequests(boolean requests) {
        this.requests = requests;
    }

    public boolean isLocations() {
        return locations;
    }

    public void setLocations(boolean locations) {
        this.locations = locations;
    }

    public boolean isMeters() {
        return meters;
    }

    public void setMeters(boolean meters) {
        this.meters = meters;
    }

    public boolean isVendorsAndCustomers() {
        return vendorsAndCustomers;
    }

    public void setVendorsAndCustomers(boolean vendorsAndCustomers) {
        this.vendorsAndCustomers = vendorsAndCustomers;
    }

    @Override
    public String toString() {
        return "UiConfiguration{" +
                "id=" + id +
                ", requests=" + requests +
                ", locations=" + locations +
                ", meters=" + meters +
                ", vendorsAndCustomers=" + vendorsAndCustomers +
                '}';
    }
}