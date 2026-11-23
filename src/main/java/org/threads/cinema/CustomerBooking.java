package org.threads.cinema;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Thread that repeatedly attempts to book a random individual seat.
 * Booking results are stored privately until the thread is joined.
 */
class CustomerBooking extends Thread {
    protected final Hall hall;
    protected final boolean verbose;
    final List<Integer> booked = new ArrayList<>();

    /**
     * Creates a customer thread.
     *
     * @param id customer identifier
     * @param hall hall in which to book seats
     * @param verbose whether successful bookings are printed
     */
    CustomerBooking(int id, Hall hall, boolean verbose) {
        super("Customer-" + String.format("%02d", id));
        this.hall = hall;
        this.verbose = verbose;
    }

    /**
     * Creates a non-verbose customer thread.
     *
     * @param id customer identifier
     * @param hall hall in which to book seats
     */
    CustomerBooking(int id, Hall hall) {
        this(id, hall, false);
    }

    /**
     * Repeatedly performs booking attempts until the attempt limit or interruption.
     */
    @Override
    public void run() {
        for (int i = 0; i < CinemaBooking.ATTEMPTS && !isInterrupted(); i++) {
            attempt();
        }
    }

    /**
     * Makes one random single-seat booking attempt.
     */
    protected void attempt() {
        int seatNumber = ThreadLocalRandom.current().nextInt(CinemaBooking.SEATS);
        if (hall.tryBook(seatNumber, getName())) {
            booked.add(seatNumber);
            if (verbose) {
                System.out.printf("[%s] booked seat %d%n", getName(), seatNumber);
            }
        }
    }
}
