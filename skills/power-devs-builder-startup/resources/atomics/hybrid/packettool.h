#if !defined PACKETTOOL
#define PACKETTOOL

#include <stdlib.h>
#include <queue>
#include <string>
#include <list>
#include <map>

// TCP Fields
#define SRC 1 
#define DST 2 
#define SEQ 3

// Protocols
#define TCP 1 
#define UDP 2 

// Metadata
#define OVERHEAD 1
#define PAYLOADSIZE 2

struct protocol {int layer; int protoID; std::map<double,double> headerFields; std::map<double,double> trailerFields; std::map<double,double> METADATA;} ;
typedef std::map<double,double> aMap ;
typedef std::map<double,double>::iterator aMapIter ;
typedef std::list<protocol> aProtocolList ;
typedef std::list<protocol>::iterator aProtocolListIter;

typedef std::list<void*> anExperimentalList ;

class Packet {

public:
 		
  Packet()
  		  {	ID=0;
		 	length=0;
			fluidPackets=0;};
			
  ~Packet()
  		  {printLog("Packet Destroyed \n");};
  
  aProtocolList ProtocolList;
  anExperimentalList experimentalList;
  
  std::string payload;

  int ID;
  char* FlowID;
  int ComplexityFactor;   

  double birthTime;
  double lastModifTime;
  int 	 lastQueueSizeAfterLeaving; // After this packet leaves 

  int 	 auxiliar_SEQ ;
  
  double currentChannelPropagationTime;
  double currentReceptionTimestamp;

  void calcLength() // in bits
  {
		this->length=0;
		        for (pit=ProtocolList.begin(); pit!=ProtocolList.end(); pit++) {
           				mp = (*pit).METADATA ;	
           				mit = mp.find(OVERHEAD) ;
		   				if (mit!=mp.end()) ( this->length += (int)mit->second );		            
           				mit = mp.find(PAYLOADSIZE) ;
		   				if (mit!=mp.end()) ( this->length += (int)mit->second );		            
			};
                    	printLog("Packet: Calculated Length %u \n", this->length);
 };

void setLength(int len){
			this->length=len;
			};

int getLength()  // in bits
{
	this->calcLength();
	return this->length;
};

void setFluidPackets(int len){
			this->fluidPackets=len;
			};

int getFluidPackets()  // in bits
{
	return 	this->fluidPackets;
};



double setPropagationTime(int plength, double lat, double bw)  // In ms.
	  {
 	  currentChannelPropagationTime = (plength/bw)*1000 + lat; // In ms. (length: bits, bw: bps, lat: ms)
      printLog("Packet: Calculated Propagation Time %g \n", currentChannelPropagationTime);

 	  return currentChannelPropagationTime;
 	  };

  int length;     
  int fluidPackets;
  aProtocolListIter pit;
  aMap mp; 
  aMapIter mit;
};

typedef std::queue<Packet*> aPacketQueue;
typedef std::deque<Packet*> aDoubleEndedPacketQueue;
#endif
