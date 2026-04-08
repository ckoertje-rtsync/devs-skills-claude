//CPP:qss/qsstools.cpp
//CPP:hybridQueue_mab/implicitDelayQSS.cpp
//CPP:hybridQueue_mab/Polynomio.cpp

#if !defined implicitDelayQSS_h
#define implicitDelayQSS_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include <deque>

#include "general/BaseSimulator.h"
#include "qsstools.h"
#include "Polynomio.h"
#include "boost/format.hpp"

/*
 * calculates the delayedInput d(t) so that d(t+tau(t)) = y(t)
 * inPort1: tau(t)
 * inPort2: y(t)
 * outport: d(t+tau(t))=y(t)
 */
class implicitDelayQSS: public BaseSimulator {
	//states
	std::shared_ptr<Polynomio> delay = nullptr; // delayPolynomio  tau(t)
	std::shared_ptr<Polynomio> input = nullptr; // inputPolynomio  y(t)

	//output
	std::deque<std::pair<double, std::shared_ptr<Polynomio>>> programmedOutputs; // as delays and inputs arrive, we program the future outputs d(t+tau(t))=y(t)
	double outputSignal[10]={0};

public:
	implicitDelayQSS(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	//	void exit();

private:

	double nextSigma(double t);
	void advanceSignals(double dt);
	std::shared_ptr<Polynomio> calculateDelaySignal();
};

#endif
