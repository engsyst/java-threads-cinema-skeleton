package org.threads.cinema;

/**
 * Represents one cinema seat and its current customer owner.
 * <p>
 * This class is intentionally not thread-safe; synchronization belongs to
 * the {@link Hall} implementation.
 */
class Seat {
    private final int number;
    private String owner;

    /**
     * Creates a seat with the supplied seat number.
     *
     * @param number index of the seat in the hall
     */
    Seat(int number) {
        this.number = number;
    }

    /**
     * Returns this seat's index.
     *
     * @return seat number
     */
    int number() {
        return number;
    }

    /**
     * Tests whether the seat is currently unoccupied.
     *
     * @return {@code true} if no customer owns the seat
     */
    boolean isFree() {
        return owner == null;
    }

    /**
     * Assigns the seat to a customer.
     *
     * @param customer name of the booking customer
     */
    void book(String customer) {
        owner = customer;
    }
}
