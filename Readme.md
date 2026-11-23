# Thread Synchronization: Cinema Ticket Booking

*Methodical instructions for students*

## 1. Purpose of the work

The purpose of this work is to study how several threads can safely work with shared data, using a simulation of an online cinema booking system.

After completing the work you will be able to:

- recognize and reproduce a race condition in a multithreaded program;
- make a critical section thread-safe and verify the result;
- compare solutions that protect shared data at different granularity and measure the difference in performance;
- recognize a deadlock, reproduce it, and eliminate it;
- verify the results of a concurrent program by an independent check after all threads have finished.

## 2. Theoretical background

Read the lecture notes on threads and synchronization before starting. Make sure you can explain the following terms:

- **shared mutable state**: data that several threads can read and modify;
- **race condition**: the result depends on the exact interleaving of threads;
- **check-then-act**: a decision based on a value that may change before the action is performed;
- **critical section** and **mutual exclusion**;
- **atomicity**: an operation is seen by other threads either as not started or as completed;
- **lock granularity**: how much data one synchronization object protects (coarse-grained vs. fine-grained);
- **deadlock**: two or more threads wait for each other forever;
- **liveness vs. safety**: "nothing bad happens" vs. "something good eventually happens".

Use only the thread-management and synchronization tools introduced in the course so far. If you are not sure that a tool is allowed, ask the instructor.

## 3. Problem statement

A cinema hall has 50 seats. Many customers try to buy tickets at the same time through an online booking system. Each customer is a separate thread. A seat may be sold to **only one** customer.

A booking of one seat consists of the following steps:

1. Look up the seat in the "database" (simulated by sleeping 1 ms).
2. If the seat is free: process the payment (simulated by sleeping 5 ms) and mark the seat as booked by this customer; the booking succeeds.
3. If the seat is already taken: the booking fails.

You must implement this logic in three versions of the hall:

| Version | Class | Requirement |
| --- | --- | --- |
| A | `HallUnsafe` | no protection of shared data at all |
| B | `HallGlobalLock` | correct; the whole hall is protected as one unit |
| C | `HallPerSeatLock` | correct; every seat is protected independently |

Then compare correctness and speed of the three versions.

**Part 2 (extension).** Some customers buy a *group* of 2–4 seats. A group booking is **all-or-nothing**: either all requested seats are booked for this customer, or none of them. You must implement it, run the group simulation, observe what happens, and make the program behave correctly.

## 4. Project structure

The project is the package `org.threads.cinema`. Files marked **given** must not be changed (except for experiments with the constants, see section 6). Files marked **to implement** contain `TODO` comments.

| File | Status | Purpose |
| --- | --- | --- |
| `CinemaBooking` | given | Entry point. Holds the constants (`SEATS`, `CUSTOMERS`, `ATTEMPTS`, `RUNS`), runs the simulation for each hall version, measures time, verifies the result, prints the comparison table, and contains the group-booking demo. |
| `Seat` | given | One seat and its owner (`null` means free). It is intentionally **not** thread-safe: protection belongs to the hall. |
| `Hall` | **to implement** (partly) | Abstract base class of all halls. Creates the seats, counts occupied seats. You implement the shared helpers `checkAndBook(...)` and `validSeatGroup(...)`. |
| `HallUnsafe` | **to implement** | Version A. |
| `HallGlobalLock` | **to implement** | Version B. |
| `HallPerSeatLock` | **to implement** | Version C and the group booking with a switch `orderedGroups`. |
| `CustomerBooking` | given | A customer thread. Makes `ATTEMPTS` attempts to book a random seat and stores the seats it has bought in its own private list `booked`. |
| `CustomerGroupBooking` | given | A customer thread that tries to book a random group of 2–4 distinct seats at once. It is a daemon thread, so a hung program does not keep the JVM alive. |
| `Result` | given | Data of one simulation run: seats sold, occupied, free, double-booked, elapsed time. |

### How the simulation works

1. `CinemaBooking` creates a hall (one of the three versions) and `CUSTOMERS = 100` customer threads.
2. All customers are started; each one calls the hall's `tryBook(...)` method `ATTEMPTS = 10` times.
3. The main thread waits until **all** customers have finished.
4. Only then it **verifies** the result: from the customers' private lists it builds a table "seat → customers who believe they bought it" and counts the seats claimed by more than one customer. A run is **correct** only if there are no such seats.
5. The whole procedure is repeated `RUNS = 10` times for each version, and the averages are printed.

Note that the verification does not trust the hall: it compares what customers were told with what the hall actually contains (`sold`, `occupied`, `double-booked`).

## 5. Order of work

### Step 0. Run the project

Compile and run `CinemaBooking`. Before you implement anything the program runs, but all numbers are zero. This is normal: all `tryBook` methods are stubs that return `false`.

### Step 1. The booking logic (`Hall.checkAndBook`)

Implement `checkAndBook(Seat seat, String customer)` exactly as described in section 3, using `CinemaBooking.sleepMs(...)` for the delays. Do **not** add any protection of shared data here. The method is shared by all versions, so the three versions must differ only in how they call it.

### Step 2. Version A: `HallUnsafe`

Implement `tryBook(int, String)` by simply delegating to `checkAndBook(...)`.

Run the program several times. You must be able to **reproduce** the problem. Look at the columns `sold`, `occupied` and `double-booked` and explain in your own words what you see and why it happens. Pay attention to the place where the time passes between the decision and the action.

### Step 3. Version B: `HallGlobalLock`

Make `tryBook(int, String)` correct, protecting the **whole hall** as one unit. Run the program and check that every run is correct. Record the average time.

### Step 4. Version C: `HallPerSeatLock`

Make `tryBook(int, String)` correct, but now every **seat** must be protected independently: two customers who want different seats must not wait for each other. Add the fields and the constructor code you need. Check that every run is correct and record the average time.

### Step 5. Compare

Fill in the table (the program prints it for you):

| Version | Double-booked seats (avg. over 10 runs) | Avg. time, ms |
| --- | --- | --- |
| A: unsafe |  |  |
| B: global lock |  |  |
| C: per-seat lock |  |  |

Explain the difference in time between the versions, in particular between B and C.

### Step 6 (Part 2). Group validation: `Hall.validSeatGroup`

Implement `validSeatGroup(int[] seatsToBook)`. A group is valid if the array is not `null`, is not empty, all seat numbers are in the range of the hall, and there are no duplicates. The method must not modify the caller's array. An invalid group is never booked: `tryBook(int[], String)` returns `false`.

### Step 7 (Part 2). Group booking: `tryBook(int[], String)`

Implement `tryBook(int[] seatsToBook, String customer)` in `HallUnsafe`, `HallGlobalLock` and `HallPerSeatLock`.

Requirements:

- the booking is **all-or-nothing**: if at least one seat is already taken, **no** seat of the group may be booked and the method returns `false`;
- the simulated delays are the same as for a single seat (lookup, then payment);
- invalid groups are rejected (Step 6);
- in `HallPerSeatLock` the constructor parameter `orderedGroups` controls the processing order of the seats of a group: for `false` the seats are processed in the order they were passed, for `true` in ascending seat-number order.

### Step 8 (Part 2). Group demo

Run `CinemaBooking` with the argument `group` (or `pairs`). The program runs the group simulation twice: with `orderedGroups = false` and with `orderedGroups = true`. The program waits 5 seconds; if the customers have not finished, it reports a suspected deadlock and prints the threads involved.

Run each mode several times. Describe what you observe, explain the reason, and explain why the second mode behaves differently.

## 6. Rules and restrictions

1. Do not change the *given* files. For experiments you may temporarily change the constants in `CinemaBooking`, but the submitted results must be obtained with `SEATS = 50`, `CUSTOMERS = 100`, `ATTEMPTS = 10`, `RUNS = 10`.
2. Do not remove or shorten the simulated delays: they model real database and payment latency.
3. Do not share the `booked` lists between customers and do not modify them from the hall. They are the independent evidence used for verification.
4. The three halls must differ only in how they protect shared data; the booking logic itself must be the same.
5. Do not "fix" the unsafe version by other means (for example, by running customers one after another). All customer threads must really run concurrently.
6. Do not simply sleep or retry until the results look good: the solution must be correct for any thread interleaving, not only for the one you observed.
7. A program that sometimes passes is not necessarily correct. Run each version at least 10 times.

## 7. Environment

- JDK 25 or later (the project uses the unnamed variable `_` and a `static void main` entry point without the `public` modifier).
- Any IDE or the command line. Compile the whole package and run the class `org.threads.cinema.CinemaBooking`; it is not a single-file program.
- Arguments: none (versions A, B, C) or `group` / `pairs` (group-booking demo).
- Absolute times depend on your machine. The *relations* between the versions are what matters.

## 8. Expected output (illustrative)

```
=== Cinema booking simulation ===
Seats: 50, Customers: 100, Attempts per customer: 10, Runs: 10

=== Version A: unsafe ===
[Customer-17] booked seat 23
[Customer-42] booked seat 23
...
Run  1: sold=118 occupied=50 free= 0 double-booked=41 time=   31 ms INCORRECT  e.g. seat 23: [Customer-17, Customer-42]
...

=== Version B: global lock ===
Run  1: sold= 50 occupied=50 free= 0 double-booked= 0 time= 1350 ms OK
...

=== Comparison (average over 10 runs) ===
Version            | Double-booked seats    | Avg. time, ms
A: unsafe          | 40.4                   | 32
B: global lock     | 0.0                    | 1356
C: per-seat lock   | 0.0                    | 44
```

*The numbers are illustrative; yours will differ.* Use the following as a self-check:

- in version A `sold` is larger than `occupied` and runs are reported as `INCORRECT`;
- in versions B and C, `double-booked` is always 0 and `sold` equals `occupied`;
- version C is clearly faster than version B.

## 9. Report

The report must contain:

1. the purpose of the work;
2. the problem statement;
3. the source code of all classes you implemented (`Hall`, `HallUnsafe`, `HallGlobalLock`, `HallPerSeatLock`), with comments on the important places;
4. console output of each version (at least one full run with individual bookings for version A, and the final comparison table);
5. the comparison table and your explanation of the results (sections Step 2 and Step 5);
6. Part 2: the output of both group-demo modes and your explanation of what happened;
7. answers to the control questions;
8. conclusions.

## 10. Grading

| Item | Points |
| --- | --- |
| Version A implemented, the problem is reproduced and explained | 0.5 |
| Version B correct in all runs | 0.5 |
| Version C correct in all runs, with comparison and explanation of the timing | 1 |
| **Total for Part 1** | **2** |
| Part 2: group booking implemented, problem demonstrated, explained and solved | +1 |

Points are reduced if the program is correct only by chance, if given files were modified, or if the report contains no explanation of the observed results.

## 11. Control questions

1. Which two operations make up the "check-then-act" pattern in a booking? Why can two threads both see a seat as free?
2. Why does the delay between the check and the booking make the problem easier to reproduce? If you remove the delays and the problem seems to disappear, does it mean the program is correct?
3. Suppose the *check* and the *booking* were each made safe on their own, but the booking method as a whole were not. Would the problem be solved? Why or why not?
4. Why is version B correct, but slow? Which operations are unnecessarily serialized?
5. What object protects a seat in version C? Why must all threads that access the same seat use the *same* protecting object?
6. What is lock granularity? What are the advantages and disadvantages of fine-grained and coarse-grained protection (speed, complexity, risk of deadlock, memory)?
7. Is it a good idea to perform slow operations (I/O, waiting) while holding a lock in a real system? How would you restructure the booking to keep the protected part short while remaining correct?
8. Does the program guarantee that the customer who has waited longest gets the seat? Is the order in which threads acquire a lock fair?
9. Running the unsafe program 100 times without a problem does not prove it correct. How could you convince yourself that your synchronized version is correct?
10. Why can two group customers block each other forever when one requests seats (5, 9) and the other (9, 5)? Why does a fixed processing order of the seats prevent this?
11. Why does a group booking need to be all-or-nothing? What inconsistent state could appear if it were not?

## 12. Hints on debugging

- If all numbers are zero, check that `checkAndBook` really returns `true` after a successful booking.
- If `sold` and `occupied` differ in a *correct* version, look for a booking that changed some seats but reported failure (or the opposite).
- If the program hangs, take a thread dump (`jstack <pid>` or Ctrl+\\ / Ctrl+Break in the console) and read what each thread is waiting for.
- When a problem is rare, increase `ATTEMPTS` or the number of runs to make it visible, but do not submit results with changed constants.
