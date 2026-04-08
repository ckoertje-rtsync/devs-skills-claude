//CPP:hybridQueue_mab/hybridRateToEvents.cpp
//CPP:qss/qsstools.cpp
#if !defined hybridRateToEvents_h
#define hybridRateToEvents_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

/**
 * This model receives a rate in Hz (port1: QSS 1 to 4) and generates output events at the given rate
 */
class hybridRateToEvents: public BaseSimulator {

// Parameters
double rate_hz[10] = {0}; // can be updated from inport1
double delta_min=std::numeric_limits<double>::infinity(); // update rate minimum every delta_min.  (ej: if the rate is too small but growing fast, the rate should be reevaluated periodically)
double normalVariance = NAN; // in case of normal distribution
std::string generationDistribution ;


// state variables
double output[10] = {0}; // can be updated from inport1
bool generateEvent=true; // do not generate event in lambda if it was a forced transition to update rate[0] (delta_min)
double lastEventTime=0;


public:
	hybridRateToEvents(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
private:
	double getStochasticSigma(double mean);
};
#endif
