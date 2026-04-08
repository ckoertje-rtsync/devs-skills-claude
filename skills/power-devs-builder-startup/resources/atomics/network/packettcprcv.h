#if !defined packettcprcv_h
#define packettcprcv_h

#include "math.h"
#include "simulator.h"
#include "event.h"

#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"
#include "NetworkData/Protocol/RoutingProtocol.h"

class packettcprcv: public BaseSimulator {
    #define SPECIAL_FLOW_ID_KEEP "KEEP" // keeps the same flowId as received packets
	#define SPECIAL_FLOW_ID_INVERT "INVERT" //  if flowId equals <SRC>_to_<DST>, it invert it to <DST>_to_<SRC>. Otherwise, it fails
    #define SPECIAL_FLOW_ID_TCP_DST_SRC "TCP_DST_SRC" //  flowId equals <DST_IP>_to_<SRC_IP>



	// Parameters
	std::string flowId; // FlowID to use for ACK packets. Can have some special strings: see defines SPECIAL_FLOW_ID_*
	int MSS; // to increase expected SEQ number
	std::string ipReceiver; // my IP to validate incoming packets
	int portReceiver; // my port to validate incoming packets


	// State variables
	std::string ipSender = ""; // learned from the first packet to validate the following packets
	int portSender = -1; // learned from the first packet to validate the following packets

	std::shared_ptr<NetworkPacket> arrivedPacket;

	int DATA_SEQ_NUMBER;
	int SEQ_NEXT_Expected;
	int SEQ_LAST_Valid;
	int SEQ_DUP;
	int SEQ_NXT;
	int ReceivedACKedFromPeerLayer;
	int SendACKtoPeerLayer;
	double DATA_TCP_SND_TIMESTAMP;

public:
	packettcprcv(const char *n): BaseSimulator(n) {};
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
