package org.project.openbaton.catalogue.mano.common;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import org.project.openbaton.catalogue.util.IdGenerator;

import java.io.Serializable;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class AutoScalePolicy implements Serializable{
	@Id
	private String id = IdGenerator.createUUID();
	@Version
	private int version = 0;

	private String action;
	private String metric;
	private String statistic;
	private String comparisonOperator;
	private int period;
	private int threshold;
	private int cooldown;

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	public String getStatistic() {
		return statistic;
	}

	public void setStatistic(String statistic) {
		this.statistic = statistic;
	}

	public String getComparisonOperator() {
		return comparisonOperator;
	}

	public void setComparisonOperator(String comparisonOperator) {
		this.comparisonOperator = comparisonOperator;
	}

	public String getId() {
		return id;
	}

	public String getAction() {

		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
