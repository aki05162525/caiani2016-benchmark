/*
 * JMAB - Java Macroeconomic Agent Based Modeling Toolkit
 * Copyright (C) 2013 Alessandro Caiani and Antoine Godin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package benchmark.strategies;

import java.nio.ByteBuffer;

import benchmark.StaticValues;
import benchmark.agents.Households;
import jmab.population.MacroPopulation;
import jmab.strategies.ConsumptionStrategy;
import net.sourceforge.jabm.strategy.AbstractStrategy;

/**
 * Consumption strategy with different propensities for Regular (R) and Non-regular (N) labor types.
 *
 * Consumption demand formula:
 * demand = persistency * pastConsumption + (1-persistency) * [propensityOOI * (netIncome/priceExp) + propensityOOW * (netWealth/priceExp)]
 *
 * Where propensityOOI and propensityOOW are selected based on the household's laborType:
 * - laborType = 0 (Regular): uses propensityOOI_R and propensityOOW_R
 * - laborType = 1 (Non-regular): uses propensityOOI_N and propensityOOW_N
 */
@SuppressWarnings("serial")
public class ConsumptionDualLaborTypeStrategy extends AbstractStrategy
		implements ConsumptionStrategy {

	// Regular labor type propensities
	private double propensityOOI_R;
	private double propensityOOW_R;

	// Non-regular labor type propensities
	private double propensityOOI_N;
	private double propensityOOW_N;

	// Common parameters
	private double persistency;
	private int pastConsumptionId;
	private int consPriceExpectationID;

	/**
	 * @return the propensityOOI_R
	 */
	public double getPropensityOOI_R() {
		return propensityOOI_R;
	}

	/**
	 * @param propensityOOI_R the propensityOOI_R to set
	 */
	public void setPropensityOOI_R(double propensityOOI_R) {
		this.propensityOOI_R = propensityOOI_R;
	}

	/**
	 * @return the propensityOOW_R
	 */
	public double getPropensityOOW_R() {
		return propensityOOW_R;
	}

	/**
	 * @param propensityOOW_R the propensityOOW_R to set
	 */
	public void setPropensityOOW_R(double propensityOOW_R) {
		this.propensityOOW_R = propensityOOW_R;
	}

	/**
	 * @return the propensityOOI_N
	 */
	public double getPropensityOOI_N() {
		return propensityOOI_N;
	}

	/**
	 * @param propensityOOI_N the propensityOOI_N to set
	 */
	public void setPropensityOOI_N(double propensityOOI_N) {
		this.propensityOOI_N = propensityOOI_N;
	}

	/**
	 * @return the propensityOOW_N
	 */
	public double getPropensityOOW_N() {
		return propensityOOW_N;
	}

	/**
	 * @param propensityOOW_N the propensityOOW_N to set
	 */
	public void setPropensityOOW_N(double propensityOOW_N) {
		this.propensityOOW_N = propensityOOW_N;
	}

	/**
	 * @return the persistency
	 */
	public double getPersistency() {
		return persistency;
	}

	/**
	 * @param persistency the persistency to set
	 */
	public void setPersistency(double persistency) {
		this.persistency = persistency;
	}

	/**
	 * @return the pastConsumptionId
	 */
	public int getPastConsumptionId() {
		return pastConsumptionId;
	}

	/**
	 * @param pastConsumptionId the pastConsumptionId to set
	 */
	public void setPastConsumptionId(int pastConsumptionId) {
		this.pastConsumptionId = pastConsumptionId;
	}

	/**
	 * @return the consPriceExpectationID
	 */
	public int getConsPriceExpectationID() {
		return consPriceExpectationID;
	}

	/**
	 * @param consPriceExpectationID the consPriceExpectationID to set
	 */
	public void setConsPriceExpectationID(int consPriceExpectationID) {
		this.consPriceExpectationID = consPriceExpectationID;
	}

	/* (non-Javadoc)
	 * @see jmab.strategies.ConsumptionStrategy#computeRealConsumptionDemand()
	 */
	@Override
	public double computeRealConsumptionDemand() {
		Households household = (Households) this.getAgent();
		int laborType = household.getLaborType();

		// Select propensities based on labor type
		double propensityOOI;
		double propensityOOW;
		if (laborType == StaticValues.LABOR_TYPE_R) {
			propensityOOI = propensityOOI_R;
			propensityOOW = propensityOOW_R;
		} else {
			propensityOOI = propensityOOI_N;
			propensityOOW = propensityOOW_N;
		}

		double priceExpectation = household.getExpectation(consPriceExpectationID).getExpectation();
		double pastConsumption = household.getPassedValue(pastConsumptionId, 1);
		double netIncome = household.getNetIncome();
		double netWealth = household.getNetWealth();

		double demand = persistency * pastConsumption
				+ (1 - persistency) * (propensityOOI * (netIncome / priceExpectation)
						+ propensityOOW * (netWealth / priceExpectation));
		return demand;
	}

	/**
	 * Generate the byte array structure of the strategy. The structure is as follow:
	 * [propensityOOI_R][propensityOOW_R][propensityOOI_N][propensityOOW_N][persistency][consPriceExpectationID][pastConsumptionId]
	 * @return the byte array content
	 */
	@Override
	public byte[] getBytes() {
		ByteBuffer buf = ByteBuffer.allocate(48);
		buf.putDouble(this.propensityOOI_R);
		buf.putDouble(this.propensityOOW_R);
		buf.putDouble(this.propensityOOI_N);
		buf.putDouble(this.propensityOOW_N);
		buf.putDouble(this.persistency);
		buf.putInt(this.consPriceExpectationID);
		buf.putInt(this.pastConsumptionId);
		return buf.array();
	}

	/**
	 * Populates the strategy from the byte array content. The structure should be as follows:
	 * [propensityOOI_R][propensityOOW_R][propensityOOI_N][propensityOOW_N][persistency][consPriceExpectationID][pastConsumptionId]
	 * @param content the byte array containing the structure of the strategy
	 * @param pop the Macro Population of agents
	 */
	@Override
	public void populateFromBytes(byte[] content, MacroPopulation pop) {
		ByteBuffer buf = ByteBuffer.wrap(content);
		this.propensityOOI_R = buf.getDouble();
		this.propensityOOW_R = buf.getDouble();
		this.propensityOOI_N = buf.getDouble();
		this.propensityOOW_N = buf.getDouble();
		this.persistency = buf.getDouble();
		this.consPriceExpectationID = buf.getInt();
		this.pastConsumptionId = buf.getInt();
	}

}
