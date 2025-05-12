package cn.kinlon.emu.input;

import cn.kinlon.emu.Machine;

import java.io.Serializable;

public interface OtherInput extends Serializable {

    long serialVersionUID = 0;

    void run(Machine machine);
}
