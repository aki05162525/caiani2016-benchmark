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
package benchmark.report;

import benchmark.StaticValues;
import benchmark.agents.Households;
import net.sourceforge.jabm.Population;
import net.sourceforge.jabm.agent.Agent;
import jmab.population.MacroPopulation;
import jmab.report.MacroVariableComputer;
import jmab.simulations.MacroSimulation;

/**
 * Computes average tenure for households.
 * Can filter by labor type (Regular/Non-regular) using laborTypeFilter.
 *
 * laborTypeFilter:
 *   -1 = all workers (default)
 *    0 = Regular workers only (LABOR_TYPE_R)
 *    1 = Non-regular workers only (LABOR_TYPE_N)
 */
public class AverageTenureComputer implements MacroVariableComputer {

	private int householdId;
	private int laborTypeFilter = -1; // -1 means all workers

	@Override
	public double computeVariable(MacroSimulation sim) {
		MacroPopulation macroPop = (MacroPopulation) sim.getPopulation();
		Population pop = macroPop.getPopulation(householdId);
		double totalTenure = 0;
		int count = 0;
		for (Agent i : pop.getAgents()) {
			Households household = (Households) i;
			// Filter by labor type if specified
			if (laborTypeFilter == -1 || household.getLaborType() == laborTypeFilter) {
				// Only count employed workers for meaningful tenure average
				if (household.isEmployed()) {
					totalTenure += household.getTenure();
					count++;
				}
			}
		}
		if (count == 0) {
			return 0;
		}
		return totalTenure / count;
	}

	public int getHouseholdId() {
		return householdId;
	}

	public void setHouseholdId(int householdId) {
		this.householdId = householdId;
	}

	public int getLaborTypeFilter() {
		return laborTypeFilter;
	}

	public void setLaborTypeFilter(int laborTypeFilter) {
		this.laborTypeFilter = laborTypeFilter;
	}
}
