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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jmab.agents.LaborSupplier;
import jmab.population.MacroPopulation;
import jmab.simulations.MacroSimulation;
import net.sourceforge.jabm.Population;
import net.sourceforge.jabm.agent.Agent;

/**
 * Caches labor market counts (labor force, employed, unemployed) per round,
 * household population set, and optional labor type filter.
 */
public final class LaborMarketStatsCache {

	private static final Map<CacheKey, LaborMarketStats> cache = new HashMap<>();
	private static int cachedRound = Integer.MIN_VALUE;

	private LaborMarketStatsCache() {
	}

	public static LaborMarketStats getStats(MacroSimulation sim, int[] householdPopIds, Integer laborType) {
		int round = sim.getRound();
		if (round != cachedRound) {
			cache.clear();
			cachedRound = round;
		}
		CacheKey key = new CacheKey(householdPopIds, laborType);
		LaborMarketStats stats = cache.get(key);
		if (stats != null) {
			return stats;
		}
		LaborMarketStats computed = compute(sim, householdPopIds, laborType);
		cache.put(key, computed);
		return computed;
	}

	private static LaborMarketStats compute(MacroSimulation sim, int[] householdPopIds, Integer laborType) {
		MacroPopulation macroPop = (MacroPopulation) sim.getPopulation();
		int laborForce = 0;
		int employed = 0;
		for (int popId : householdPopIds) {
			Population hhPop = macroPop.getPopulation(popId);
			for (Agent agent : hhPop.getAgents()) {
				LaborSupplier hh = (LaborSupplier) agent;
				if (laborType != null && hh.getLaborType() != laborType.intValue()) {
					continue;
				}
				laborForce += 1;
				if (hh.isEmployed()) {
					employed += 1;
				}
			}
		}
		int unemployed = laborForce - employed;
		return new LaborMarketStats(laborForce, employed, unemployed);
	}

	public static final class LaborMarketStats {
		private final int laborForce;
		private final int employed;
		private final int unemployed;

		public LaborMarketStats(int laborForce, int employed, int unemployed) {
			this.laborForce = laborForce;
			this.employed = employed;
			this.unemployed = unemployed;
		}

		public int getLaborForce() {
			return laborForce;
		}

		public int getEmployed() {
			return employed;
		}

		public int getUnemployed() {
			return unemployed;
		}
	}

	private static final class CacheKey {
		private final int[] populationIds;
		private final Integer laborType;

		CacheKey(int[] populationIds, Integer laborType) {
			this.populationIds = normalize(populationIds);
			this.laborType = laborType;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			CacheKey other = (CacheKey) obj;
			return Arrays.equals(populationIds, other.populationIds) && Objects.equals(laborType, other.laborType);
		}

		@Override
		public int hashCode() {
			int result = Arrays.hashCode(populationIds);
			result = 31 * result + Objects.hashCode(laborType);
			return result;
		}

		private static int[] normalize(int[] populationIds) {
			int[] copy = Arrays.copyOf(populationIds, populationIds.length);
			Arrays.sort(copy);
			return copy;
		}
	}
}
