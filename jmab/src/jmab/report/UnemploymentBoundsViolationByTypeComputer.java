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

import jmab.agents.LaborSupplier;
import jmab.population.MacroPopulation;
import jmab.simulations.MacroSimulation;
import net.sourceforge.jabm.Population;
import net.sourceforge.jabm.agent.Agent;

/**
 * Flags unemployment rate bound violations (including LF=0).
 * Returns 1.0 when a violation is detected, otherwise 0.0.
 */
public class UnemploymentBoundsViolationByTypeComputer implements MacroVariableComputer {

	private int[] householdPopIds;
	private int laborType; // 0=R, 1=N

	public int[] getHouseholdPopIds() {
		return householdPopIds;
	}

	public void setHouseholdPopIds(int[] householdPopIds) {
		this.householdPopIds = householdPopIds;
	}

	public int getLaborType() {
		return laborType;
	}

	public void setLaborType(int laborType) {
		this.laborType = laborType;
	}

	@Override
	public double computeVariable(MacroSimulation sim) {
		MacroPopulation macroPop = (MacroPopulation) sim.getPopulation();
		int totPop = 0;
		int employed = 0;
		for (int i = 0; i < householdPopIds.length; i++) {
			Population hhPop = macroPop.getPopulation(householdPopIds[i]);
			for (Agent agent : hhPop.getAgents()) {
				LaborSupplier hh = (LaborSupplier) agent;
				if (hh.getLaborType() != laborType) {
					continue;
				}
				totPop += 1;
				if (hh.isEmployed()) {
					employed += 1;
				}
			}
		}
		if (totPop == 0) {
			return 1.0;
		}
		double uRate = 1.0 - (double) employed / (double) totPop;
		if (uRate < 0.0 || uRate > 1.0) {
			return 1.0;
		}
		return 0.0;
	}
}
