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

import java.nio.ByteBuffer;

import jmab.agents.WageSetterWithTargets;
import jmab.population.MacroPopulation;
import net.sourceforge.jabm.distribution.AbstractDelegatedDistribution;
import net.sourceforge.jabm.strategy.AbstractStrategy;

/**
 * @author Alessandro Caiani and Antoine Godin
 *
 */
@SuppressWarnings("serial")
public class AdaptiveWageStrategy extends AbstractStrategy implements
		WageStrategy {
	
	private double microThreshold; //to be set through the configuration file.
	private double macroThreshold;
	private double microAdaptiveParameter;
	private double macroAdaptiveParameter;
	private double macroThresholdR = Double.NaN;
	private double macroThresholdN = Double.NaN;
	private double microAdaptiveParameterR = Double.NaN;
	private double microAdaptiveParameterN = Double.NaN;
	private double macroAdaptiveParameterR = Double.NaN;
	private double macroAdaptiveParameterN = Double.NaN;
	protected AbstractDelegatedDistribution distribution;

	/* (non-Javadoc)
	 * @see jmab.strategies.WageStrategy#computeWage()
	 */
	@Override
	public double computeWage() {
		WageSetterWithTargets worker = (WageSetterWithTargets)getAgent();
		double macroThresholdLocal = macroThreshold;
		double microAdaptiveLocal = microAdaptiveParameter;
		double macroAdaptiveLocal = macroAdaptiveParameter;
		if (worker instanceof jmab.agents.LaborSupplier) {
			int laborType = ((jmab.agents.LaborSupplier) worker).getLaborType();
			if (laborType == 0) { // Regular
				macroThresholdLocal = selectParam(macroThresholdR, macroThreshold);
				microAdaptiveLocal = selectParam(microAdaptiveParameterR, microAdaptiveParameter);
				macroAdaptiveLocal = selectParam(macroAdaptiveParameterR, macroAdaptiveParameter);
			} else { // Non-regular
				macroThresholdLocal = selectParam(macroThresholdN, macroThreshold);
				microAdaptiveLocal = selectParam(microAdaptiveParameterN, microAdaptiveParameter);
				macroAdaptiveLocal = selectParam(macroAdaptiveParameterN, macroAdaptiveParameter);
			}
		}
		double microReferenceVariable= worker.getMicroReferenceVariableForWage();
		double wage = worker.getWage();
		if(microReferenceVariable>microThreshold){
			wage-=(microAdaptiveLocal*wage*distribution.nextDouble());
		}else{
			double macroReferenceVariable= worker.getMacroReferenceVariableForWage();
			if(macroReferenceVariable<=macroThresholdLocal)
				wage+=(macroAdaptiveLocal*wage*distribution.nextDouble());
		}
		return Math.max(wage, worker.getWageLowerBound());
	}

	private double selectParam(double typeValue, double legacyValue) {
		return Double.isNaN(typeValue) ? legacyValue : typeValue;
	}

	/**
	 * @return the microThreshold
	 */
	public double getMicroThreshold() {
		return microThreshold;
	}

	/**
	 * @param microThreshold the microThreshold to set
	 */
	public void setMicroThreshold(double microThreshold) {
		this.microThreshold = microThreshold;
	}

	/**
	 * @return the macroThreshold
	 */
	public double getMacroThreshold() {
		return macroThreshold;
	}

	/**
	 * @param macroThreshold the macroThreshold to set
	 */
	public void setMacroThreshold(double macroThreshold) {
		this.macroThreshold = macroThreshold;
	}

	/**
	 * @return the microAdaptiveParameter
	 */
	public double getMicroAdaptiveParameter() {
		return microAdaptiveParameter;
	}

	/**
	 * @param microAdaptiveParameter the microAdaptiveParameter to set
	 */
	public void setMicroAdaptiveParameter(double microAdaptiveParameter) {
		this.microAdaptiveParameter = microAdaptiveParameter;
	}

	/**
	 * @return the macroAdaptiveParameter
	 */
	public double getMacroAdaptiveParameter() {
		return macroAdaptiveParameter;
	}

	/**
	 * @param macroAdaptiveParameter the macroAdaptiveParameter to set
	 */
	public void setMacroAdaptiveParameter(double macroAdaptiveParameter) {
		this.macroAdaptiveParameter = macroAdaptiveParameter;
	}

	/**
	 * @return the macroThresholdR
	 */
	public double getMacroThresholdR() {
		return macroThresholdR;
	}

	/**
	 * @param macroThresholdR the macroThresholdR to set
	 */
	public void setMacroThresholdR(double macroThresholdR) {
		this.macroThresholdR = macroThresholdR;
	}

	/**
	 * @return the macroThresholdN
	 */
	public double getMacroThresholdN() {
		return macroThresholdN;
	}

	/**
	 * @param macroThresholdN the macroThresholdN to set
	 */
	public void setMacroThresholdN(double macroThresholdN) {
		this.macroThresholdN = macroThresholdN;
	}

	/**
	 * @return the microAdaptiveParameterR
	 */
	public double getMicroAdaptiveParameterR() {
		return microAdaptiveParameterR;
	}

	/**
	 * @param microAdaptiveParameterR the microAdaptiveParameterR to set
	 */
	public void setMicroAdaptiveParameterR(double microAdaptiveParameterR) {
		this.microAdaptiveParameterR = microAdaptiveParameterR;
	}

	/**
	 * @return the microAdaptiveParameterN
	 */
	public double getMicroAdaptiveParameterN() {
		return microAdaptiveParameterN;
	}

	/**
	 * @param microAdaptiveParameterN the microAdaptiveParameterN to set
	 */
	public void setMicroAdaptiveParameterN(double microAdaptiveParameterN) {
		this.microAdaptiveParameterN = microAdaptiveParameterN;
	}

	/**
	 * @return the macroAdaptiveParameterR
	 */
	public double getMacroAdaptiveParameterR() {
		return macroAdaptiveParameterR;
	}

	/**
	 * @param macroAdaptiveParameterR the macroAdaptiveParameterR to set
	 */
	public void setMacroAdaptiveParameterR(double macroAdaptiveParameterR) {
		this.macroAdaptiveParameterR = macroAdaptiveParameterR;
	}

	/**
	 * @return the macroAdaptiveParameterN
	 */
	public double getMacroAdaptiveParameterN() {
		return macroAdaptiveParameterN;
	}

	/**
	 * @param macroAdaptiveParameterN the macroAdaptiveParameterN to set
	 */
	public void setMacroAdaptiveParameterN(double macroAdaptiveParameterN) {
		this.macroAdaptiveParameterN = macroAdaptiveParameterN;
	}

	/**
	 * @return the distribution
	 */
	public AbstractDelegatedDistribution getDistribution() {
		return distribution;
	}

	/**
	 * @param distribution the distribution to set
	 */
	public void setDistribution(AbstractDelegatedDistribution distribution) {
		this.distribution = distribution;
	}
	

	/**
	 * Generate the byte array structure of the strategy. The structure is as follow:
	 * [macroThreshold][microThreshold][macroAdaptiveParameter][microAdaptiveParameter]
	 * [macroThresholdR][macroThresholdN][macroAdaptiveParameterR][macroAdaptiveParameterN]
	 * [microAdaptiveParameterR][microAdaptiveParameterN]
	 * @return the byte array content
	 */
	@Override
	public byte[] getBytes() {
		ByteBuffer buf = ByteBuffer.allocate(80);
		buf.putDouble(this.macroThreshold);
		buf.putDouble(this.microThreshold);
		buf.putDouble(this.macroAdaptiveParameter);
		buf.putDouble(this.microAdaptiveParameter);
		buf.putDouble(this.macroThresholdR);
		buf.putDouble(this.macroThresholdN);
		buf.putDouble(this.macroAdaptiveParameterR);
		buf.putDouble(this.macroAdaptiveParameterN);
		buf.putDouble(this.microAdaptiveParameterR);
		buf.putDouble(this.microAdaptiveParameterN);
		return buf.array();
	}


	/**
	 * Populates the strategy from the byte array content. The structure should be as follows:
	 * [macroThreshold][microThreshold][macroAdaptiveParameter][microAdaptiveParameter]
	 * [macroThresholdR][macroThresholdN][macroAdaptiveParameterR][macroAdaptiveParameterN]
	 * [microAdaptiveParameterR][microAdaptiveParameterN]
	 * @param content the byte array containing the structure of the strategy
	 * @param pop the Macro Population of agents
	 */	@Override
	public void populateFromBytes(byte[] content, MacroPopulation pop) {
		ByteBuffer buf = ByteBuffer.wrap(content);
		this.macroThreshold = buf.getDouble();
		this.microThreshold = buf.getDouble();
		this.macroAdaptiveParameter= buf.getDouble();
		this.microAdaptiveParameter= buf.getDouble();
		if (buf.remaining() >= 48) {
			this.macroThresholdR = buf.getDouble();
			this.macroThresholdN = buf.getDouble();
			this.macroAdaptiveParameterR = buf.getDouble();
			this.macroAdaptiveParameterN = buf.getDouble();
			this.microAdaptiveParameterR = buf.getDouble();
			this.microAdaptiveParameterN = buf.getDouble();
		} else {
			this.macroThresholdR = Double.NaN;
			this.macroThresholdN = Double.NaN;
			this.macroAdaptiveParameterR = Double.NaN;
			this.macroAdaptiveParameterN = Double.NaN;
			this.microAdaptiveParameterR = Double.NaN;
			this.microAdaptiveParameterN = Double.NaN;
		}
	}

}
