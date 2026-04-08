#if !defined hybridReadQDelay_h
#define hybridReadQDelay_h

#include "simulator.h"
#include "event.h"

#include "qss/qsstools.h"
#include "NetworkData/NetworkPacket.h"
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

/**
 * This model reads a continuous signal (representing a delay) and sets it to arriving packets (p.hybridQDelay).
 * The delay can be updated by inport1 (QSS 1 to 4).
 *
 */
class hybridReadQDelay: public BaseSimulator {

// Parameters
double delay_s[10] = {0}; // can be updated from inport1

// states
std::shared_ptr<NetworkPacket> packetToForward = nullptr;


public:
	hybridReadQDelay(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
};
#endif
