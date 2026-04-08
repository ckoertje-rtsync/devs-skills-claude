//CPP:network/packetRED_AQM_ECN.cpp
#if !defined packetRED_AQM_ECN_h
#define packetRED_AQM_ECN_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "hybrid/stdevstool.h"
#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

/*
 * based on this paper: http://www.icir.org/floyd/papers/early.twocolumn.pdf
 * Similar to packetred.cpp but it calculates the w-avg on each packet arrival (discrete w-avg instead of continuous).
 * It also implements the decrease of the w-avg when the queue is empty for a period of time.
 *
 * TODO: - Drop prob is affected by the number of packets since last discard.
 *       - evaluate if t-q_time is a good f
 *       - using a queueSize in bytes has advantages over using queueSize in packets. The dropProb should be proportional to the packetSize.
 */
class packetRED_AQM_ECN: public BaseSimulator {

	std::shared_ptr<NetworkPacket> p = NULL;

	// parameters
	int minTH ;
	double pMinTH ;
	int maxTH ;
	double pMaxTH ;
	double alpha;

	// state variables
	double sampledQueueSize = 0; // Currently implemented as a sampled weighted average
	double drop_prob = 0;
	enum State { WAITING_FOR_PACKET, T_EMITTING_PACKET};
	State phase = WAITING_FOR_PACKET;
	double q_time = 0; // start of the queue idle time
	double queueSize = 0; // current queue size

	// counters
	double marked = 0;

public:
	packetRED_AQM_ECN(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	//double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	//void exit(double t);
private:
	/* this is implements the weighted sampled average */
	void updateMeasuredQueueSize(double t, double newQueueSizeSample);

	/* this is implements the AQM dropProb function  */
	void updateDropProbability();
};
#endif
