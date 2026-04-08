//CPP:signal_bus/net_fluid_flow/hybridizedDemultiplex.cpp
//CPP:signal_bus/net_fluid_flow/FluidFlow.cpp
//CPP:signal_bus/net_fluid_flow/FluidFlowRoutingTable.cpp
//CPP:signal_bus/IEntity.cpp
#if !defined hybridizedDemultiplex_h
#define hybridizedDemultiplex_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include <math.h>       /* isinf */
#include "boost/format.hpp"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"
#include "signal_bus/net_fluid_flow/FluidFlow.h"
#include "Packet2hybridFlow.h"

/**
 * Demultiplexes fluid flows detecting the ones that were created by an hybridLink.
 *
 * Hybridized flows are send by outport0, the rest by outport1
 * It is the same an an FluidFlowDeMultiplexer, only that is has the same hardoded base as used by the hybridizationLink (instead of a parameter that has to be set separatly)
 *
 */
class hybridizedDemultiplex: public BaseSimulator {


std::deque<std::shared_ptr<FluidFlow>> pendingFlows; // pending flows to forward

public:
	hybridizedDemultiplex(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
};
#endif
