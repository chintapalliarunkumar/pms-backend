package com.cognizant.policyservice.entity;

/**
 * Supported policy types along with their short codes used
 * for generating human-readable policy identifiers.
 * e.g. Health Insurance -> HI-2026-001
 */
public enum PolicyType {

    VEHICLE_INSURANCE("VI"),
    TRAVEL_INSURANCE("TI"),
    LIFE_INSURANCE("LI"),
    HEALTH_INSURANCE("HI"),
    CHILD_PLANS("CP"),
    RETIREMENT_PLANS("RP");

    private final String shortCode;

    PolicyType(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}
