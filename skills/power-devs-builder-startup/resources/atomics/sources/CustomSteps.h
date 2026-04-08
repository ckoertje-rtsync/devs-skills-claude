//CPP:sources/CustomSteps.cpp

#if !defined CustomSteps_h
#define CustomSteps_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"

class CustomSteps: public BaseSimulator {
	//  parameters
	std::vector<double> stepTimeChanges;
	std::vector<double> stepValueChanges; // 0 derivative
	std::vector<double> stepValueChanges1; // 1st derivative
	std::vector<double> stepValueChanges2; // 2nd derivative
	std::vector<double> stepValueChanges3; // 3rd derivative

	// state variables
	double y[10];

public:
	CustomSteps(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
};
#endif
