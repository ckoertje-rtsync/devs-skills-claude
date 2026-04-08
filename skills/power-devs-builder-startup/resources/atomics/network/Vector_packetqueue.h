#if !defined Vector_packetqueue_h
#define Vector_packetqueue_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "engine.h"
#include "packetqueue.h"
#include "vector/VectorialModel.h"

class Vector_packetqueue: public VectorialModel<packetqueue> {
public:
	Vector_packetqueue(const char *n): VectorialModel<packetqueue>(n) {};
	~Vector_packetqueue() { }

	int getParameterCount() { return 2; };
};
#endif
