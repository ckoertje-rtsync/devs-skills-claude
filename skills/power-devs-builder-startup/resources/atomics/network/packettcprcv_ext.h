#if !defined packettcprcv_ext_h
#define packettcprcv_ext_h

#include "math.h"

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"
#include "NetworkData/Protocol/RoutingProtocol.h"

/**
 * Exteded TCP receiver
 * - Added delayACK
 */
class packettcprcv_ext: public BaseSimulator {

	// Parameters
	std::string ipSender;
	int portSender;
	std::string ipReceiver;
	int portReceiver;
	uint MSS;
	double delayACK; // time to delay ACKs (in sec). This is the maximum time an ACK will be delayed, once the first ACKs is not hold all the rest will go accumulated into a single ACK.

	// State variables
	std::shared_ptr<NetworkPacket> arrivedPacket;

	uint DATA_SEQ_NUMBER;
	uint SEQ_NEXT_Expected;
	uint SEQ_DUP;
	uint SEQ_NXT;
	uint ReceivedACKedFromPeerLayer;
	uint SendACKtoPeerLayer;
	double DATA_TCP_SND_TIMESTAMP;

public:
	packettcprcv_ext(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	//	void exit(double t);
private:
	bool validatePacket(std::shared_ptr<NetworkPacket>& packet);
};
#endif
