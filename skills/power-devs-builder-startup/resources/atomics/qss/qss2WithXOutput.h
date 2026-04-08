//CPP:qss/qsstools.cpp

//CPP:qss/qss2WithXOutput.cpp
#if !defined qss2WithXOutput_h
#define qss2WithXOutput_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "math.h"
#include "string.h"
#include "qss/qsstools.h"
#include "sinks/ParameterReader.h"

class qss2WithXOutput: public Simulator {
// Declare here the state, output
// variables and parameters
double X[10],q[10];
double sigma;

double qs,qi;

//parameters
double dQ;
double dQmin,dQrel;


double eps,ep;
int band;

double y[10]; //output




















public:
	qss2WithXOutput(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
