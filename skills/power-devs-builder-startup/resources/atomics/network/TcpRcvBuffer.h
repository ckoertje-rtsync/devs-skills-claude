#if !defined TcpRcvBuffer_h
#define TcpRcvBuffer_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "general/BaseSimulator.h"

/**
 *  Buffers out-of-order TCP packets and forwards them as they are ordered.
 *  Discards duplicated TCP packets.
 *  Demultiplex incoming packets:
 *
 * 	In0: Incoming packets
 * 	out0: non-tcp packets (forwarded immediately)
 * 	out1: all TCP packets (forwarded immediately, usually for receiving side to be able to send ACKs)
 * 	out2: TCP packets delivered in ordered, removing duplicates (forwarding in order of SEQ, as needed by layer 7)
 *
**/
class TcpRcvBuffer: public BaseSimulator {

	enum OUTPORTS { NON_TCP_OUTPORT = 0, ALL_TCP_OUTPORT = 1, ORDERED_TCP_OUTPORT = 2 };
	struct outgoingPacket {
		int outgoingPort;
		std::shared_ptr<NetworkPacket> packet;
	};

	// Parameters
	int MSS; // TCP.MSS needed to increase expected SEQ number

	// state variables
	std::map<uint /*SEQ*/, std::shared_ptr<NetworkPacket>> oooBuffer; // TCP out-of-order buffer. We use std::map as a sorted (RBT) structure
	std::queue<outgoingPacket> outputPackets; // Packets which are ready to be forwarded out

	uint nextExpectedSeqNumber;

	public:
		TcpRcvBuffer(const char *n): BaseSimulator(n) {};
		void init(double, ...);
		void dint(double);
		void dext(Event , double );
		Event lambda(double);
	private:
		void proccessTcpPacket(std::shared_ptr<NetworkPacket> packet, double t);

};
#endif
