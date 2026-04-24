import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.model.impl.Coupled;

/**
 * Ef — embeddable Closure-DEVS coupled.
 *
 * Declares an external output port `out` and republishes a single TinySource's
 * output through it. No main() — intended to be embedded inside a larger coupled.
 *
 * Demonstrates:
 *   - extends com.ms4systems.devs.core.model.impl.Coupled
 *   - public final Port declared at the class level (raw Port, no generics)
 *   - addChildModel(DEVS) for atomic children
 *   - port-style addCoupling(Port, Port) for an EOC (child.out -> this.out)
 */
public class Ef extends Coupled {
    private static final long serialVersionUID = 1L;

    public final Port out = addOutputPort("out", String.class);

    public Ef() {
        this("Ef");
    }

    public Ef(String name) {
        super(name);

        TinySource source = new TinySource("source", 1.0);
        addChildModel(source);

        // EOC: republish the source's output as Ef's external output
        addCoupling(source.out, this.out);
    }
}
