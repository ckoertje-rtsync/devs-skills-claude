//CPP:network/TcpPortDemux.cpp
#if !defined TcpPortDemux_h
#define TcpPortDemux_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"

/**
* In0: Incomming packets
* outN: Outgoing packets
*
* Demultiplexes  packets using the TCP src/dst port.
* outport is calculated as outport=mod(floor(pkt.dstPort/divisor), modulo)
**/
class TcpPortDemux: public BaseSimulator {

	//parameters
	int modulo = 1; // modulo to apply to the port
	int divisor= 1; // divisor to apply to the port
	bool use_dst = true; // either uses getPortDst or getPortSrc

	// state variables
	std::queue<std::shared_ptr<NetworkPacket>> packetBuffer;

	public:
		TcpPortDemux(const char *n): BaseSimulator(n) {};
		void init(double, ...);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
};
#endif
