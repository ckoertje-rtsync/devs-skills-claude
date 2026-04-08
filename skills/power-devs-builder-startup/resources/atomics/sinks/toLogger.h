//CPP:sink/logtoscilabtool.cpp
//CPP:sink/toLogger.cpp
#if !defined toLogger_h
#define toLogger_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "general/BaseSimulator.h"

/**
 * Losg QSS signals (values and time ) using the configured logger.
 *
 */
class toLogger: public BaseSimulator {

public:
	toLogger(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
};
#endif
