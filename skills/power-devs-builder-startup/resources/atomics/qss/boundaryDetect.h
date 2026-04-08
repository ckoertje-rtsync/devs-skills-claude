//CPP:qss/qsstools.cpp
//CPP:qss/boundaryDetect.cpp
#if !defined boundaryDetect_h
#define boundaryDetect_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"

/**
 * Produces events when u<=level (if highDetection==false).
 * Produces events when u>=level (if highDetection==true).
 *
 * It is very similar to cross_detect, but this one produces just one event within the same t. Also it produces events while u *= level, not only in the crossing.
 * (QSS1 to 4)
 */
class boundaryDetect: public Simulator {
	//states
	double sigma;
	double u[10];
	double lastT = -1;
	bool highDetection; // if true, detects higher values than 'level'. If low detects lower values values than 'level'

	//output
	double y[10];

	//parameters
	double level,outval;

public:
	boundaryDetect(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
