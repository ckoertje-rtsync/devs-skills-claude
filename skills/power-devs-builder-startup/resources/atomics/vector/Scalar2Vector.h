#if !defined Scalar2Vector_h
#define Scalar2Vector_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "VectorialEvent.h"
#include "sinks/ParameterReader.h"

/**
 * Assumes the event has a double[10] as value. Can not use it for general purposes
 */
class Scalar2Vector: public Simulator {

	double sigma;
	int index;

	VectorialEvent vec;

public:
	Scalar2Vector(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
