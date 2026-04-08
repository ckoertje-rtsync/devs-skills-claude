#if !defined packetgen_h
#define packetgen_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"


#include "NetworkData/NetworkPacket.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "NetworkData/Protocol/DummyProtocol.h"
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

/**
 * Generates packets using 2 distributions read from parameters: 1) for the packetSize 2) for the interleave time between each packet generation
 * inPort0: can receive events that force the generation of a new packet the moment of the event arrival (can be used to generate packets following an outside source distribution)
 * outPort0: generated packets
 *
 */

class packetgen: public BaseSimulator {

// Parameters
std::string flowId;
std::shared_ptr<IDistributionParameter> period;
std::shared_ptr<IDistributionParameter> packetSize;
double packetOverheadSize;
std::deque<double> startStopTimes; // times at which to start and stop. Ej: [start at 0, stop at 3, start at 5, stop 10, start 11]
bool generationStopped=false; // flag to ignore incoming messages if generation is stopped

//std::shared_ptr<NetworkPacket> p;
double counter;


public:
	packetgen(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit(double t);
};
#endif
