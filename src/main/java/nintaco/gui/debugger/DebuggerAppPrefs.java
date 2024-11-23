package nintaco.gui.debugger;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

import static nintaco.disassembler.AddressType.BankAndAddress;
import static nintaco.disassembler.BranchesType.AbsoluteBranches;

public class DebuggerAppPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Integer addressType;
    private Integer branchesType;
    private Boolean showPC;
    private Boolean showInspections;
    private Boolean showAddressLabels;
    private Boolean showMachineCode;
    private Boolean showUnofficialOpcodes;

    public int getAddressType() {
        synchronized (AppPrefs.class) {
            if (addressType == null) {
                addressType = BankAndAddress;
            }
            return addressType;
        }
    }

    public void setAddressType(final int addressType) {
        synchronized (AppPrefs.class) {
            this.addressType = addressType;
        }
    }

    public int getBranchesType() {
        synchronized (AppPrefs.class) {
            if (branchesType == null) {
                branchesType = AbsoluteBranches;
            }
            return branchesType;
        }
    }

    public void setBranchesType(final int branchesType) {
        synchronized (AppPrefs.class) {
            this.branchesType = branchesType;
        }
    }

    public boolean isShowPC() {
        synchronized (AppPrefs.class) {
            if (showPC == null) {
                showPC = true;
            }
            return showPC;
        }
    }

    public void setShowPC(final boolean showPC) {
        synchronized (AppPrefs.class) {
            this.showPC = showPC;
        }
    }

    public boolean isShowInspections() {
        synchronized (AppPrefs.class) {
            if (showInspections == null) {
                showInspections = true;
            }
            return showInspections;
        }
    }

    public void setShowInspections(final boolean showInspections) {
        synchronized (AppPrefs.class) {
            this.showInspections = showInspections;
        }
    }

    public boolean isShowAddressLabels() {
        synchronized (AppPrefs.class) {
            if (showAddressLabels == null) {
                showAddressLabels = true;
            }
            return showAddressLabels;
        }
    }

    public void setShowAddressLabels(final boolean showAddressLabels) {
        synchronized (AppPrefs.class) {
            this.showAddressLabels = showAddressLabels;
        }
    }

    public boolean isShowMachineCode() {
        synchronized (AppPrefs.class) {
            if (showMachineCode == null) {
                showMachineCode = true;
            }
            return showMachineCode;
        }
    }

    public void setShowMachineCode(final boolean showMachineCode) {
        synchronized (AppPrefs.class) {
            this.showMachineCode = showMachineCode;
        }
    }

    public boolean isShowUnofficialOpcodes() {
        synchronized (AppPrefs.class) {
            if (showUnofficialOpcodes == null) {
                showUnofficialOpcodes = false;
            }
            return showUnofficialOpcodes;
        }
    }

    public void setShowUnofficialOpcodes(final boolean showUnofficialOpcodes) {
        synchronized (AppPrefs.class) {
            this.showUnofficialOpcodes = showUnofficialOpcodes;
        }
    }
}