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

import benchmark.agents.Government;
import jmab.agents.AbstractFirm;
import jmab.population.MacroPopulation;
import jmab.report.MacroVariableComputer;
import jmab.simulations.MacroSimulation;
import net.sourceforge.jabm.Population;
import net.sourceforge.jabm.agent.Agent;

/**
 * Computes total vacancies (laborDemandR/N) for a specific labor type.
 */
public class VacanciesByTypeComputer implements MacroVariableComputer {

	private int[] populationIds;
	private int laborType; // 0=R, 1=N

	public int[] getPopulationIds() {
		return populationIds;
	}

	public void setPopulationIds(int[] populationIds) {
		this.populationIds = populationIds;
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
		int vacancies = 0;
		for (int i = 0; i < populationIds.length; i++) {
			Population pop = macroPop.getPopulation(populationIds[i]);
			for (Agent agent : pop.getAgents()) {
				if (agent instanceof AbstractFirm) {
					AbstractFirm firm = (AbstractFirm) agent;
					vacancies += (laborType == 0) ? firm.getLaborDemandR() : firm.getLaborDemandN();
				} else if (agent instanceof Government) {
					Government govt = (Government) agent;
					vacancies += (laborType == 0) ? govt.getLaborDemandR() : govt.getLaborDemandN();
				}
			}
		}
		return vacancies;
	}
}
