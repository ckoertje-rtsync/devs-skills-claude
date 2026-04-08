#if !defined JobGenerator_h
#define JobGenerator_h


#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "hybrid/stdevstool.h"

#include "queueing/Jobs/Job.h"

class JobGenerator: public BaseSimulator {

	// Parameters
//	double sigma; Implemented by BaseSimulator
	double startTime = -1;
	int eventsThreshold = -1; // stop after sending eventsThreadhold events
	std::shared_ptr<IDistributionParameter> generationPeriod;
	std::shared_ptr<IDistributionParameter> jobSize;

	// State variables
	int counter = 0;

public:
	JobGenerator(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);  Implemented by BaseSimulator
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
};
#endif
