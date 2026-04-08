#if !defined qss_signal_filter_h
#define qss_signal_filter_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"

/**
 * Reduces the number of events following QSS error bounds.
 */
class qss_signal_filter: public BaseSimulator {
	//states
	double u[10]; // last input
	double y[10]; // output
	double diff[10]; // auxiliary

	//parameters
	double dQmin,dQrel;

public:
	qss_signal_filter(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	//	void exit();
};
#endif
