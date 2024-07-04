package org.aya.garaschedule;

public class GaraItem {
    private String label;
    private boolean enabled;
    public GaraItem(String label, boolean enabled) {
        this.label = label;
        this.enabled = enabled;
    }

    public GaraItem(String label) {
        this.label = label;
        this.enabled = true;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
