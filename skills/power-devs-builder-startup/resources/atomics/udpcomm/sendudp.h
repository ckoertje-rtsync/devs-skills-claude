//CPP:dover/sendudp.cpp
#if !defined sendudp_h
#define sendudp_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"

class sendudp: public Simulator { 
	// Declare the state,
	// output variables
	// and parameters
	double sigma;
	//parametros
	int port;
	char ip[16];
	
	//output
	double y[10];
	
	public:
		sendudp(const char *n): Simulator(n) {};
		void init(double, ...);
		double ta(double t);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
		void exit();
};
#endif
