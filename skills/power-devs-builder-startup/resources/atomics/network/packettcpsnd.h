//CPP:network/packettcpsnd.cpp
#if !defined packettcpsnd_h
#define packettcpsnd_h

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

class packettcpsnd: public BaseSimulator {
protected:

const  double MIN_RTO = 0.2; // Minimum RTO, usually in linux is 200ms
//const  double MIN_RTO = 1; // In OMNET++

std::deque<std::shared_ptr<NetworkPacket>> SendBuffer;
std::shared_ptr<NetworkPacket> p;    // TODO Matias: does it need to be a class field?
std::shared_ptr<NetworkPacket> pout; // TODO Matias: does it need to be a class field?
std::shared_ptr<NetworkPacket> pack; // TODO Matias: does it need to be a class field?

// Parameters
int MSS;
std::string ipSender;
int portSender;
std::string ipReceiver;
int portReceiver;
int WND_CWND_MAX; // maximum value for the congestion window 'clamp'
int WND_SSTHRESH; //
double T_FORCED_RTO; // sets a fixed RTO instead of calculating it dynamically
double RTT_alfa;
int DUP_ACK_LIMIT;
double T_RTT; // initial RTT
double INTERPACKET_SND_TIME;
double INTER_REQ_TIME;

bool WND_firsttime_SSTHRESH;
bool BUF_Fully_SNT; 
int BUF_SIZE; 
bool WND_Fully_SNT; 
int WND_NXT_SND;
int SEQ_NEXT_Expected_ACK; 
int SEQ_LAST_Valid_ACK; 
int WND_DUP_ACK; 
double WND_CWND; 
bool firstSS;
double WND_CWND_precise;
int SEQ_CYCLE_ACK; 
int SEQ_NXT;  // Next position to be sent (when allowed) -> It can overflow and be 1 ahead of the current SEND BUFFER size
int SEQ_NextAcceptedECNMark = -1; // packets with SEQ < SEQ_NextAcceptedECNMark are not taken into account by ECN. This is to avoid reducing the window several times in the same cycle/RTT
int ReceivedACKedFromPeerLayer; 
int SendACKtoPeerLayer;  // TODO: no used?
double T_MEASURED_RTT;
double T_RTT_DEVIATION; 
double T_RTO;


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
	packettcpsnd(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit(double t);

private:
	bool validateAck(std::shared_ptr<NetworkPacket> packet);
};
#endif
