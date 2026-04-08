#if !defined FlowRouter_h
#define FlowRouter_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"
#include "signal_bus/net_fluid_flow/IFlow.h"
#include "NetworkData/RouterTable.h"

/**
 * Demultiplexes multiple input fluid-flows into multiple output ports
 *
 * The output port to be used is defined in the routing table, which maps <nodeName, inport, flowId> into --> <outPort>
 */
class FlowRouter: public BaseSimulator {

typedef std::pair<std::shared_ptr<IFlow>, int> PendingFlow; // flow and inport
std::deque<PendingFlow> pendingFlows; // pending flows to forward

RouterTable* table;

public:
	FlowRouter(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit(double t);
private:

};
#endif
