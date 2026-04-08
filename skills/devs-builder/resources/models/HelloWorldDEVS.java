// File: HelloWorldDEVS.java
//
// A tiny standalone DEVS “engine” plus a HelloWorld atomic model.
// No external libraries, no other DEVS engine – just plain Java.
package Models.java;

public class HelloWorldDEVS {

    // ---------------------------------------------------------------------
    // 1. Minimal DEVS Atomic interface (classic: ta, δint, δext, λ)
    // ---------------------------------------------------------------------
    public interface AtomicModel {
        /** Time advance function: returns time until next internal event. */
        double ta();

        /** Internal transition δint: executed when scheduled internal event occurs. */
        void deltaInt();

        /**
         * External transition δext: executed when an input arrives before next internal event.
         *
         * @param e  elapsed time since last transition
         * @param x  input value
         */
        void deltaExt(double e, Object x);

        /**
         * Output function λ: called right before δint.
         *
         * @return output value (may be null if no output)
         */
        Object lambda();
    }

    // ---------------------------------------------------------------------
    // 2. A very small DEVS simulator for a single atomic model
    // ---------------------------------------------------------------------
    public static class AtomicSimulator {
        private final AtomicModel model;
        private double t;   // current simulation time

        public AtomicSimulator(AtomicModel model) {
            this.model = model;
            this.t = 0.0;
        }

        /**
         * Run simulation until the given time bound.
         *
         * @param until simulation end time
         */
        public void simulate(double until) {
            while (t < until) {
                double ta = model.ta();
                if (Double.isInfinite(ta)) {
                    // No more internal events scheduled
                    break;
                }

                double tNext = t + ta;
                if (tNext > until) {
                    // Next event would be beyond simulation end
                    break;
                }

                // Output phase (λ)
                Object y = model.lambda();
                if (y != null) {
                    System.out.println("t = " + tNext + " -> output: " + y);
                }

                // Internal transition (δint)
                model.deltaInt();

                // Advance time
                t = tNext;
            }
        }
    }

    // ---------------------------------------------------------------------
    // 3. HelloWorld atomic DEVS model
    // ---------------------------------------------------------------------
    public static class HelloWorldAtomic implements AtomicModel {

        private enum Phase {
            PASSIVE,
            DONE
        }

        private Phase phase;
        private double sigma; // time until next internal event

        public HelloWorldAtomic() {
            // Start in PASSIVE; schedule internal event at t = 1.0
            this.phase = Phase.PASSIVE;
            this.sigma = 1.0;
        }

        @Override
        public double ta() {
            return sigma;
        }

        @Override
        public void deltaInt() {
            if (phase == Phase.PASSIVE) {
                // After outputting once, go to DONE with no more events
                phase = Phase.DONE;
                sigma = Double.POSITIVE_INFINITY;
            } else if (phase == Phase.DONE) {
                // Remain done; no further events
                sigma = Double.POSITIVE_INFINITY;
            }
        }

        @Override
        public void deltaExt(double e, Object x) {
            // For a pure HelloWorld example, we ignore external input.
            // If you want to react to input later, you’d update phase/sigma here.
        }

        @Override
        public Object lambda() {
            if (phase == Phase.PASSIVE) {
                // Single “Hello, World!” output when the internal event fires
                return "Hello, World!";
            }
            // No output in DONE
            return null;
        }
    }

    // ---------------------------------------------------------------------
    // 4. Main: wire it together and run
    // ---------------------------------------------------------------------
    public static void main(String[] args) {
        HelloWorldAtomic helloModel = new HelloWorldAtomic();
        AtomicSimulator sim = new AtomicSimulator(helloModel);

        // Run simulation long enough to hit the single internal event at t=1.0
        sim.simulate(10.0);
    }
}
