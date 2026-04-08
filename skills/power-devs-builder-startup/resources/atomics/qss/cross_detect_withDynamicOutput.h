//CPP:qss/qsstools.cpp
//CPP:qss/cross_detect_withDynamicOutput.cpp
#if !defined cross_detect_withDynamicOutput_h
#define cross_detect_withDynamicOutput_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"


class cross_detect_withDynamicOutput: public Simulator {
//states
double sigma;
double u[10];
int sw;

//output
double y[10];

//parameters
double level;

public:
	cross_detect_withDynamicOutput(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
