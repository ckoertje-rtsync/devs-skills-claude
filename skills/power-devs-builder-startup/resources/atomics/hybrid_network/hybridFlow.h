#if !defined hybridFlow_H
#define hybridFlow_H

#include <stdlib.h>
#include <string>

#include "signal_bus/net_fluid_flow/IFlow.h"
#include "signal_bus/net_fluid_flow/FluidFlow.h"
#include "network/NetworkData/NetworkPacket.h"
#include "signal_bus/IEntity.h"

/**
 * Represents hybrid flows which inherit from IFlow and IEntity (to share behaviour)
 * Contains instances of packet and/or FluidFlows. These are Attributes so that they can be selected upon its changes
 *
 */
class hybridFlow :  public IFlow {  // I hate it can not use Hybrid with capital H. PowerDEVS GUI !!!
public:

    hybridFlow(std::shared_ptr<FluidFlow> fluid) : IFlow(fluid->flowId.value),
    packet(nullptr), // the attribute will have updated=false so not to use it
    fluid_flow(fluid){
        this->packet.updated = false; // just in case
    }

    hybridFlow(std::shared_ptr<NetworkPacket> packet) : IFlow(packet->flowId.value),
        packet(packet),
        fluid_flow(nullptr){// the attribute will have updated=false so not to use it
            this->fluid_flow.updated = false; // just in case
        }

    hybridFlow(std::shared_ptr<NetworkPacket> packet, std::shared_ptr<FluidFlow> fluid) : IFlow(packet->flowId.value),
            packet(packet),
            fluid_flow(fluid){
        //        if(packet->flowId.value != fluid->flowId.value){
        //            throw std::runtime_error("hybridFlow :: Attempted to create an hybrid flow with a packet and a fluid with different flowId attributes \n");
        //        }
    }

    Attribute<std::shared_ptr<NetworkPacket>> packet ;
    Attribute<std::shared_ptr<FluidFlow>> fluid_flow;

};


#endif
