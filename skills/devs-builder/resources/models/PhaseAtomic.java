package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;

import Models.java.DataStructures.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Thrust 1: Harmonization layer and physiology estimation.
 * Input: EHRRecord
 * Output: TwinState
 */

class PhaseAtomic extends AtomicModelImpl{
    protected String phase = "passive";
    protected Double sigma = Double.POSITIVE_INFINITY;

    public PhaseAtomic(){
    this("PhaseAtomic");
    }
    public PhaseAtomic(String nm) {
    super(nm);
    }
    public String getPhase() {
    return phase;
    }
    public boolean phaseIs(String phase) {
    return this.phase == phase;
    }
    public void holdIn(String phase,double sigma) {
    this.phase = phase;
    this.sigma = sigma;
    }
    public void passivateIn(String phase) {
    this.phase = phase;
    this.sigma = Double.POSITIVE_INFINITY;
    }
    public void passivate() {
    this.phase = "passive";
    this.sigma = Double.POSITIVE_INFINITY;
    }
}