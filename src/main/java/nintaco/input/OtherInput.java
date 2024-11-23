package nintaco.input;

import nintaco.Machine;

import java.io.Serializable;

public interface OtherInput extends Serializable {

    long serialVersionUID = 0;

    void run(Machine machine);
}
