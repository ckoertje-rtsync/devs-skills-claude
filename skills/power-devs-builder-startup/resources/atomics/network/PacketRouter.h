#if !defined PacketRouter_h
#define PacketRouter_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "hybrid/stdevstool.h"

#include "NetworkData/NetworkPacket.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "NetworkData/Protocol/RoutingProtocol.h"
#include "general/BaseSimulator.h"

#include <deque>

/*
 * Routes based on the routing protocol contained in the packets
 */
class PacketRouter: public BaseSimulator {
	struct outgoingPacket {
		int outgoingPort;
		std::shared_ptr<NetworkPacket> packet;
	};

	// Parameters
	std::queue<outgoingPacket> outputPackets; // use a queue to allow for multiple packets to arrive at the same time

	public:
		PacketRouter(const char *n): BaseSimulator(n) {};
		void init(double, ...);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
	private:

};
#endif
