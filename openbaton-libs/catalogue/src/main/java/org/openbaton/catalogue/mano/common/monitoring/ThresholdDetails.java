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
    private String hostOperator;

    public ThresholdDetails(String function, String triggerOperator, PerceivedSeverity perceivedSeverity, String value, String hostOperator) {
        this.function = function;
        this.triggerOperator = triggerOperator;
        this.perceivedSeverity = perceivedSeverity;
        this.value = value;
        this.hostOperator = hostOperator;
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

    public String getHostOperator() {
        return hostOperator;
    }

    public void setHostOperator(String hostOperator) {
        this.hostOperator = hostOperator;
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

    @Override
    public String toString() {
        return "ThresholdDetails{" +
                "function='" + function + '\'' +
                ", triggerOperator='" + triggerOperator + '\'' +
                ", perceivedSeverity=" + perceivedSeverity +
                ", value='" + value + '\'' +
                ", hostOperator='" + hostOperator + '\'' +
                '}';
    }
}
