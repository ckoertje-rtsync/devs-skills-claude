//CPP:hybridQueue_mab/logicalGenerateEventCrossTrue.cpp
#if !defined logicalGenerateEventCrossTrue_h
#define logicalGenerateEventCrossTrue_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"


/**
 * Generates and event if input>LEVEL
 * Similar  to cross-detect and logicalGenerateEventCrossTrue.h.
 */
class logicalGenerateEventCrossTrue: public BaseSimulator {
//states
double u[10]={0};
int sw;

//output
double y[10]={0};

//parameters
double level;

public:
	logicalGenerateEventCrossTrue(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
};
#endif
