//CPP:qss/mathexpr.cpp

//CPP:qss/qsstools.cpp

//CPP:qss/qss_nlfunction_step.cpp
#if !defined qss_nlfunction_step_h
#define qss_nlfunction_step_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "continuous/mathexpr.h"
#include <math.h>       /* fabs */

#include "qss/qsstools.h"
#include "sinks/ParameterReader.h"


class qss_nlfunction_step: public BaseSimulator {
// Declare here the state, output
// variables and parameters

//states
double u[10][10];
double uaux[10][10],f[10];
int order;
double dt;

//parameters
char* expre;
int n;
PROperation pop;
double tol,dQmin,dQrel,y_n1,y_n2;
bool purely_static;
//output
double y[10];
double f3dt,f2dt,fdt,f0,f_dt,f_2dt,f_3dt;

























public:
	qss_nlfunction_step(const char *n): BaseSimulator(n) {};
	void init(double, ...);

	void dint(double);
	void dext(Event , double );
	Event lambda(double);

};
#endif
