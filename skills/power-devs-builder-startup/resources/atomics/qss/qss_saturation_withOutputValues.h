//CPP:qss/qsstools.cpp

//CPP:qss/qss_saturation_withOutputValues.cpp
#if !defined qss_saturation_withOutputValues_h
#define qss_saturation_withOutputValues_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"

/**
 * Port0: value to saturate
   Port1: value to output when there is no saturation
   Port2: value to output when there is saturation (currently 0)
 */
class qss_saturation_withOutputValues: public Simulator {
//states
double sigma,tcross;
double u[10] = {0};
double noSaturationOutput[10] = {0}; // Incoming from port1
double saturationOutput[10] = {0}; // Incoming from port2

enum SaturationState {LOW_SATURATION=-1, HIGH_SATURATION=1, NO_SATURATION=0};
SaturationState saturationState; //int sw;

//output
double y[10] = {0};

//parameters
double lowLimit,upperLimit;













public:
	qss_saturation_withOutputValues(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();

private:
	void setOutput();
};
#endif
