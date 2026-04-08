//CPP:sinks/to_csv.cpp
#if !defined to_csv_h
#define to_csv_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include "string.h"

class to_csv: public Simulator {
	double sigma;
	std::string fileName;
	long int file;



public:
	to_csv(const char *n): Simulator(n) {};
	void init(double, ...);
	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	void exit();
};
#endif
