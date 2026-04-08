//CPP:sink/logtoscilabtool.cpp

//CPP:hybrid/packethybridflowprop.cpp
#if !defined packethybridflowprop_h
#define packethybridflowprop_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "stdevstool.h"
#include "packettool.h"
#include "sink/logtoscilabtool.h"
#include "qss/qsstools.h"


class packethybridflowprop: public Simulator { 
// Declare the state,
// output variables
// and parameters

aLoggerToScilab *myLoggerToScilab;

Packet *p;

double sigma;

double fluidpackets;
double prop[10];












public:
	packethybridflowprop(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
