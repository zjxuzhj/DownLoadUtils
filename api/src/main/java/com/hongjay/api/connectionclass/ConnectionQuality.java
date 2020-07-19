package com.hongjay.api.connectionclass;

public enum ConnectionQuality {
    /**
     * RTT over 2000 ms.
     */
    POOR,
    /**
     * Bandwidth between 550 and 2000 ms.
     */
    MODERATE,
    /**
     * Bandwidth between 150 and 550 ms.
     */
    GOOD,
    /**
     * EXCELLENT - RTT under 150 ms.
     */
    EXCELLENT,
    /**
     * Placeholder for unknown bandwidth. This is the initial value and will stay at this value
     * if a bandwidth cannot be accurately found.
     */
    UNKNOWN
}