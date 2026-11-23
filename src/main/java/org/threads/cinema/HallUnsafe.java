package org.threads.cinema;

/**
 * Hall implementation without synchronization.
 * It demonstrates races in concurrent seat booking.
 */
class HallUnsafe extends Hall {
    @Override
    public boolean tryBook(int seatNumber, String customer) {
        // TODO: delegate to checkAndBook(...) without any locking
        return false;
    }

    @Override
    public boolean tryBook(int[] seatsToBook, String customer) {
        // TODO: implement non-synchronized version
        return false;
    }
}
