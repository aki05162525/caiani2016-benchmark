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

import java.util.List;
import java.util.TreeMap;

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
public class SelectCheapestWorkerStrategy extends AbstractStrategy implements SelectWorkerStrategy {

	private int sampleSizeR;
	private int sampleSizeN;
	private RandomEngine prng;

	/* (non-Javadoc)
	 * @see jmab.strategies.SelectWorkerStrategy#selectWorker(java.util.ArrayList)
	 */
	@Override
	public MacroAgent selectWorker(List<Agent> workers) {
		List<Agent> sampled = sampleWorkers(workers);
		double minWage=Double.POSITIVE_INFINITY;
		MacroAgent cheapestWorker=(MacroAgent)sampled.get(0);
		for(Agent agent:sampled){
			LaborSupplier worker=(LaborSupplier)agent;
			if(worker.getWage()<minWage){
				minWage=worker.getWage();
				cheapestWorker=worker;
			}
		}
		return cheapestWorker;
	}

	/* (non-Javadoc)
	 * @see jmab.strategies.SelectWorkerStrategy#selectWorkers(java.util.List)
	 */
	@Override
	public List<MacroAgent> selectWorkers(List<Agent> workers, int n) {
		List<Agent> sampled = sampleWorkers(workers);
		double maxWage=Double.POSITIVE_INFINITY;
		TreeMap<Double, MacroAgent> tree = new TreeMap<Double, MacroAgent>(); 
		for(Agent agent:sampled){
			LaborSupplier worker=(LaborSupplier)agent;
			if(tree.size()<n){
				maxWage=Math.max(maxWage,worker.getWage());
				tree.put(worker.getWage(), worker);
			}else if(worker.getWage()<maxWage){
				tree.remove(maxWage);
				tree.put(worker.getWage(),worker);
				maxWage = tree.lastKey();
			}
		}
		return (List<MacroAgent>)tree.values();
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
		if (prng != null) {
			agents.shuffle(prng);
		}
		List<Agent> result = new java.util.ArrayList<Agent>(sampleSize);
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
