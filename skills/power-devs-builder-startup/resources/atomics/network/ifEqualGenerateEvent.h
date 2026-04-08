//CPP:hybridQueue_mab/ifEqualGenerateEvent.cpp
#if !defined ifEqualGenerateEvent_h
#define ifEqualGenerateEvent_h

#include "simulator.h"
#include "event.h"

#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"


/**
 * Similar  to cross-detect, but outputs only if u0==LEVEL (not every time it crosses)
 * u1 can set the value of the generated output (0 by default)
 */
class ifEqualGenerateEvent: public BaseSimulator {
//states
double u[10]={0};

//output
double y[10]={0};

//parameters
double level;

public:
	ifEqualGenerateEvent(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
};
#endif
