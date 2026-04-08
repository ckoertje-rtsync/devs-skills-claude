//CPP:network/packetdemux.cpp
#if !defined packetdemux_h
#define packetdemux_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "NetworkData/NetworkPacket.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"

class packetdemux: public BaseSimulator {
	//parameters
	std::string Labels[10]; // TODO: better to use a Hash<label, port>
	int n;

	std::queue<std::shared_ptr<NetworkPacket>> packetBuffer;

	public:
		packetdemux(const char *n): BaseSimulator(n) {};
		void init(double, ...);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
};
#endif
