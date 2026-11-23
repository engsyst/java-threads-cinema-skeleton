package org.threads.cinema;

/**
 * Utility class.
 * Stores seat counts, timing, and verification details from one simulation run.
 */
class Result {
    /** Number of seats reported by successful customer bookings. */
    int sold;
    /** Number of seats that actually have an owner. */
    int occupied;
    /** Number of seats claimed by more than one customer. */
    int doubleBooked;
    /** Number of seats with no owner. */
    int free;
    /** Simulation elapsed time in milliseconds. */
    long millis;
    /** Optional example of a double-booked seat. */
    String example = "";

    /**
     * Tests whether no seat was double-booked.
     *
     * @return {@code true} if verification found no conflicting claims
     */
    boolean correct() {
        return doubleBooked == 0;
    }
}
