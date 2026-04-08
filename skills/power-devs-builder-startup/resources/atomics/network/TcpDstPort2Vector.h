#if !defined TcpDstPort2Vector_h
#define TcpDstPort2Vector_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "vector/VectorialEvent.h"
#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"

/**
 * Forwards a VectorialEvent with index equal to the TCP destination port.
 */
class TcpDstPort2Vector: public BaseSimulator {

	// State variables
	int index;
	std::queue<std::shared_ptr<VectorialEvent>> outputVectorialEvents;


public:
	TcpDstPort2Vector(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
};
#endif
