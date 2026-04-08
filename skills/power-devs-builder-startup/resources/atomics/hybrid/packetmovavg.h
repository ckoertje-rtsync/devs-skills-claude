//CPP:sink/logtoscilabtool.cpp

//CPP:hybrid/packetmovavg.cpp
#if !defined packetmovavg_h
#define packetmovavg_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"


class packetmovavg: public Simulator { 
// Declare the state,
// output variables
// and parameters

double out_mavg[10];
double alfa ;
double sample ;
double mavg;

double sigma;








public:
	packetmovavg(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
