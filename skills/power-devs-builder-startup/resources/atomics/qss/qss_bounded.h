//CPP:qss/qss_bounded.cpp
#if !defined qss_bounded_h
#define qss_bounded_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "math.h"
#include "string.h"
#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"

class qss_bounded: public Simulator {
// Declare here the state, output
// variables and parameters
double X[10],q[10];
double sigma;
enum BoundaryCondition { LOWER_BOUND, NO_BOUND };
BoundaryCondition boundaryState;

double qs,qi;

//parameters
double dQ;
double dQmin,dQrel;
double lowerBound;


double eps,ep;
int band;

double y[10]; //output






















public:
	qss_bounded(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
