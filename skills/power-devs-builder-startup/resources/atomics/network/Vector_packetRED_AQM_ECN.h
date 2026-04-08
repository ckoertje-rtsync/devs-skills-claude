//CPP:network/Vector_packetRED_AQM_ECN.cpp
//CPP:vector/VectorialModel.cpp
//CPP:vector/ScalarSimulator.cpp
//CPP:network/packetRED_AQM_ECN.cpp
#if !defined Vector_packetRED_AQM_ECN_h
#define Vector_packetRED_AQM_ECN_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "engine.h"
#include "packetRED_AQM_ECN.h"
#include "vector/VectorialModel.h"

class Vector_packetRED_AQM_ECN: public VectorialModel<packetRED_AQM_ECN> {
public:
	Vector_packetRED_AQM_ECN(const char *n): VectorialModel<packetRED_AQM_ECN>(n) {};
	~Vector_packetRED_AQM_ECN() { }

	int getParameterCount() { return 5; };
};
#endif
