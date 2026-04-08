#if !defined hybridMergeFluidIntoPacket_h
#define hybridMergeFluidIntoPacket_h

#include "simulator.h"
#include "event.h"

#include "hybrid/stdevstool.h"
#include "general/BaseSimulator.h"
#include "qss/qsstools.h"
#include "network/NetworkData/NetworkPacket.h"
#include "signal_bus/net_fluid_flow/FluidFlow.h"


/*
 * Applies Fluid-Flow metrics to incoming packets
 *
 * inPort0: discrete packets
 * inPort1: FluidFlow QSS signal
 *
 * For each incoming packet is it affected as follows:
 * Discards packets according to the given probability given by FluidFlow.rate / FluidFlow.dropRate
 * Applies a delay according to FluidFlow.delay
 */
class hybridMergeFluidIntoPacket: public BaseSimulator {
	// parameters

	// state variables
	std::queue<std::pair<std::shared_ptr<NetworkPacket>, double>> pendingPackets; // packets pending to be sent and their corresponding output time
	double rate[10]={0};
	double drops[10]={0};
	double delay[10]={0};

	//counters
	std::pair<int, int> a;

public:
	hybridMergeFluidIntoPacket(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
private:
	void setSigma(double t);
	void updateSignals(double t, std::shared_ptr<FluidFlow> fluidFlow);
};
#endif
