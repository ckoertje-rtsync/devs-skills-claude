//CPP:continuous/gain_sci.cpp
#if !defined gain_sci_h
#define gain_sci_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"

class gain_sci: public Simulator { 
//states
double sigma;
double u[10];
int order;

//output
double y[10];

//parameters
double K;



public:
	gain_sci(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
