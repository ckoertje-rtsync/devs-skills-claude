//CPP:qss/qss2_advanced.cpp
#if !defined qss2_advanced_h
#define qss2_advanced_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "math.h"
#include "string.h"
#include "qss/qsstools.h"
#include "sinks/ParameterReader.h"

#include "root_simulator.h"
#include "stdio.h"

class qss2_advanced: public Simulator {
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

	// qss plot
	long int foutput;
	char PlotFile[128];
	
	public:
		qss2_advanced(const char *n): Simulator(n) {};
		void init(double, ...);
		double ta(double t);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
		void exit();
};
#endif
