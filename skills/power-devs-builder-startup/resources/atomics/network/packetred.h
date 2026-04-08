//CPP:network/packetred.cpp
#if !defined packetred_h
#define packetred_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "hybrid/stdevstool.h"
#include "NetworkData/NetworkPacket.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

class packetred: public BaseSimulator {
#define WAITINGFORPACKET 0
#define T_EMITTINGPACKET 1
#define T_NOTIFYINGDROP  2

	// parameters
	int minTH ;
	double PminTH ;
	int maxTH ;
	double PmaxTH ;

	// state variables
	double dropped[10];
	double measuredQueueState; // Matias fix: previously using int (drop_prob calculating is different and affects simulation)
	double drop_prob;
	int phase;
	std::shared_ptr<NetworkPacket> p;

public:
	packetred(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	//	void exit(double t);
};
#endif
