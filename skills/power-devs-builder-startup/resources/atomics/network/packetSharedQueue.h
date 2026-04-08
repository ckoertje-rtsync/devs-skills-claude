#if !defined packetSharedQueue_h
#define packetSharedQueue_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include <queue>
#include <unordered_map>
#include <set>

#include "NetworkData/NetworkPacket.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"

/**
 * Represents a shared buffered within a switch/router.
 * There is a fixed buffer per port (per_port_capacity_bits), which is used only by packets destined to that port (same as several packetQueue.cpp)
 * Additionaly, there is a shared buffered (shared_capacity_bits), which is used by all packets which does not fit in their corresponding fixed queue.
 * Packets are discarded if 1) the fixed buffer is full AND 2) the shared buffer is full
 * Packets are always severed in a FIFO order respecting egress ports request orders. Once a packet is in a given buffer (fixed or shared) stays in that same buffer until sent
 * 
 * Other features implemented:
 *   - (buffer_cell_size_bits != -1) Buffer memory can be setup to be splitted in 'cells' (e.g. as implemented in Juniper ToR QFX5100). The cell size determines the minimum space a packet can use, thus a packet can effectively use more buffer than its size. 
 *   - (port_max_shared_buffer_use_bits) # A single outport can be limited in the amount of shared buffer it can use (e.g. as implemented in Juniper ToR QFX5100). There is a 'dynamic_threadhold' parameter that allows setting the percentage of the shared buffer a single port can use (default is 50%). See: https://gitlab.cern.ch/atlas-tdaq-networking/phase2-tor-buffer-test/-/blob/b122a28d0823716335a34e5a5ad8db5913cb0917/james_tools/dynthresh-calculator/5100.txt
 *
 * In port <N (even) > (0,2,4,...): request to dequeue from egress port N/2
 * In port <M (odd) (1,3,5,...: packet destined to egress port N-1/2
 *
 * Outport P: packet sent to egress port P
 */
class packetSharedQueue: public BaseSimulator {
	struct QueuedPacket{
		QueuedPacket(std::shared_ptr<NetworkPacket> packet, bool in_shared_buffer) :
			packet(packet), in_shared_buffer(in_shared_buffer){};

		std::shared_ptr<NetworkPacket> packet;
		bool in_shared_buffer; // true if the packet was queued in the shared buffer
	};

	struct OutputQueue{
		std::queue<std::shared_ptr<QueuedPacket>> queue; // queues of packets to be sent
		long port_size_bits = 0;    // amount of per port buffer used by this out port		
		long shared_size_bits = 0; // amount of shared buffer used by this out port
	};

// Parameters
long per_port_capacity_bits=-1; // bits
long shared_capacity_bits=-1; // bits
long buffer_cell_size_bits=-1; // bits
long port_max_shared_buffer_use_bits=-1; // bits

// state variables
std::unordered_map<int/*outport*/, OutputQueue> queues;
std::set<int/*outport*/> pending_requests; // if pending_requests[i] is defined, then a request to dequeue a packet arrived but was not fulfilled, as soon as a packet arrived for that queue it needs to be sent out (does not allow multiple pending request on the same queue)
std::queue<int/*outport*/> pending_sends; // queue of outports which need to send outband packets
long shared_buffer_size_bits=0;

/*********** DEBUG *********************/
long max_single_port_shared_buffer_use_bits=-1; // bits
/*********** DEBUG *********************/


public:
	packetSharedQueue(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit(double t);
private:
	long getBufferingSize(long packetSize_bits) const;
	bool hasBuffer(const OutputQueue& portQueue, long buffering_size) ;
};
#endif

