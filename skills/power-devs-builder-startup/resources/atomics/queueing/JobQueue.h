#if !defined JobQueue_h
#define JobQueue_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include "general/BaseSimulator.h"

#include "hybrid/packettool.h"
#include "queueing/Jobs/Job.h"


class JobQueue: public BaseSimulator {
	// parameters
	int maxCapacity;

	// State Variables
	std::deque<std::shared_ptr<Job>> queuedJobs;

	double stateOutSignal[10];

	// Counters
	int discardedJobs; // Counter for logging

	enum STATE {WAITING,t_NOTIFYING,t_READYTOEMIT};
	STATE mystate;
	enum SERVERSTATE {SERVERWAITING,SERVERBUSY};
	SERVERSTATE serverstate;

public:
	JobQueue(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
};

#endif
