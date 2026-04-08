//CPP:qss/qsstools.cpp
//CPP:advancedgraphics/gnuplotxy2.cpp
#if !defined gnuplotxy2_h
#define gnuplotxy2_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "root_simulator.h"

#include "random"
#include "stdio.h"
#include "qss/qsstools.h"


class gnuplotxy2: public Simulator { 

	// Declare the state, output
	// variables and parameters
	double sigma;
	int n;
	char printString[1024];
	long int foutput;
	int hasOutput;
	char FName[128];
	double X[2][10];
	int order;


public:
	gnuplotxy2(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
