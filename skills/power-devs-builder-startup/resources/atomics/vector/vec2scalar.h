//CPP:vector/vec2scalar.cpp
#if !defined vec2scalar_h
#define vec2scalar_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "vector.h"
#include "sinks/ParameterReader.h"

class vec2scalar: public Simulator { 
// Declare the state,
// output variables
// and parameters

double sigma;
int index;
double y[10];





public:
	vec2scalar(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
