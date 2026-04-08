//CPP:sinks/to_csv_vectorial.cpp
#if !defined to_csv_vectorial_h
#define to_csv_vectorial_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include "string.h"
#include "vector/vector.h"
#include "general/BaseSimulator.h"
#include <string>

/**
 * Write to cvs the QSS events arriving as vectorial events
 */
class to_csv_vectorial: public BaseSimulator {


public:
	to_csv_vectorial(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
};
#endif
