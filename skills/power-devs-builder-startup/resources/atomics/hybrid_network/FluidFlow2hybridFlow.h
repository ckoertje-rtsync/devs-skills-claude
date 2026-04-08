#if !defined FluidFlow2hybridFlow_h
#define FluidFlow2hybridFlow_h

#include "simulator.h"
#include "event.h"

#include "hybrid/stdevstool.h"
#include "sink/logtoscilabtool.h"
#include "qss/qsstools.h"
#include "general/BaseSimulator.h"

#include "hybrid_network/hybridFlow.h"

/*
 * Encapsulates FluidFlows within a HybridFlow
 * Receives FluidFlows (inPort0).  Outputs HybridFlows (outPort0) without any packet  and the given fluidFlow
 */
class FluidFlow2hybridFlow: public BaseSimulator {
    // State variables for the fluid-flow
    std::queue<std::shared_ptr<FluidFlow>> fluidQueue; // enqueue flows so that we dont loose them if they arrive in the same t

public:

    FluidFlow2hybridFlow(const char *n): BaseSimulator(n) {};
    void init(double, ...);
    void dint(double);
    void dext(Event , double );
    Event lambda(double);
};
#endif
