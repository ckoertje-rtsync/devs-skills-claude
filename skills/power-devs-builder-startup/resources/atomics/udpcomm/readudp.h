//CPP:dover/sensor.cpp
//CPP:dover/readudp.cpp
#if !defined readudp_h
#define readudp_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

class readudp: public Simulator { 
	// Declare the state,
	// output variables
	// and parameters
	
	// parameters
	int port;
	int flag = 0;
	
	// states
	double sigma;
	double u[10]; 
	
	//salida
	double y[10];
	public:
		readudp(const char *n): Simulator(n) {};
		void init(double, ...);
		double ta(double t);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
		void exit();
};
#endif
