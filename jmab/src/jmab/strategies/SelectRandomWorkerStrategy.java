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
package jmab.strategies;

import java.util.ArrayList;
import java.util.List;

import jmab.agents.LaborSupplier;
import jmab.agents.MacroAgent;
import jmab.population.MacroPopulation;
import net.sourceforge.jabm.agent.Agent;
import net.sourceforge.jabm.agent.AgentList;
import net.sourceforge.jabm.strategy.AbstractStrategy;
import cern.jet.random.engine.RandomEngine;

/**
 * @author Alessandro Caiani and Antoine Godin
 *
 */
@SuppressWarnings("serial")
public class SelectRandomWorkerStrategy extends AbstractStrategy implements
		SelectWorkerStrategy {

	protected RandomEngine prng;
	protected int sampleSizeR;
	protected int sampleSizeN;
	
	/* (non-Javadoc)
	 * @see jmab.strategies.SelectWorkerStrategy#selectWorker(java.util.ArrayList)
	 */
	@Override
	public MacroAgent selectWorker(List<Agent> workers) {
		List<Agent> sampled = sampleWorkers(workers);
		AgentList agents = new AgentList(sampled);
		agents.shuffle(prng);
		return (MacroAgent)agents.get(0);
	}

	/* (non-Javadoc)
	 * @see jmab.strategies.SelectWorkerStrategy#selectWorkers(java.util.List)
	 */
	@Override
	public List<MacroAgent> selectWorkers(List<Agent> workers, int n) {
		List<Agent> sampled = sampleWorkers(workers);
		AgentList agents = new AgentList(sampled);
		agents.shuffle(prng);
		List<MacroAgent> result = new ArrayList<MacroAgent>();
		for(int i = 0; i<Math.min(n, agents.size());i++){
			result.add((MacroAgent)agents.get(i));
		}
		return result;
	}

	private List<Agent> sampleWorkers(List<Agent> workers) {
		if (workers == null || workers.isEmpty()) {
			return workers;
		}
		int sampleSize = resolveSampleSize(workers);
		if (sampleSize <= 0 || sampleSize >= workers.size()) {
			return workers;
		}
		AgentList agents = new AgentList(workers);
		agents.shuffle(prng);
		List<Agent> result = new ArrayList<Agent>(sampleSize);
		for (int i = 0; i < sampleSize; i++) {
			result.add(agents.get(i));
		}
		return result;
	}

	private int resolveSampleSize(List<Agent> workers) {
		Agent first = workers.get(0);
		if (first instanceof LaborSupplier) {
			int laborType = ((LaborSupplier) first).getLaborType();
			return laborType == 0 ? sampleSizeR : sampleSizeN;
		}
		return 0;
	}

	/**
	 * @return the prng
	 */
	public RandomEngine getPrng() {
		return prng;
	}

	/**
	 * @param prng the prng to set
	 */
	public void setPrng(RandomEngine prng) {
		this.prng = prng;
	}

	/**
	 * @return the sampleSizeR
	 */
	public int getSampleSizeR() {
		return sampleSizeR;
	}

	/**
	 * @param sampleSizeR the sampleSizeR to set
	 */
	public void setSampleSizeR(int sampleSizeR) {
		this.sampleSizeR = sampleSizeR;
	}

	/**
	 * @return the sampleSizeN
	 */
	public int getSampleSizeN() {
		return sampleSizeN;
	}

	/**
	 * @param sampleSizeN the sampleSizeN to set
	 */
	public void setSampleSizeN(int sampleSizeN) {
		this.sampleSizeN = sampleSizeN;
	}

	/* (non-Javadoc)
	 * @see jmab.strategies.SingleStrategy#getBytes()
	 */
	@Override
	public byte[] getBytes() {
		return new byte[1];//TODO
	}

	/* (non-Javadoc)
	 * @see jmab.strategies.SingleStrategy#populateFromBytes(byte[], jmab.population.MacroPopulation)
	 */
	@Override
	public void populateFromBytes(byte[] content, MacroPopulation pop) {}
	
}
