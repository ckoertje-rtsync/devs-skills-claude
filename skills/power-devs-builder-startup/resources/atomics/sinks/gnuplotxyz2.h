//CPP:qss/qsstools.cpp
//CPP:advancedgraphics/gnuplotxyz2.cpp
#if !defined gnuplotxyz2_h
#define gnuplotxyz2_h

#include <random>

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include "root_simulator.h"

#include "stdio.h"
#include "qss/qsstools.h"

class gnuplotxyz2: public Simulator { 
	// Declare the state, output
	// variables and parameters
	double sigma;
	int n;
	char printString[1024];
	long int foutput;
	int hasOutput;
	char FName[128];
	double X[3][10];
	int order;
	public:
		gnuplotxyz2(const char *n): Simulator(n) {};
		void init(double, ...);
		double ta(double t);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
		void exit();
};
#endif
