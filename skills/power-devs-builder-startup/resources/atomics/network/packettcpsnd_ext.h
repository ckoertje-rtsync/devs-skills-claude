//CPP:network/packettcpsnd_ext.cpp
#if !defined packettcpsnd_ext_h
#define packettcpsnd_ext_h

#include "math.h"
#include "algorithm"

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "NetworkData/NetworkPacket.h"
#include "NetworkData/Protocol/TcpIpProtocol.h"
//#include "packettool.h" // TODO: NetworkPacket is a better implementation but needs to be use in all models (one day)
#include "hybrid/stdevstool.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when using baseSimulator as base class
#include "general/BaseSimulator.h"

/**
 * Extended TCP sender:
 * 	- min RTO
 * 	- single (cumulative/delay) ACK can acknowledge several packets
 */
class packettcpsnd_ext: public BaseSimulator {
protected:
std::deque<std::shared_ptr<NetworkPacket>> SendBuffer;
std::shared_ptr<NetworkPacket> p;    // TODO Matias: does it need to be a class field?
std::shared_ptr<NetworkPacket> pout; // TODO Matias: does it need to be a class field?
std::shared_ptr<NetworkPacket> pack; // TODO Matias: does it need to be a class field?

// Parameters
uint MSS;
std::string ipSender;
int portSender;
std::string ipReceiver;
int portReceiver;
double MIN_RTO;


bool WND_firsttime_SSTHRESH;
bool BUF_Fully_SNT; 
uint BUF_SIZE;
bool WND_Fully_SNT; 
uint WND_NXT_SND;
uint SEQ_NEXT_Expected_ACK; // is it SND.UNA? oldest unacknowledged sequence number (rfc793)
uint SEQ_LAST_Valid_ACK;
uint WND_DUP_ACK;
double WND_CWND; 
uint WND_CWND_MAX;
uint WND_SSTHRESH;
bool firstSS;
double WND_CWND_precise;
uint SEQ_NXT;  // Next position to be sent (when allowed) -> It can overflow and be 1 ahead of the current SEND BUFFER size
uint SEQ_NextAcceptedECNMark = -1; // packets with SEQ < SEQ_NextAcceptedECNMark are not taken into account by ECN. This is to avoid reducing the window several times in the same cycle/RTT
uint ReceivedACKedFromPeerLayer;
uint SendACKtoPeerLayer;
double INTERPACKET_SND_TIME; 
double INTER_REQ_TIME; 
double RTT_alfa;
double T_MEASURED_RTT;
double T_RTT_DEVIATION; 
double T_RTO;
double T_RTT;
uint DUP_ACK_LIMIT;
double T_FORCED_RTO;
double OriginalACKedSEQ_TimeStamp;

double NchangeFactor;
double NchangeFactorInit;
double NchangeFactorEnd;

//double sigma;

enum STATE {WAITxAPP,WAITxACK,SENDING,tr_REQxAPP};
STATE mystate;
bool CtrolCongestion_Record_log;
bool SndPktAcks_Record_log;
double effectiveWin;


public:
	packettcpsnd_ext(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit(double t);

private:
	bool validateAck(std::shared_ptr<NetworkPacket> packet);

	/*
	 The  fundamental  function  for  comparing  sequence numbers.
	 This function returns true if Sequence is contained in the semi-open segment between Left  and  Right.
	 RFC for TCP modulo 32 arithmetics: https://www.rfc-editor.org/ien/ien74.txt
	 */
	bool checkWindow(uint left, uint sequence, uint right);
};
#endif
