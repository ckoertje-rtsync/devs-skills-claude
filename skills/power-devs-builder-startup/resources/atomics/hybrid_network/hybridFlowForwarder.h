#if !defined hybridFlowForwarder_h
#define hybridFlowForwarder_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include <math.h>       /* isinf */

#include "qss/qsstools.h"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"
#include "hybrid_network/hybridFlow.h"


/**
 * This model doesnt really do mach, it just queues and forward in the same t. It is useful to enforce low priorities in the hybrid queue
 */
class hybridFlowForwarder: public BaseSimulator {

    // state variables
    std::queue<std::shared_ptr<hybridFlow>> packetQueue; // enqueue packet so that we dont loose them if they arrive in the same t

public:
    hybridFlowForwarder(const char *n): BaseSimulator(n) {};
    void init(double, ...);
    void dint(double);
    void dext(Event , double );
    Event lambda(double);
};
#endif
