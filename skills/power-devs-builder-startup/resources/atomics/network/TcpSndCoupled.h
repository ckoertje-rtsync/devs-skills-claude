//CPP:network/TcpSndCoupled.cpp
//CPP:network/NetworkData/NetworkPacket.cpp
//CPP:network/NetworkData/Protocol/RoutingProtocol.cpp"
//CPP:general/BaseSimulator.cpp
#if !defined TcpSndCoupled_h
#define TcpSndCoupled_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include <sources/CustomSteps.h>
#include <network/packettcpsnd.h>
#include <network/PacketFlowGenerator.h>
#include <network/packetLogger.h>
#include <network/packetqueue.h>

class TcpSndCoupled: public Coupling {
	public:
		TcpSndCoupled(const char *name): Coupling(name) {
			createModel();
		};

	private:
		void createModel();
};
#endif
