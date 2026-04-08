//CPP:network/TcpPortFilter.cpp
#if !defined TcpPortFilter_h
#define TcpPortFilter_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"
#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"

/**
 * Discards packets with <TcpDstPort> < maxPort (continous signal)
 */
class TcpPortFilter: public BaseSimulator {
	//parameters

	// state variables
	std::queue<std::shared_ptr<NetworkPacket>> packetBuffer;
	double maxPort[10]; // continous signal

	public:
		TcpPortFilter(const char *n): BaseSimulator(n) {};
		void init(double, ...);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
};
#endif
