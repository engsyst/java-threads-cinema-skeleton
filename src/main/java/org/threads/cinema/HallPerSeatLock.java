package org.threads.cinema;

/**
 * Hall implementation that protects each seat with its own lock.
 * Group requests (all or nothing). Lock order acquisition, to avoid deadlocks,
 * is controlled by {@code orderedGroups} flag.
 */
class HallPerSeatLock extends Hall {
    private final boolean orderedGroups;
    // TODO add your fields here

    /**
     * Creates a hall that acquires group locks seat-number ordering.
     */
    HallPerSeatLock() {
        this(false);
    }

    /**
     * Creates a per-seat-lock hall with the requested lock-order strategy.
     *
     * @param orderedGroups {@code true} to sort seats before acquiring locks
     */
    HallPerSeatLock(boolean orderedGroups) {
        this.orderedGroups = orderedGroups;
        // TODO add your code here

    }

    /**
     * Books a single seat.
     *
     * @param seatNumber seat to book
     * @param customer customer name
     * @return true if seat is booked
     */
    @Override
    public boolean tryBook(int seatNumber, String customer) {
        // TODO add your code here
        return false;
    }

    /**
     * Books a group of seats (all or nothing).
     * This method is used by a {@link CustomerGroupBooking} thread
     * that picks group different random seats per attempt.
     *
     * @param seatsToBook seat numbers to book
     * @param customer customer name
     * @return true if seats group is booked
     */
    @Override
    public boolean tryBook(int[] seatsToBook, String customer) {
        // TODO add your code here
        // TODO: implement method
        //       boolean tryBook(int[] seatsToBook, String customer)
        //       that books ALL seats or NONE.
        //
        // TODO: fix the problem by always locking the seat with the smaller number first.
        return false;
    }
}
