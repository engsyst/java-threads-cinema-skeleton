package org.threads.cinema;

/**
 * Hall implementation that synchronizes every booking with a global lock.
 */
class HallGlobalLock extends Hall {
    @Override
    public boolean tryBook(int seatNumber, String customer) {
        // TODO implement thread safe version
        return false;
    }

    @Override
    public boolean tryBook(int[] seatsToBook, String customer) {
        // TODO implement thread safe version
        return false;
    }
}
