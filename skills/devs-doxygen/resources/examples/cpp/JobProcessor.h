#ifndef JOB_PROCESSOR_H
#define JOB_PROCESSOR_H

#include "adevs.h"

/// A unit of work: an id and a service demand in model time units.
struct Job {
    int id;
    double size;
};

/// Port/value pair type exchanged by this model.
typedef adevs::PortValue<Job> IO_Type;

/**
 * JobProcessor — single-server processor without a queue (adevs).
 *
 * Accepts one job on port @c in while passive, holds in @c BUSY for a fixed
 * processing time, emits the job on port @c out, then returns to @c PASSIVE.
 * Jobs arriving while busy are dropped.
 */
class JobProcessor : public adevs::Atomic<IO_Type> {
public:
    /// Input port carrying jobs to be processed.
    static const int in;
    /// Output port carrying finished jobs.
    static const int out;

    /// Create a passive processor with the given fixed service time.
    explicit JobProcessor(double processing_time = 1.0)
        : adevs::Atomic<IO_Type>(),
          phase(PASSIVE), sigma(DBL_MAX), processing_time(processing_time), processed(0) {}

    /// Internal transition: retire the emitted job and go passive.
    void delta_int() {
        processed++;
        phase = PASSIVE;
        sigma = DBL_MAX;
    }

    /// External transition: accept the first job when passive, otherwise drop it.
    void delta_ext(double e, const adevs::Bag<IO_Type>& xb) {
        if (phase == PASSIVE && !xb.empty()) {
            current = (*xb.begin()).value;
            phase = BUSY;
            sigma = processing_time;
        } else {
            sigma -= e;
        }
    }

    /// Confluent transition: internal first, then external with e = 0.
    void delta_conf(const adevs::Bag<IO_Type>& xb) {
        delta_int();
        delta_ext(0.0, xb);
    }

    /// Output function: emit the current job on @c out while busy.
    void output_func(adevs::Bag<IO_Type>& yb) {
        if (phase == BUSY) {
            yb.insert(IO_Type(out, current));
        }
    }

    /// Time advance: remaining hold time.
    double ta() { return sigma; }

    /// Nothing to free: Job is a value type.
    void gc_output(adevs::Bag<IO_Type>&) {}

    /// Number of jobs completed so far.
    int jobs_processed() const { return processed; }

private:
    /// Discrete phase of the processor.
    enum Phase { PASSIVE, BUSY };

    Phase phase;             ///< Current phase.
    double sigma;            ///< Remaining hold time (DBL_MAX when passive).
    Job current;             ///< Job being served while busy.
    double processing_time;  ///< Fixed service time per job.
    int processed;           ///< Completed-job counter.
};

const int JobProcessor::in  = 0;
const int JobProcessor::out = 1;

#endif // JOB_PROCESSOR_H
