//CPP:signal/modulator.cpp
#if !defined modulator_h
#define modulator_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "math.h"
#include "sinks/ParameterReader.h"

class modulator: public Simulator { 
// Declare the state,
// output variables
// and parameters
double u,sigma;
long v[100];
int nextport;

double y[10];
double dQ;
int n;
int offset;


public:
	modulator(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
