#if !defined Vector_packetqueue_simple_h
#define Vector_packetqueue_simple_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "engine.h"
#include "packetqueue_simple.h"
#include "vector/VectorialModel.h"

class Vector_packetqueue_simple: public VectorialModel<packetqueue_simple> {
public:
	Vector_packetqueue_simple(const char *n): VectorialModel<packetqueue_simple>(n) {};
	~Vector_packetqueue_simple() { }

	int getParameterCount() { return 2; };
};
#endif
