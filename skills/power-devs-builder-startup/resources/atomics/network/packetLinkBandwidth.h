//CPP:network/packetLinkBandwidth.cpp
#if !defined packetLinkBandwidth_h
#define packetLinkBandwidth_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "NetworkData/NetworkPacket.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

/**
 * kChannel.h, packetServer.h and packetChannel.cpp all consider a delay which is applied to all packets ONCE THE PREVIOUS PACKET IS SENT.
 * This is wrong, as the delay should be considered once the previous packet serviceTime is over (without its delay).
 * Solution: split these models in 2 models. 'packetLinkBandwidth' that applies the serviceTime = packetSize/BW. And 'packetLinkDelay' that applies the delay (arrivalTime+delay)
 */
class packetLinkBandwidth: public BaseSimulator {

std::deque<std::shared_ptr<NetworkPacket>> packetQueue;

// Parameters
double bw_bit_s;


//states 


public:
	packetLinkBandwidth(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit(double t);

private:
	double getServiceTime(std::shared_ptr<NetworkPacket> packet);
};
#endif
