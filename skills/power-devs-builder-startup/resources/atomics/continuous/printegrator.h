//CPP:Continuous\printegrator.cpp
#if !defined printegrator_h
#define printegrator_h

#include "math.h"
#include "string.h"
#include "simulator.h"
#include "event.h"
#include "stdarg.h"

class printegrator:public Simulator{
// Declare here the state, output
// variables and parameters
double u,mu,pu,X;
double inf;
double sigma;
double y[10]; //output



public:
	printegrator(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
};
#endif
