package org.threads.cinema;

/**
 * Base class for cinema hall implementations.
 * <p>
 * Subclasses define how individual and group bookings are synchronized.
 */
abstract class Hall {
    protected final Seat[] seats = new Seat[CinemaBooking.SEATS];

    /**
     * Initializes all seats in the hall.
     */
    public Hall() {
        for (int i = 0; i < CinemaBooking.SEATS; i++) {
            seats[i] = new Seat(i);
        }
    }

    /**
     * Attempts to book one seat for a customer.
     *
     * @param seatNumber seat to book
     * @param customer customer name
     * @return {@code true} if the booking succeeded
     */
    public abstract boolean tryBook(int seatNumber, String customer);

    /**
     * Attempts to book a complete group of distinct seats.
     * @see Hall#checkAndBook(Seat, String)
     *
     * @param seatsToBook seat numbers to book
     * @param customer customer name
     * @return {@code true} if every requested seat was booked
     */
    public boolean tryBook(int[] seatsToBook, String customer) {
        throw new UnsupportedOperationException();
    }

    /**
     * Utility method. Contains booking logic.<br>
     * Simulates lookup and payment before booking one seat.<br>
     * Used by {@code tryBook(...)} methods.
     *
     * @param seat seat to book
     * @param customer customer name
     * @return {@code true} if the seat was free and was booked
     */
    protected boolean checkAndBook(Seat seat, String customer) {
        /* TODO The "check-then-act" sequence shared by all three versions:
         *   1. simulate a database lookup  (sleep 1 ms)
         *   2. if the seat is free:
         *        simulate payment          (sleep 5 ms)
         *        mark the seat as booked by this customer
         *        return true
         *   3. otherwise return false
         * Use {@code CinemaBooking.sleepMs(1)} to simulate delays
         */
        return false;
    }

    /**
     * Validates a group without mutating the caller's array.<br>
     * A seat group is considered valid if:
     * <ul>
     *   <li>The array is not {@code null} and has at least one seat.</li>
     *   <li>All seat numbers are within the valid hall range (0 &lt; seat &lt; seats.length).</li>
     *   <li>There are no duplicate seat numbers in the request.</li>
     * </ul>
     *
     * @param seatsToBook requested seat numbers
     * @return {@code true} if the group is non-empty, in range, and unique
     */
    protected boolean validSeatGroup(int[] seatsToBook) {
        // TODO implement validation logic
        return false;
    }

    /**
     * Utility method.
     * Counts seats that currently have an owner.
     *
     * @return number of occupied seats
     */
    int occupiedCount() {
        int count = 0;
        for (Seat seat : seats) {
            if (!seat.isFree()) count++;
        }
        return count;
    }
}
