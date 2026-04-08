//CPP:hybrid/packetcounter.cpp
#if !defined packetcounter_h
#define packetcounter_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "stdio.h"
#include "string.h"


class packetcounter: public Simulator { 

double Sigma;
double y[10];
double total;










public:
	packetcounter(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
