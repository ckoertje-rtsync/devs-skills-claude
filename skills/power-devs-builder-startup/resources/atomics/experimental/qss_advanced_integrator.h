//CPP:qss/qsstools.cpp
//CPP:qss/qss_advanced.cpp
//CPP:qss/qss2_advanced.cpp
//CPP:qss/qss3_advanced.cpp
//CPP:qss/qss4_advanced.cpp
//CPP:qss/qss_advanced_integrator.cpp
#if !defined qss_advanced_integrator_h
#define qss_advanced_integrator_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "math.h"
#include "string.h"
#include "qss/qsstools.h"
#include "experimental/qss_advanced.h"
#include "experimental/qss2_advanced.h"
#include "experimental/qss3_advanced.h"
#include "experimental/qss4_advanced.h"

class qss_advanced_integrator: public Simulator {

	// Declare here the state, output
	// variables and parameters
	
	char* Method;
	Simulator *solver;
	enum { QSS, QSS2, QSS3, QSS4, 
		BQSS,CQSS,LIQSS,LIQSS2,
		LIQSS3 } met;
	
	public:
		qss_advanced_integrator(const char *n): Simulator(n) {};
		void init(double, ...);
		double ta(double t);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
		void exit();
};
#endif
