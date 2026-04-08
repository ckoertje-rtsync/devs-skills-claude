#ifndef Generator_h
#define Generator_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "general/BaseSimulator.h"

/**
 * Generates events with a period following the configured distribution. Event value is always 0
 */
class Generator: public BaseSimulator {
	// Parameters
	std::shared_ptr<IDistributionParameter> period;

	double output[10] = {0};

public:
	Generator(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
};
#endif
