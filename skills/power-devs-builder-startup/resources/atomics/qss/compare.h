//CPP:qss/qsstools.cpp
//CPP:qss/compare.cpp
#if !defined compare_h
#define compare_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include <limits>
#include <cmath>
#include <iostream>

#include "qsstools.h"

#include "sinks/ParameterReader.h"

class compare: public BaseSimulator {
//states
double tcross;
double u[2][10],du[10];
enum State {LOW, UP, UNDEFINED};
State sw;

//output
double y[10];

//parameters
double low,up;














public:
	compare(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
};
#endif
