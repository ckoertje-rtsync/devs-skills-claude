#ifndef PacketFlowGenerator_h
#define PacketFlowGenerator_h


#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include <boost/tokenizer.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/algorithm/string.hpp>

#include "sinks/ParameterReader.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "hybrid/stdevstool.h"

#include "NetworkData/NetworkPacket.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
//#include "NetworkData/Protocol/RoutingProtocol.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
#include "NetworkData/Protocol/QualityOfServiceProtocol.h"
#include "general/BaseSimulator.h"
#include "PacketFlowDefinitions.h"

class PacketFlowGenerator: public BaseSimulator {

	// Parameters
	std::deque<double> startStopTimes;
	std::string flowName;
	std::shared_ptr<Flow> flow;

	std::shared_ptr<IDistributionParameter> generationPeriod;
	std::shared_ptr<IDistributionParameter> packetSize_bits;

	// State variables
	int queuedGenerations = 0;
	bool generationStopped = false; // helper for startStopTimes

	// Log counters
	int generatedPackets = 0;

public:
	PacketFlowGenerator(const char *n): BaseSimulator(n) {};
	void init(double, ...);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
private:
	int getNextPacketSize_bits();

	std::shared_ptr<NetworkPacket> createPacket(double t);
};
#endif
