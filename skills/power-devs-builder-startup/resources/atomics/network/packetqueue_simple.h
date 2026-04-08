#if !defined packetqueue_simple_h
#define packetqueue_simple_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h" // TODO: needed temprorary because of hardcoded Id
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"

/**
 * Same as packet queue but with a single output port, and does not report size to the outside (should be more preformant.. less evets)
 */
class packetqueue_simple: public BaseSimulator {


std::queue<std::shared_ptr<NetworkPacket>> mypacketqueue;

// Parameters
long maxCapacity_bits=-1; // bits
double ForcedPeriod=-1;

// state variables
long qSize_bits=0; // helper variable to avoid calculating every time

enum SERVERSTATE {SERVERWAITING,SERVERBUSY};
SERVERSTATE serverstate=SERVERBUSY;


public:
	packetqueue_simple(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit(double t);
};
#endif
