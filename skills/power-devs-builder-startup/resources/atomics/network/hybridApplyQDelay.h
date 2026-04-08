#if !defined hybridApplyQDelay_h
#define hybridApplyQDelay_h

#include "simulator.h"
#include "event.h"


#include "qss/qsstools.h"
#include "NetworkData/NetworkPacket.h"
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

/**
 * This model applies the delay that was set in the packet (p.hybridQDelay)
 */
class hybridApplyQDelay: public BaseSimulator {

// states
std::deque<std::pair<double, std::shared_ptr<NetworkPacket>>> packetOutputTimes; // see comment in dext about sorting packets
//std::vector<std::pair<double, Packet*>> packetOutputTimes; // if we want to force the delay disordering packets

// counters
int wrongDelayFixed=0; // to track unexplained behavior

public:
	hybridApplyQDelay(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
private:
	void enqueuePacket(std::shared_ptr<NetworkPacket> p, double outTime);
	std::shared_ptr<NetworkPacket> getNextOutputPacket();
	double getNextOutputTime();
	void dequeuePacket();


};
#endif
