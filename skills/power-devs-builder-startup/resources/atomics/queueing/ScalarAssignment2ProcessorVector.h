#if !defined ScalarAssignment2ProcessorVector_h
#define ScalarAssignment2ProcessorVector_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"
#include "vector/VectorialEvent.h"
#include "queueing/Jobs/JobAssignment.h"

#include <queue>


class ScalarAssignment2ProcessorVector: public BaseSimulator {

	// Parameters
	double sigma;
	int index;
	std::queue<std::shared_ptr<VectorialEvent>> outputVectorialEvents;

	// Helpers

public:
	ScalarAssignment2ProcessorVector(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
};
#endif
