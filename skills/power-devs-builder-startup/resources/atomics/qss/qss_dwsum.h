#if !defined qss_dwsum_h
#define qss_dwsum_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"
#include "sinks/ParameterReader.h"

/**
 * Exactly the same as qss_wsum only that the first parameter indicates the number of inputs.
 * Written to keep qss_wsum for back compatibility in models which already use it
 */
class qss_dwsum: public BaseSimulator {

	static constexpr int MAX_INPUTS=24; // TODO: remove this and receive no parameters. Use K=1 always, so that the for in lambda does not iterate that much

	//states
	double X[MAX_INPUTS][10]; // Todo: use a std::vector to receive an arbitrary number of signals
	int order;

	//parameters
	double K[MAX_INPUTS];
	int n;

	//outputs
	double y[10];

public:
	qss_dwsum(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	//	void exit();
};
#endif
