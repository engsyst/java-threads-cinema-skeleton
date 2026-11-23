package org.threads.cinema;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Daemon customer that attempts to book two to four distinct seats atomically.
 */
class CustomerGroupBooking extends CustomerBooking {
    /**
     * Creates a non-verbose daemon group customer.
     *
     * @param id customer identifier
     * @param hall hall in which to book seats
     */
    CustomerGroupBooking(int id, Hall hall) {
        super(id, hall, false);
        setDaemon(true);
    }

    /**
     * Creates a daemon group customer with optional verbose output.
     *
     * @param id customer identifier
     * @param hall hall in which to book seats
     * @param verbose whether successful groups are printed
     */
    CustomerGroupBooking(int id, Hall hall, boolean verbose) {
        super(id, hall, verbose);
        setDaemon(true);
    }

    /**
     * Builds a random distinct-seat group and attempts to book it as a unit.
     */
    @Override
    protected void attempt() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int groupSize = rnd.nextInt(2, 5);
        Set<Integer> seats = new HashSet<>();
        while (seats.size() < groupSize) {
            seats.add(rnd.nextInt(CinemaBooking.SEATS));
        }

        int[] seatsToBook = seats.stream().mapToInt(Integer::intValue).toArray();
        if (hall.tryBook(seatsToBook, getName())) {
            for (int seatNumber : seatsToBook) {
                booked.add(seatNumber);
            }
            if (verbose) {
                System.out.printf("[%s] booked seats %s%n", getName(), Arrays.toString(seatsToBook));
            }
        }
    }
}
