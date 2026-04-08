#if !defined ProcesorSharingServer_h
#define ProcesorSharingServer_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "sinks/ParameterReader.h"
#include "general/BaseSimulator.h"
#include "queueing/Jobs/Job.h"
#include "queueing/Jobs/FinishedJob.h"

#include <deque>

class ProcesorSharingServer : public BaseSimulator {
private:
	class JobBeingProcessed; // forward declaration of private class

	static uint ProcessorId; // Incremental processor Id shared by all instances


	// Parameters
	double serviceTime = -1;
	double maxCapacity = -1;
	uint id = 0;

	// State variables
	enum State { INITIALIZING, PROCESSING, DISCARDING };
	State myState = INITIALIZING;

	std::shared_ptr<Job> jobToDiscard;
	std::deque<std::shared_ptr<JobBeingProcessed>> queuedJobs;

	// Counters for logging
	uint finished = 0;
	uint arrival = 0;

public:
	ProcesorSharingServer(const char *n): BaseSimulator(n) {};
	void init(double, ...);
//	double ta(double t);
	void dint(double);
	void dext(Event , double );
	Event lambda(double);
//	void exit();
private:

	std::shared_ptr<FinishedJob> getInitializationJob();
	void updateJobsPendingServiceTime();
	void addNewJob(double t, std::shared_ptr<Job> newJob);

	double getServiceTimePerJob();
	double getNextJobFinishTime();

	void logSignals(double t);
	void printLoad(int logLevel);

	// internal class to represent jobs being processed
	class JobBeingProcessed {
	private:
		std::shared_ptr<Job> originalJob;
		double pendingServiceTime;
		double startTime;

	public:
		JobBeingProcessed(std::shared_ptr<Job> originalJob, double startTime):
			originalJob(originalJob),
			pendingServiceTime(originalJob->getWeight()),
			startTime(startTime){
		}

		std::shared_ptr<Job> getOriginalJob() { return this->originalJob; }

		double getPendingService() { return this->pendingServiceTime; }
		void setPendingServiceTime(double pendingServiceTime) { this->pendingServiceTime = pendingServiceTime; }

		double getStartTime() { return this->startTime; }
		double getTotalProcessingTime(double currentTime) { return currentTime - this->startTime; }
	};
};
#endif
