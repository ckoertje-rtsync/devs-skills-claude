//CPP:qss/qss_delay.cpp
#if !defined qss_delay_h
#define qss_delay_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"


class qss_delay: public BaseSimulator {
// Declare the state, output
// variables and parameters

static constexpr long maxQ = 100000;

//states
double Bt[maxQ];
double Bu[maxQ][4];
long PNi, PNo;
//double sigma;

//output
double y[10];

//parameters
double Delay;




public:
	qss_delay(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
};
#endif
