#if !defined qss_sum_h
#define qss_sum_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "qss/qsstools.h"
#include "sinks/ParameterReader.h"
#include "boost/multi_array.hpp"

/**
 * Same as qss_wsum but using gains K_n=1, and can receive and arbitrary large number of inputs (wsum has a max of 8 inputs).
 * Performance might not be good for large number of inputs (n>>), as on each dext:
 *    - all inputs (except one) are advanced (O(n))
 *    - output value is calculated iterating over all inputs (and adding them) (O(n))
 *
 * The model starts with n=START_SIZE and reserves new memory if more inputs arrive.
 * There is a trade-off in performance to set the value of START_SIZE.
 * - reserving memory might be time-consuming: thus it is good START_SIZE to be high
 * - having START_SIZE bigger than necessary affects performance on each dext: thus is good START_SIZE to be small.
 *
 */
class qss_sum: public BaseSimulator {
#define N_INPUTS ((int)X.shape()[0])
#define GET_INPUT(i) (X[ boost::indices[i][range(0,10)]].origin())

	typedef boost::multi_array_types::index_range range;
	static constexpr int START_SIZE=24; // starting shape

	//states
	boost::multi_array<double, 2> X; // starts with shape [10][10] and reshapes if more inputs are needed. See dext
	int order;

	//outputs
	double y[10];

public:
	qss_sum(const char *n): BaseSimulator(n), X(boost::extents[START_SIZE][10]){};
	void init(double, ...);
	//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
	//	void exit();
};
#endif
