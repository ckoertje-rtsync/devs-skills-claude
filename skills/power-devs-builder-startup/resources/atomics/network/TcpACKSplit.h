#if !defined TcpACKSplit_h
#define TcpACKSplit_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"

/**
* In0: Incoming packets
* out0: ACK packets
* out1: other packets
*
* Split incoming packets: ACKs are sent on port0, other on port1
**/
class TcpACKSplit: public BaseSimulator {

	// state variables
	std::queue<std::shared_ptr<NetworkPacket>> packetBuffer;

	public:
		TcpACKSplit(const char *n): BaseSimulator(n) {};
		void init(double, ...);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
};
#endif
