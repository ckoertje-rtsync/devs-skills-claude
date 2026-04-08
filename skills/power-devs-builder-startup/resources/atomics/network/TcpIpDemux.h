//CPP:network/TcpIpDemux.cpp
#if !defined TcpIpDemux_h
#define TcpIpDemux_h

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
* Demultiplexes a packets using the TCP getIpSrc.
*
**/
class TcpIpDemux: public BaseSimulator {

	//parameters
	std::string ipLabels[10]; // TODO: better to use a Hash<label, port>
	const int N=4; // change it if more needed

	// state variables
	std::queue<std::shared_ptr<NetworkPacket>> packetBuffer;

	public:
		TcpIpDemux(const char *n): BaseSimulator(n) {};
		void init(double, ...);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
};
#endif
