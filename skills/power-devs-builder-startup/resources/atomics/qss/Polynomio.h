//CPP:qss/qsstools.cpp
//CPP:hybridQueue_mab/Polynomio.cpp
#if !defined Polynomio_h
#define Polynomio_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include <deque>

#include "qss/qsstools.h"

//TODO: would be nice to have a class to represent polynomios (implemented as vector instead of double* to facilitate memory management).
// Other useful methods can be put in this class (minposroot, advance, etc, derivativate, add, mult, div etc).
// See boost.polinomio
// For now just an internal class fastly programmed with only needed methods
// disadvantages of this class:
// - requires copying the values to create the class (could be avoided if all model use this class)
// - using vector makes no sense as either all 10 values should be set, or the coefficients can not be set to increase the order
class Polynomio {
	double coefficients[10]={0}; // NOTE: &coefficients[0] should work fine as a double* because Vectors are guaranteed to store their data contiguously
	int lastNonCeroCoeff=0;
public:
	// order -1
	Polynomio (){};

	// assumes coefficients[10]
	Polynomio (double* coeffs);

	Polynomio (std::vector<double> coeffs);

	unsigned int order() const;

	// to easily access the coefficients of the polynomio
	double& operator[](unsigned int index);

	/*
	 * return the time at which the polynomio will be equal to crossingValue
	 * (wrapper for minposroot)
	 */
	double nextCrossing(double crossingValue);

	/*
	 * advances the polynomio t seconds
	 * (wrapper for advance_time)
	 */
	void advanceTime(double t);

};


#endif
