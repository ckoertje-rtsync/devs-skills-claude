//CPP:sinks/gnuplot.cpp
#if !defined gnuplot_h
#define gnuplot_h

#include <random>

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include "root_simulator.h"

#include "stdio.h"


class gnuplot: public Simulator { 
// Declare the state, output
// variables and parameters
double sigma;
int n;
char printString[1024];
long int foutput;
int hasOutput;
char FName[128];

std::uniform_int_distribution<int> dist;
std::mt19937 rng;









public:
	gnuplot(const char *n): Simulator(n), dist(1,100000), rng(std::random_device()()) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
