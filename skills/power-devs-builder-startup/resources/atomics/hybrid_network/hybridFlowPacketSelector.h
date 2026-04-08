#if !defined HybridFlowPacketSelector_h
#define HybridFlowPacketSelector_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include <math.h>       /* isinf */

#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"
#include "hybrid_network/hybridFlow.h"


/**
 * Outputs only the packet
 */
class hybridFlowPacketSelector: public BaseSimulator {

    // state variables
    std::queue<std::shared_ptr<NetworkPacket>> packetQueue; // enqueue packet so that we dont loose them if they arrive in the same t

public:
    hybridFlowPacketSelector(const char *n): BaseSimulator(n) {};
    void init(double, ...);
    void dint(double);
    void dext(Event , double );
    Event lambda(double);
};
#endif
