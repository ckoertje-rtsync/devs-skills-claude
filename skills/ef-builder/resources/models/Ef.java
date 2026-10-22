package Models.java;

import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;

public class Ef extends CoupledModelImpl {
	private static final long serialVersionUID = 1L;

	public final Port<Job> in = addInputPort("in",Job.class);
	public final Port<Job> out = addOutputPort("out",Job.class);
	public final Port<Job> result = addOutputPort("result",Job.class);
	
	public Ef(String name, double intArrT, double observeT) {
		super(name);
		
		Transd transd = new Transd("Transd",observeT);
		Genr genr = new Genr("Genr",intArrT);
		
		addChildModel(genr);
		addChildModel(transd);
		
		addCoupling(this.in, transd.solved);
		addCoupling(genr.out,transd.ariv);
		addCoupling(genr.out,this.out);
		addCoupling(transd.out,this.result);
		addCoupling(transd.out,genr.stop);
	}
}
