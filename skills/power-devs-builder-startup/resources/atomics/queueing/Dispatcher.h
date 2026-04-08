#if !defined Dispatcher_h
#define Dispatcher_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"
#include "vector/VectorialEvent.h"

#include "DispatcherStrategies/IDispatcherStrategy.h"
#include "DispatcherStrategies/FifoDispatcherStrategy.h"
#include "DispatcherStrategies/JsqDispatcherStrategy.h"
#include "DispatcherStrategies/BalancedDispatcherStrategy.h"
#include "DispatcherStrategies/RandomLoadAwareDispatcherStrategy.h"
#include "DispatcherStrategies/RandomDispatcherStrategy.h"
#include "Jobs/Job.h"
#include "Jobs/FinishedJob.h"
#include "Jobs/JobAssignment.h"


#include <memory>

class Dispatcher: public BaseSimulator {
private:

	enum DISPATCHER_STRATEGY { FIFO, RANDOM_LOAD_AWARE, JOIN_SHORTEST_QUEUE, BALANCED, RANDOM };


	// State variables
	std::shared_ptr<IDispatcherStrategy> strategy;
	uint nextProcessorToAssign;
	std::shared_ptr<Job> nextOutputJob;

	// Counters
	int finished = 0;
	int sent = 0;

public:
	Dispatcher(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
private:
	void processNewJob(double t, std::shared_ptr<Job> job);
	void processFinishedJob(double t, std::shared_ptr<FinishedJob> job);
	std::shared_ptr<IDispatcherStrategy>  createStrategy(DISPATCHER_STRATEGY assignmentAlgorithm);

};
#endif
