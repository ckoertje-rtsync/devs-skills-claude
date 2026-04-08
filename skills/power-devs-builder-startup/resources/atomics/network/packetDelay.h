//CPP:network/packetDelay.cpp
//CPP:qss/qsstools.cpp
#if !defined packetDelay_h
#define packetDelay_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"
#include "NetworkData/NetworkPacket.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

/**
 * This model applies a fix delay (not depending on packetSize) to incoming packets.
 * The delay is set as a parameter and can be updated by inport1 (QSS 1 to 4).
 *
 * kChannel.h, packetServer.h and packetChannel.cpp all consider a delay which is applied to all packets ONCE THE PREVIOUS PACKET IS SENT.
 * This is wrong, as the delay should be considered once the previous packet serviceTime is over (without its delay).
 * Solution: split these models in 2 models. 'packetBandwidthDelay' that applies the serviceTime = packetSize/BW. And 'packetDelay' that applies the propagation delay (arrivalTime+delay)
 *
 */
class packetDelay: public BaseSimulator {

std::deque<std::pair<double, std::shared_ptr<NetworkPacket>>> packetOutputTimes;

// Parameters
double delay_s[10] = {0}; // can be updated from inport1

// counters
int arrived = 0;
int departed= 0;

// states

public:
	packetDelay(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit(double t);

};
#endif
