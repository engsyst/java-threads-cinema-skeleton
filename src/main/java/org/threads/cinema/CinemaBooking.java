package org.threads.cinema;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Entry point for the cinema booking concurrency simulation.
 * <p>
 * The application compares booking implementations with no synchronization,
 * a global lock, and per-seat locks. Pass {@code group} or {@code pairs} to
 * run the group-booking deadlock demonstration.
 */
public class CinemaBooking {

    /** Number of seats in the simulated cinema hall. */
    static final int SEATS = 50;
    /** Number of regular customers used in each simulation run. */
    static final int CUSTOMERS = 100;
    /** Maximum number of booking attempts made by each customer thread. */
    static final int ATTEMPTS = 10;
    /** Number of runs used to calculate version averages. */
    static final int RUNS = 10;

    /**
     * Runs the cinema booking demonstration.
     *
     * @param args use {@code group} or {@code pairs} for the group-booking demo
     * @throws Exception when a customer thread cannot be joined
     */
    static void main(String[] args) throws Exception {
        System.out.printf("=== Cinema booking simulation ===%nSeats: %d, Customers: %d, Attempts per customer: %d, Runs: %d%n",
                SEATS, CUSTOMERS, ATTEMPTS, RUNS);

        if (args.length > 0 && (args[0].equals("group") || args[0].equals("pairs"))) {
            groupBookingDemo("Extension: group booking, locks in argument order", false);
            groupBookingDemo("Extension: group booking, locks in seat-number order", true);
            return;
        }

        double[] a = runVersion("Version A: unsafe", HallUnsafe::new);
        double[] b = runVersion("Version B: global lock", HallGlobalLock::new);
        double[] c = runVersion("Version C: per-seat lock", HallPerSeatLock::new);

        System.out.println("\n=== Comparison (average over " + RUNS + " runs) ===");
        System.out.printf("%-18s | %-22s | %s%n", "Version", "Double-booked seats", "Avg. time, ms");
        System.out.printf("%-18s | %-22.1f | %.0f%n", "A: unsafe", a[0], a[1]);
        System.out.printf("%-18s | %-22.1f | %.0f%n", "B: global lock", b[0], b[1]);
        System.out.printf("%-18s | %-22.1f | %.0f%n", "C: per-seat lock", c[0], c[1]);
    }

    /**
     * Verifies that no seat was sold to more than one customer.
     *
     * @param customers completed customer threads
     * @param hall hall containing the actual seat owners
     * @param millis elapsed time of the simulation
     * @return verification counts and timing
     */
    static Result verify(List<? extends CustomerBooking> customers, Hall hall, long millis) {
        Map<Integer, List<String>> claims = new TreeMap<>();
        int sold = 0;
        for (CustomerBooking customer : customers) {
            for (int seat : customer.booked) {
                claims.computeIfAbsent(seat, _ -> new ArrayList<>()).add(customer.getName());
                sold++;
            }
        }

        Result result = new Result();
        result.sold = sold;
        result.occupied = hall.occupiedCount();
        result.free = SEATS - result.occupied;
        result.millis = millis;
        for (Map.Entry<Integer, List<String>> entry : claims.entrySet()) {
            if (entry.getValue().size() > 1) {
                if (result.doubleBooked == 0) {
                    result.example = "seat " + entry.getKey() + ": " + entry.getValue();
                }
                result.doubleBooked++;
            }
        }
        return result;
    }

    /**
     * Runs one complete regular-customer simulation.
     *
     * @param hallFactory factory that creates the hall implementation
     * @param verbose whether the first run prints individual bookings
     * @return verification result
     * @throws InterruptedException if joining a customer thread is interrupted
     */
    static Result runOnce(Supplier<Hall> hallFactory, boolean verbose) throws InterruptedException {
        Hall hall = hallFactory.get();
        List<CustomerBooking> customers = new ArrayList<>();
        for (int i = 1; i <= CUSTOMERS; i++) {
            customers.add(new CustomerBooking(i, hall, verbose));
        }

        long start = System.nanoTime();
        for (CustomerBooking customer : customers) customer.start();
        for (CustomerBooking customer : customers) customer.join();
        long millis = (System.nanoTime() - start) / 1_000_000;
        return verify(customers, hall, millis);
    }

    /**
     * Runs all iterations of one hall implementation and prints their results.
     *
     * @param title display name of the implementation
     * @param factory factory that creates a new hall for each run
     * @return average double-booking count and average elapsed time
     * @throws InterruptedException if joining a customer thread is interrupted
     */
    static double[] runVersion(String title, Supplier<Hall> factory) throws InterruptedException {
        System.out.println("\n=== " + title + " ===");
        double sumDouble = 0;
        double sumTime = 0;
        for (int run = 1; run <= RUNS; run++) {
            Result result = runOnce(factory, run == 1);
            System.out.printf("Run %2d: sold=%3d occupied=%2d free=%2d double-booked=%2d time=%5d ms %s%s%n",
                    run, result.sold, result.occupied, result.free, result.doubleBooked, result.millis,
                    result.correct() ? "OK" : "INCORRECT",
                    result.doubleBooked > 0 ? "  e.g. " + result.example : "");
            sumDouble += result.doubleBooked;
            sumTime += result.millis;
        }
        return new double[]{sumDouble / RUNS, sumTime / RUNS};
    }

    /**
     * Sleeps while preserving the current thread's interrupted status.
     *
     * @param ms number of milliseconds to sleep
     */
    static void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Demonstrates group booking with argument-ordered or seat-number-ordered locks.
     *
     * @param title display name of the demonstration
     * @param ordered whether seat locks are acquired in ascending seat-number order
     * @throws InterruptedException if joining a group customer is interrupted
     */
    static void groupBookingDemo(String title, boolean ordered) throws InterruptedException {
        System.out.println("\n=== " + title + " ===");
        Hall hall = new HallPerSeatLock(ordered);
        List<CustomerBooking> customers = new ArrayList<>();
        for (int i = 1; i <= CUSTOMERS / 3; i++) {
            customers.add(new CustomerGroupBooking(i, hall, true));
        }

        long start = System.nanoTime();
        for (CustomerBooking customer : customers) customer.start();

        long deadline = System.currentTimeMillis() + 5_000;
        for (CustomerBooking customer : customers) {
            long left = deadline - System.currentTimeMillis();
            if (left > 0) customer.join(left);
        }
        long millis = (System.nanoTime() - start) / 1_000_000;

        boolean stuck = customers.stream().anyMatch(Thread::isAlive);
        if (stuck) {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            long[] ids = threadMXBean.findDeadlockedThreads();
            System.out.println("Not finished after 5 s -> DEADLOCK suspected.");
            if (ids != null) {
                System.out.println("Deadlocked threads: " + ids.length);
                for (ThreadInfo threadInfo : threadMXBean.getThreadInfo(ids)) {
                    System.out.printf("  %s waits for %s held by %s%n",
                            threadInfo.getThreadName(), threadInfo.getLockName(), threadInfo.getLockOwnerName());
                }
            }
        } else {
            Result result = verify(customers, hall, millis);
            System.out.printf("Finished: seats occupied=%d, double-booked=%d, time=%d ms %s%n",
                    result.occupied, result.doubleBooked, result.millis,
                    result.correct() ? "OK" : "INCORRECT");
        }
    }
}
