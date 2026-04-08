//CPP:network/Vector_packetRED_AQM.cpp
//CPP:vector/VectorialModel.cpp
//CPP:vector/ScalarSimulator.cpp
//CPP:network/packetRED_AQM.cpp
#if !defined Vector_packetRED_AQM_h
#define Vector_packetRED_AQM_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "engine.h"
#include "packetRED_AQM.h"
#include "vector/VectorialModel.h"

class Vector_packetRED_AQM: public VectorialModel<packetRED_AQM> {
public:
	Vector_packetRED_AQM(const char *n): VectorialModel<packetRED_AQM>(n) {};
	~Vector_packetRED_AQM() { }

	int getParameterCount() { return 5; };
};
#endif
