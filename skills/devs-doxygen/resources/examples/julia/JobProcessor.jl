"""
    JobProcessing

Single-server job processor written as a plain-struct DEVS atomic model.
There is no Julia DEVS framework dependency: the model is a mutable struct plus
the five DEVS functions (`delta_int!`, `delta_ext!`, `delta_con!`, `lambda`, `ta`).
"""
module JobProcessing

export Job, JobProcessor, delta_int!, delta_ext!, delta_con!, lambda, ta

"Root type for atomic DEVS models in this module."
abstract type AbstractAtomic end

"""
    Job

A unit of work. `size` is the service demand in model time units.
"""
struct Job
    id::Int
    size::Float64
end

"""
    JobProcessor <: AbstractAtomic

Processes one job at a time. A job arriving while the server is busy is
dropped; there is no queue. Phases: `:passive` (ta = Inf) and `:busy`
(ta = remaining service time).
"""
mutable struct JobProcessor <: AbstractAtomic
    phase::Symbol
    sigma::Float64
    job::Union{Nothing, Job}
    processing_time::Float64
    processed::Int
end

"Construct a passive processor with a fixed service time (default 1.0)."
JobProcessor(processing_time::Float64 = 1.0) =
    JobProcessor(:passive, Inf, nothing, processing_time, 0)

"""
    delta_int!(m::JobProcessor)

Internal transition: the job just emitted by `lambda` is retired and the
processor returns to `:passive`.
"""
function delta_int!(m::JobProcessor)
    m.processed += 1
    m.job = nothing
    m.phase = :passive
    m.sigma = Inf
    return m
end

"""
    delta_ext!(m::JobProcessor, e::Float64, x::Vector{Job})

External transition on port `in`. When passive, the first job in `x` is
accepted and the processor holds in `:busy` for `processing_time`. When busy,
the elapsed time `e` is subtracted from `sigma` and the arrivals are dropped.
"""
function delta_ext!(m::JobProcessor, e::Float64, x::Vector{Job})
    if m.phase == :passive && !isempty(x)
        m.job = x[1]
        m.phase = :busy
        m.sigma = m.processing_time
    else
        m.sigma -= e
    end
    return m
end

"""
    delta_con!(m::JobProcessor, x::Vector{Job})

Confluent transition: internal first, then external with e = 0.
"""
function delta_con!(m::JobProcessor, x::Vector{Job})
    delta_int!(m)
    delta_ext!(m, 0.0, x)
end

"""
    lambda(m::JobProcessor)

Output function: in `:busy` the current job is emitted on port `out`.
Returns a `Dict` keyed by port name.
"""
function lambda(m::JobProcessor)
    out = Dict{Symbol, Vector{Job}}()
    if m.phase == :busy && m.job !== nothing
        out[:out] = [m.job]
    end
    return out
end

"Time advance: the remaining hold time `sigma`."
ta(m::JobProcessor) = m.sigma

end # module
