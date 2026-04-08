# PowerDEVS Builder Resources

This directory contains reference files for the power-devs-builder skill, sourced from the PowerDEVS CERN development project.

## Directory Structure

```
resources/
├── README.md           # This file
├── models/             # 44 reference .pdm model files
│   ├── lotka_volterra.pdm
│   ├── vanderpol_osc.pdm
│   ├── buck_controlled.pdm
│   └── ... (41 more)
├── atomics/            # 306 atomic block headers (.h)
│   ├── qss/            # 48 files - QSS integration methods
│   ├── continuous/     # 33 files - Continuous system blocks
│   ├── hybrid/         # 44 files - Hybrid system blocks
│   ├── sinks/          # 28 files - Output/visualization
│   ├── sources/        # 32 files - Signal generators
│   └── ... (16 more categories)
└── templates/          # Reusable model templates
```

## Source

Files imported from: `C:\Users\carbo\OneDrive\Desktop\powerdevs-CERN-development`

## Models Catalog (44 files)

### Continuous Systems
| File | Description |
|------|-------------|
| `lotka_volterra.pdm` | Predator-prey population dynamics |
| `vanderpol_osc.pdm` | Van der Pol oscillator |
| `nl_stiff.pdm` | Nonlinear stiff system |
| `lcline.pdm` | LC transmission line |

### Hybrid Systems
| File | Description |
|------|-------------|
| `buck_controlled.pdm` | Controlled buck converter |
| `buck_controlled_coupled.pdm` | Coupled buck converter model |
| `buck_disc.pdm` | Discrete buck converter |
| `dc_drive.pdm` | DC motor drive |
| `dc_drive_buck.pdm` | DC drive with buck converter |
| `bball_downstairs.pdm` | Bouncing ball hybrid model |
| `spikes.pdm` | Spiking neuron model |
| `inverted_pendulum.pdm` | Inverted pendulum control |

### Delay Differential Equations (DDE)
| File | Description |
|------|-------------|
| `cellular_network_spikes.pdm` | Cellular network with delays |
| `cellular_network_spikes_ss.pdm` | Steady-state variant |
| `hairer_et_al.pdm` | Hairer DDE benchmark |
| `hairer_et_al_qssplot.pdm` | With QSS plotting |
| `oberle_and_pesch.pdm` | Oberle-Pesch DDE problem |

### Discrete Event Systems
| File | Description |
|------|-------------|
| `nicholson_bailey.pdm` | Nicholson-Bailey host-parasitoid |
| `qoperator.pdm` | Q-operator discrete model |
| `statespace.pdm` | State-space discrete system |

### Network Models
| File | Description |
|------|-------------|
| `network_basic.pdm` | Basic network topology |
| `network_advanced_1.pdm` | Advanced network example 1 |
| `network_advanced_2.pdm` | Advanced network example 2 |

### Petri Nets
| File | Description |
|------|-------------|
| `dining_philosophers.pdm` | Classic dining philosophers |
| `queue_processor.pdm` | Queue with processor |

### Queueing Systems
| File | Description |
|------|-------------|
| `basic.pdm` | Basic queueing model |

### Vector/Large-Scale
| File | Description |
|------|-------------|
| `airs.pdm` | AIRS satellite model |
| `inverters.pdm` | Inverter array |
| `neurons.pdm` | Neural network model |
| `vector1.pdm` | Vector operations example |

### Other Models
| File | Description |
|------|-------------|
| `differential-drive.pdm` | Differential drive robot |
| `rtview.pdm` | Real-time visualization |
| `test-udp.pdm` | UDP communication test |
| `templateModel.pdm` | Template with infrastructure |

### Test/Validation Models
| File | Description |
|------|-------------|
| `testQSS.pdm` | QSS method validation |
| `testBoundedIntegrator.pdm` | Bounded integrator test |
| `testComparator.pdm` | Comparator block test |
| `testReservoir.pdm` | Reservoir model test |
| `testBufferServer.pdm` | Buffer server test |
| `testBufferServer_RED.pdm` | RED queue variant |
| `TestLogicalOperators.pdm` | Logical operators test |
| `SaturationTest.pdm` | Saturation block test |
| `testBugInSaturation.pdm` | Saturation bug regression |
| `continousQueueOnly_weightedAvg.pdm` | Weighted average queue |

## Atomics Catalog (306 headers)

### QSS Components (48 files)
Primary quantized state system blocks for continuous simulation.

| File | Description |
|------|-------------|
| `qss_integrator.h` | QSS1-4, LIQSS, BQSS, CQSS integrators |
| `qss_wsum.h` | Weighted sum (up to 8 inputs) |
| `qss_multiplier.h` | Signal multiplier |
| `qss_gain.h` | Gain block |
| `qss_saturation.h` | Saturation limiter |
| `qss_hysteresis.h` | Hysteresis block |
| `qss_cross.h` | Zero-crossing detector |
| `qss_switch.h` | Signal switch |
| `qss_delay.h` | Transport delay |
| `qss_pow.h` | Power function |
| `qss_sqrt.h` | Square root |
| `qss_sin.h` / `qss_cos.h` | Trigonometric functions |
| `qss_exp.h` / `qss_log.h` | Exponential/logarithm |
| *...and 33 more* | |

### Continuous (33 files)
Classical continuous system blocks.

| File | Description |
|------|-------------|
| `integrator.h` | Standard integrator |
| `multiplier.h` | Signal multiplier |
| `gain.h` | Gain block |
| `delay.h` | Time delay |
| `inverse.h` | Signal inverse |
| `mathexpr.h` | Mathematical expression |
| `logintegrator.h` | Logarithmic integrator |
| *...and 26 more* | |

### Hybrid (44 files)
Blocks for hybrid continuous/discrete systems.

| File | Description |
|------|-------------|
| `hyb_integrator.h` | Hybrid integrator |
| `hyb_cross.h` | State event detector |
| `hyb_switch.h` | Hybrid switch |
| `hyb_saturation.h` | Saturation with events |
| `hyb_buck.h` | Buck converter |
| `hyb_neuron.h` | Spiking neuron |
| *...and 38 more* | |

### Sinks (28 files)
Output and visualization blocks.

| File | Description |
|------|-------------|
| `gnuplot.h` | GnuPlot visualization |
| `toLogger.h` | Data logger |
| `toFile.h` | File output |
| `toScilab.h` | Scilab interface |
| `display.h` | Display block |
| `scope.h` | Oscilloscope view |
| *...and 22 more* | |

### Sources (32 files)
Signal generation blocks.

| File | Description |
|------|-------------|
| `step.h` | Step function |
| `ramp.h` | Ramp signal |
| `pulse.h` | Pulse generator |
| `sine.h` | Sine wave |
| `square.h` | Square wave |
| `random.h` | Random signal |
| `constant.h` | Constant value |
| `fromFile.h` | File input |
| *...and 24 more* | |

### Network (38 files)
Data network simulation blocks.

| File | Description |
|------|-------------|
| `router.h` | Network router |
| `queue.h` | Packet queue |
| `server.h` | Network server |
| `link.h` | Network link |
| `traffic_generator.h` | Traffic source |
| *...and 33 more* | |

### Vector (28 files)
Large-scale/vectorized operations.

| File | Description |
|------|-------------|
| `vec_integrator.h` | Vector integrator |
| `vec_sum.h` | Vector sum |
| `vec_gain.h` | Vector gain |
| `vec_coupling.h` | Vector coupling |
| *...and 24 more* | |

### Other Categories

| Category | Files | Description |
|----------|-------|-------------|
| `hybrid_network/` | 8 | Hybrid network blocks |
| `modelica/` | 8 | Modelica interface blocks |
| `petri/` | 6 | Petri net components |
| `adquisicion/` | 6 | Data acquisition blocks |
| `queueing/` | 5 | Queueing theory blocks |
| `experimental/` | 5 | Experimental features |
| `realtime/` | 5 | Real-time execution |
| `control/` | 3 | Control system blocks |
| `discrete/` | 2 | Discrete event blocks |
| `signal/` | 2 | Signal processing |
| `udpcomm/` | 2 | UDP communication |
| `general/` | 1 | General utilities |
| `random/` | 1 | Random number generation |
| `signal_bus/` | 1 | Signal bus routing |

## Usage

The power-devs-builder skill automatically scans these directories to:

1. **Extract component patterns** from .pdm models
2. **Parse port configurations** from .h headers
3. **Learn layout conventions** for generated models
4. **Provide block references** when generating new models

## Adding New References

### Adding Model Files
Place `.pdm` files in the `models/` directory:
- Used to extract component patterns
- Path declarations inform generated models
- Layout conventions are learned automatically

### Adding Atomic Block Sources
Place `.h` files in the appropriate `atomics/` subdirectory:
- Header files are parsed for port configurations
- Parameter definitions are extracted
- Enables generation using custom atomic blocks

### Adding Templates
Place template `.pdm` files in the `templates/` directory:
- Used as starting points for specific system types
- Can include placeholder markers for customization
