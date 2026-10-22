"""Generator -> JobProcessor pipeline written against PythonPDEVS (pypdevs).

Two atomic models and one coupled model:

* ``Generator``    emits a ``Job`` every ``period`` time units.
* ``JobProcessor`` serves one job at a time; arrivals while busy are dropped.
* ``GenProc``      couples them and re-exports the processor's ``out`` port.
"""

from pypdevs.DEVS import AtomicDEVS, CoupledDEVS
from pypdevs.infinity import INFINITY


class Job:
    """A unit of work with an id and a service demand."""

    def __init__(self, job_id, size=1.0):
        self.job_id = job_id
        self.size = size

    def __repr__(self):
        return "Job(%d, %.2f)" % (self.job_id, self.size)


class GeneratorState:
    """State of :class:`Generator`: phase, sigma and a running job counter."""

    def __init__(self, period):
        self.phase = "active"
        self.sigma = period
        self.count = 0


class Generator(AtomicDEVS):
    """Periodic job source.

    Phases: ``active`` (ta = period) and ``passive`` (ta = INFINITY, entered on
    receipt of any message on ``stop``).
    """

    def __init__(self, name="gen", period=2.0):
        AtomicDEVS.__init__(self, name)
        self.period = period
        self.state = GeneratorState(period)
        self.stop = self.addInPort("stop")
        self.out = self.addOutPort("out")

    def timeAdvance(self):
        return self.state.sigma

    def outputFnc(self):
        if self.state.phase == "active":
            return {self.out: [Job(self.state.count)]}
        return {}

    def intTransition(self):
        self.state.count += 1
        self.state.sigma = self.period
        return self.state

    def extTransition(self, inputs):
        if self.stop in inputs:
            self.state.phase = "passive"
            self.state.sigma = INFINITY
        else:
            self.state.sigma -= self.elapsed
        return self.state


class ProcessorState:
    """State of :class:`JobProcessor`."""

    def __init__(self):
        self.phase = "passive"
        self.sigma = INFINITY
        self.job = None
        self.processed = 0


class JobProcessor(AtomicDEVS):
    """Single-server processor without a queue.

    Phases: ``passive`` (ta = INFINITY) and ``busy`` (ta = remaining service
    time). A job arriving while busy is dropped.
    """

    def __init__(self, name="proc", processing_time=1.0):
        AtomicDEVS.__init__(self, name)
        self.processing_time = processing_time
        self.state = ProcessorState()
        self.inport = self.addInPort("in")
        self.outport = self.addOutPort("out")

    def timeAdvance(self):
        return self.state.sigma

    def outputFnc(self):
        if self.state.phase == "busy":
            return {self.outport: [self.state.job]}
        return {}

    def intTransition(self):
        self.state.processed += 1
        self.state.job = None
        self.state.phase = "passive"
        self.state.sigma = INFINITY
        return self.state

    def extTransition(self, inputs):
        jobs = inputs.get(self.inport, [])
        if self.state.phase == "passive" and jobs:
            self.state.job = jobs[0]
            self.state.phase = "busy"
            self.state.sigma = self.processing_time
        else:
            self.state.sigma -= self.elapsed
        return self.state

    def confTransition(self, inputs):
        self.intTransition()
        self.elapsed = 0.0
        return self.extTransition(inputs)


class GenProc(CoupledDEVS):
    """Generator feeding a JobProcessor.

    External input ``stop`` is routed to the generator; the processor's
    ``out`` port is re-exported as the coupled model's ``out``.
    """

    def __init__(self, name="GenProc", period=2.0, processing_time=1.0):
        CoupledDEVS.__init__(self, name)
        self.stop = self.addInPort("stop")
        self.out = self.addOutPort("out")
        self.gen = self.addSubModel(Generator("gen", period))
        self.proc = self.addSubModel(JobProcessor("proc", processing_time))
        self.connectPorts(self.stop, self.gen.stop)
        self.connectPorts(self.gen.out, self.proc.inport)
        self.connectPorts(self.proc.outport, self.out)
