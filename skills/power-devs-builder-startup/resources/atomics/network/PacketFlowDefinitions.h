//CPP:network/PacketFlowDefinitions.cpp

#ifndef PacketFlowDefinitions_H_
#define PacketFlowDefinitions_H_

#include <memory>
#include <stdlib.h>
#include <deque>

#include "NetworkData/Flow.h"
//#include "NetworkData/Route.h"

class PacketFlowDefinitions {

    static constexpr const char* PARAMETER_PACKET_FLOW = "PacketFlowNames";
//    static const std::string PARAMETER_PACKET_FLOW = "PacketFlowNames";
private:
	static std::multimap<std::string, std::shared_ptr<Flow>> Flows; // TODO: use a multimap to allow for several flow from a single server

	static void defineFlows();
public:

	static void addFlow(std::shared_ptr<Flow> flow);
	static void addFlow(const std::string& flowName, std::vector<double> startStopTimes, int typeOfService, std::shared_ptr<IDistributionParameter> period, std::shared_ptr<IDistributionParameter> size);
//	static void addFlow(const std::string& flowName, std::vector<double> startStopTimes, int typeOfService, std::shared_ptr<IDistributionParameter> period);

	/**
	 * Get all the flows that has the sourceName as the first host in the route
	 */
	static std::vector<std::shared_ptr<Flow>> getSourceFlows(const std::string& sourceName);\

	/**
	 * Get the flow with the give name
	 */
	static std::shared_ptr<Flow> getFlow(const std::string& flowName);
};



#endif /* PacketFlowDefinitions_H_ */
