//CPP:general/BaseSimulator.cpp
#if !defined BaseSimulator_h
#define BaseSimulator_h

#include <memory>

#include "simulator.h"
#include "event.h"
#include "stdarg.h"
#include "boost/format.hpp"

#include "coupling.h"

#include "sinks/ParameterReader.h"
#include "sinks/Loggers/IPowerDEVSLogger.h"
#include "sinks/Loggers/ConfigurationLogger.h" // TODO: delete this include afterwards, when the default logger is set to configure

/* define a macro with the name so that the string is not evaluated if it will not be logged */
//#define debugMsg(__level, __fmt) if(this->debugLevel >= __level) _debugMsg(__level, __fmt)
#define debugMsg(__level, __fmt, ...) if(this->debugLevel >= __level) _debugMsg(__level, __fmt, __VA_ARGS__)




class BaseSimulator: public Simulator {
protected:
	// Parameters
	double sigma;

	// Helpers
	int logLevel = -1;
	int debugLevel = -1;
	std::shared_ptr<IPowerDEVSLogger> logger;

	/**
	 * DO NOT use this function, use the debugMsg macro instead
	 * Write a debug message to the pdevs_<ExperimentNumber>.log file.
	 * If level <  Config(debugLevel)
	 */
	void _debugMsg(int level, const char *fmt,...) const;

	/**
	 * Write a debug message to the pdevs_<ExperimentNumber>.log file.
	 * If level <  Config(debugLevel)
	 * to use as debugMsg(LEVEL, boost::format("My string:%s) % "pepe")
	 */
//	void _debugMsg(int level, boost::format msg);

	/**
	 * Implements safety check for casting the value in the shared_ptr in Event.valuePtr.	 *
	 */
	template<typename T>
	std::shared_ptr<T> castEventPointer(Event x){
		auto value = std::static_pointer_cast<T>(x.valuePtr);

		// Safety Checks
		if(value == NULL){ // wrong message format
			printLog(LOG_LEVEL_ERROR, "[%f] %s: [WARNING!] Error wrong message received: unable to cast to (%s). \n", this->e, this->getName(), typeid(T).name());
			throw std::runtime_error( std::string("Unable to cast to event value in model ") + std::string(this->getName()));
		}

		return value;
	}
public:
	BaseSimulator(const char *n): Simulator(n), sigma(std::numeric_limits<double>::max()), logLevel(-1), debugLevel(-1)  {};
	void init(double);
	double ta(double t);
	void exit(double t);
};
#endif
