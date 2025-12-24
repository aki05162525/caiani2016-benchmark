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
package jmab.report;

import jmab.simulations.MacroSimulation;

/**
 * @author Alessandro Caiani and Antoine Godin
 * This computer computes the aggregate unemployment rate.
 */
public class UnemploymentRateComputer implements MacroVariableComputer {

	private int[] householdPopIds;
	
	
	
	/**
	 * @return the householdPopIds
	 */
	public int[] getHouseholdPopIds() {
		return householdPopIds;
	}

	/**
	 * @param householdPopIds the householdPopIds to set
	 */
	public void setHouseholdPopIds(int[] householdPopIds) {
		this.householdPopIds = householdPopIds;
	}

	/* (non-Javadoc)
	 * @see jmab.report.VariableComputer#computeVariable()
	 */
	@Override
	public double computeVariable(MacroSimulation sim) {
		LaborMarketStatsCache.LaborMarketStats stats = LaborMarketStatsCache.getStats(sim, householdPopIds, null);
		if (stats.getLaborForce() == 0) {
			return 0.0;
		}
		return 1 - (double) stats.getEmployed() / (double) stats.getLaborForce();
	}

	/**
	 * @return the householdPopIds
	 */
	public int[] getHouseholdPopId() {
		return householdPopIds;
	}

	/**
	 * @param householdPopIds the householdPopId to set
	 */
	public void setHouseholdPopId(int[] householdPopIds) {
		this.householdPopIds = householdPopIds;
	}

	
	
}
