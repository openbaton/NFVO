package org.openbaton.catalogue.mano.common.monitoring;


import java.io.Serializable;

/**
 * Created by mob on 18.11.15.
 */
public class ThresholdDetails implements Serializable{
    private String function;
    private String triggerOperator;
    private PerceivedSeverity perceivedSeverity;
    private String value;

    public ThresholdDetails(String function, String value, String triggerOperator) {
        this.function = function;
        this.value = value;
        this.triggerOperator = triggerOperator;
        this.perceivedSeverity=PerceivedSeverity.INDETERMINATE;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getTriggerOperator() {
        return triggerOperator;
    }

    public void setTriggerOperator(String triggerOperator) {
        this.triggerOperator = triggerOperator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PerceivedSeverity getPerceivedSeverity() {
        return perceivedSeverity;
    }

    public void setPerceivedSeverity(PerceivedSeverity perceivedSeverity) {
        this.perceivedSeverity = perceivedSeverity;
    }
}
