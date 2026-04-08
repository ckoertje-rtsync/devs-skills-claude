//CPP:network/packetqueue.cpp
#if !defined packetqueue_h
#define packetqueue_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "NetworkData/NetworkPacket.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "NetworkData/Protocol/TcpIpProtocol.h" // TODO: needed temprorary because of hardcoded Id
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"

class packetqueue: public BaseSimulator {

//aMap myFields;  TODO: Matias removed it to migrate to NetworkPacket, was it needed?
//aMapIter mapIt; TODO: Matias removed it to migrate to NetworkPacket, was it needed?
//protocol myprotocol; TODO: Matias removed it to migrate to NetworkPacket, was it needed?

//aProtocolList myprotoStack; TODO: Matias removed it to migrate to NetworkPacket, was it needed?
//aProtocolListIter myprotoStackit; TODO: Matias removed it to migrate to NetworkPacket, was it needed?

//aPacketQueue myPacketQueue;
std::queue<std::shared_ptr<NetworkPacket>> myPacketQueue;

// Parameters
long maxCapacity_bits=-1; // bits
double ForcedPeriod=-1;

// state variables
double sizeout[10]={0};
long qSize_bits=0; // helper variable to avoid calculating every time

enum STATE {WAITING,t_NOTIFYING,t_READYTOEMIT};
STATE mystate=WAITING;
enum SERVERSTATE {SERVERWAITING,SERVERBUSY};
SERVERSTATE serverstate=SERVERBUSY;


public:
	packetqueue(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit(double t);
};
#endif
