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
package benchmark.agents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import benchmark.StaticValues;
import cern.jet.random.engine.RandomEngine;
import jmab.agents.BondSupplier;
import jmab.agents.LaborDemander;
import jmab.agents.LaborSupplier;
import jmab.agents.LiabilitySupplier;
import jmab.agents.MacroAgent;
import jmab.events.MacroTicEvent;
import jmab.population.MacroPopulation;
import jmab.stockmatrix.Deposit;
import jmab.stockmatrix.Item;
import net.sourceforge.jabm.Population;
import net.sourceforge.jabm.SimulationController;
import net.sourceforge.jabm.agent.Agent;
import net.sourceforge.jabm.agent.AgentList;

/**
 * @author Alessandro Caiani and Antoine Godin
 * Note that the government uses a reserve account in the central bank rather than a deposit account due to
 * the bond market.
 */
/**
 * @author Alessandro Caiani and Antoine Godin
 *
 */
@SuppressWarnings("serial")
public class GovernmentAntiCyclical extends Government implements LaborDemander, BondSupplier{

	protected double unemploymentBenefit;
	protected double doleExpenditure;
	protected double profitsFromCB;
	protected double turnoverLaborR;
	protected double turnoverLaborN;
	

	/**
	 * @return the unemploymentBenefit
	 */
	public double getUnemploymentBenefit() {
		return unemploymentBenefit;
	}

	/**
	 * @param unemploymentBenefit the unemploymentBenefit to set
	 */
	public void setUnemploymentBenefit(double unemploymentBenefit) {
		this.unemploymentBenefit = unemploymentBenefit;
	}

	/* (non-Javadoc)
	 * @see jmab.agents.SimpleAbstractAgent#onTicArrived(AgentTicEvent)
	 */
	@Override
	protected void onTicArrived(MacroTicEvent event) {
		switch(event.getTic()){
		case StaticValues.TIC_GOVERNMENTLABOR:
			computeLaborDemand();
			break;
		case StaticValues.TIC_TAXES:
			collectTaxes(event.getSimulationController());
			break;
		case StaticValues.TIC_BONDINTERESTS:
			payInterests();
			break;
		case StaticValues.TIC_BONDSUPPLY:
			receiveCBProfits();
			determineBondsInterestRate();
			emitBonds();
			break;
		case StaticValues.TIC_WAGEPAYMENT:
			payWages();
			payUnemploymentBenefits(event.getSimulationController());
			break;
		case StaticValues.TIC_UPDATEEXPECTATIONS:
			this.updateAggregateVariables();
			break;
		}
	}

	/**
	 * 
	 */
	private void receiveCBProfits() {
		Item deposit=this.getItemStockMatrix(true, StaticValues.SM_RESERVES);
		CentralBank cb=(CentralBank) deposit.getLiabilityHolder();
		deposit.setValue(deposit.getValue()+cb.getCBProfits());
		profitsFromCB=cb.getCBProfits();
	}

	/**
	 * 
	 */
	private void payUnemploymentBenefits(SimulationController simulationController) {
		MacroPopulation macroPop = (MacroPopulation) simulationController.getPopulation();
		Population households= (Population) macroPop.getPopulation(StaticValues.HOUSEHOLDS_ID);
		double wagebillR = 0;
		double wagebillN = 0;
		int employedR = 0;
		int employedN = 0;
		for(Agent agent:households.getAgents()){
			Households worker= (Households) agent;
			if (worker.getEmployer()!=null){
				if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
					wagebillR += worker.getWage();
					employedR += 1;
				} else {
					wagebillN += worker.getWage();
					employedN += 1;
				}
			}
		}
		double totalEmployed = employedR + employedN;
		double averageWage = 0;
		if (totalEmployed > 0) {
			double avgWageR = employedR > 0 ? wagebillR / employedR : 0;
			double avgWageN = employedN > 0 ? wagebillN / employedN : 0;
			averageWage = (avgWageR * employedR + avgWageN * employedN) / totalEmployed;
		}
		double unemploymentBenefit=averageWage*this.unemploymentBenefit;
		double doleAmount=0;
		for(Agent agent:households.getAgents()){
			Households worker= (Households) agent;
			
			if (worker.getEmployer()==null){
				LaborSupplier unemployed = (LaborSupplier) worker;
				Deposit depositGov = (Deposit) this.getItemStockMatrix(true, StaticValues.SM_RESERVES);
				Item payableStock = unemployed.getPayableStock(StaticValues.MKT_LABOR);
				LiabilitySupplier payingSupplier = (LiabilitySupplier) depositGov.getLiabilityHolder();
				payingSupplier.transfer(depositGov, payableStock, unemploymentBenefit);
				doleAmount+=unemploymentBenefit;
			}
		}
		this.doleExpenditure=doleAmount;
	}

	/**
	 * Sets the labor demand equal to the fixed labor demand.
	 * Phase B2.3: Government only hires Regular (R) workers, with type-specific turnover.
	 */
	@Override
	protected void computeLaborDemand() {
		// Legacy labor market is disabled.
		this.setActive(false, StaticValues.MKT_LABOR);
		// Type-specific turnover (Government hires R-only, so only R workers are employed)
		// All employees are R-type, so we only apply turnoverLaborR
		AgentList emplPop = new AgentList();
		for(MacroAgent ag : this.employees)
			emplPop.add(ag);
		emplPop.shuffle(prng);
		int turnoverFire = (int) Math.floor(this.turnoverLaborR * emplPop.size());
		for(int i = 0; i < turnoverFire; i++){
			fireAgent((MacroAgent)emplPop.get(i));
		}
		cleanEmployeeList();

		int currentWorkers = this.employees.size();
		int nbWorkers = this.fixedLaborDemand;
		if(nbWorkers>currentWorkers){
			// Phase B2: Government only hires Regular workers
			this.laborDemandR = nbWorkers - currentWorkers;
			this.laborDemandN = 0;
			this.laborDemand=nbWorkers-currentWorkers;
			// Activate type-specific market
			this.setActive(true, StaticValues.MKT_LABOR_R);
		}else{
			this.setActive(false, StaticValues.MKT_LABOR_R);
			this.laborDemandR = 0;
			this.laborDemandN = 0;
			this.laborDemand=0;
			emplPop = new AgentList();
			for(MacroAgent ag : this.employees)
				emplPop.add(ag);
			emplPop.shuffle(prng);
			for(int i=0;i<currentWorkers-nbWorkers;i++){
				fireAgent((MacroAgent)emplPop.get(i));
			}
		}
		cleanEmployeeList();
	}

	protected void payWages(){
		if(employees.size()>0){
			Deposit deposit = (Deposit) this.getItemStockMatrix(true, StaticValues.SM_RESERVES);
			payWages(deposit,StaticValues.MKT_LABOR_R);
		}
	}

	/**
	 * @return the doleExpenditure
	 */
	public double getDoleExpenditure() {
		return doleExpenditure;
	}

	/**
	 * @param doleExpenditure the doleExpenditure to set
	 */
	public void setDoleExpenditure(double doleExpenditure) {
		this.doleExpenditure = doleExpenditure;
	}


	/**
	 * @return the profitsFromCB
	 */
	public double getProfitsFromCB() {
		return profitsFromCB;
	}

	/**
	 * @param profitsFromCB the profitsFromCB to set
	 */
	public void setProfitsFromCB(double profitsFromCB) {
		this.profitsFromCB = profitsFromCB;
	}

	/**
	 * @return the turnoverLaborR
	 */
	public double getTurnoverLaborR() {
		return turnoverLaborR;
	}

	/**
	 * @param turnoverLaborR the turnoverLaborR to set
	 */
	public void setTurnoverLaborR(double turnoverLaborR) {
		this.turnoverLaborR = turnoverLaborR;
	}

	/**
	 * @return the turnoverLaborN
	 */
	public double getTurnoverLaborN() {
		return turnoverLaborN;
	}

	/**
	 * @param turnoverLaborN the turnoverLaborN to set
	 */
	public void setTurnoverLaborN(double turnoverLaborN) {
		this.turnoverLaborN = turnoverLaborN;
	}	

	
	
	/**
	 * Populates the agent characteristics using the byte array content. The structure is as follows:
	 * [sizeMacroAgentStructure][MacroAgentStructure][bondPrice][bondInterestRate][turnoverLabor][turnoverLaborR][turnoverLaborN][unemploymentBenefit][laborDemand]
	 * [laborDemandR][laborDemandN][fixedLaborDemand][bondMaturity][sizeTaxedPop][taxedPopulations][matrixSize][stockMatrixStructure][expSize][ExpectationStructure]
	 * [passedValSize][PassedValStructure][stratsSize][StrategiesStructure]
	 */
	@Override
	public void populateAgent(byte[] content, MacroPopulation pop) {
		ByteBuffer buf = ByteBuffer.wrap(content);
		byte[] macroBytes = new byte[buf.getInt()];
		buf.get(macroBytes);
		super.populateCharacteristics(macroBytes, pop);
		bondPrice = buf.getDouble();
		bondInterestRate = buf.getDouble();
		turnoverLabor = buf.getDouble();
		// Backward compatibility for turnoverLaborR/N
		if(buf.remaining() >= 40) { // 16 for turnoverLaborR/N + 8 for unemploymentBenefit + 4 for laborDemand + 8 for laborDemandR/N + 8 for fixedLaborDemand/bondMaturity
			turnoverLaborR = buf.getDouble();
			turnoverLaborN = buf.getDouble();
		} else {
			turnoverLaborR = turnoverLabor;
			turnoverLaborN = turnoverLabor;
		}
		unemploymentBenefit = buf.getDouble();
		laborDemand = buf.getInt();

		// Phase B2: Backward compatibility check for laborDemandR/N
		if(buf.remaining() >= 8) {
			this.laborDemandR = buf.getInt();
			this.laborDemandN = buf.getInt();
		} else {
			// Old format: initialize to zero
			this.laborDemandR = 0;
			this.laborDemandN = 0;
		}

		fixedLaborDemand = buf.getInt();
		bondMaturity = buf.getInt();
		int lengthTaxedPopulatiobns = buf.getInt();
		taxedPopulations = new int[lengthTaxedPopulatiobns];
		for(int i = 0 ; i < lengthTaxedPopulatiobns ; i++){
			taxedPopulations[i] = buf.getInt();
		}
		int matSize = buf.getInt();
		if(matSize>0){
			byte[] smBytes = new byte[matSize];
			buf.get(smBytes);
			this.populateStockMatrixBytes(smBytes, pop);
		}
		int expSize = buf.getInt();
		if(expSize>0){
			byte[] expBytes = new byte[expSize];
			buf.get(expBytes);
			this.populateExpectationsBytes(expBytes);
		}
		int lagSize = buf.getInt();
		if(lagSize>0){
			byte[] lagBytes = new byte[lagSize];
			buf.get(lagBytes);
			this.populatePassedValuesBytes(lagBytes);
		}
		int stratSize = buf.getInt();
		if(stratSize>0){
			byte[] stratBytes = new byte[stratSize];
			buf.get(stratBytes);
			this.populateStrategies(stratBytes, pop);
		}
	}
	
	/**
	 * protected ArrayList<MacroAgent> employees;
	protected UnemploymentRateComputer uComputer;
	 * Generates the byte array containing all relevant informations regarding the household agent. The structure is as follows:
	 * [sizeMacroAgentStructure][MacroAgentStructure][bondPrice][bondInterestRate][turnoverLabor][turnoverLaborR][turnoverLaborN][unemploymentBenefit][laborDemand]
	 * [laborDemandR][laborDemandN][fixedLaborDemand][bondMaturity][sizeTaxedPop][taxedPopulations][matrixSize][stockMatrixStructure][expSize][ExpectationStructure]
	 * [passedValSize][PassedValStructure][stratsSize][StrategiesStructure]
	 */
	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			byte[] charBytes = super.getAgentCharacteristicsBytes();
			out.write(ByteBuffer.allocate(4).putInt(charBytes.length).array());
			out.write(charBytes);
			ByteBuffer buf = ByteBuffer.allocate(72+4*taxedPopulations.length); // Phase B2.3: +16 bytes for turnoverLaborR/N
			buf.putDouble(bondPrice);
			buf.putDouble(bondInterestRate);
			buf.putDouble(turnoverLabor);
			buf.putDouble(turnoverLaborR);
			buf.putDouble(turnoverLaborN);
			buf.putDouble(unemploymentBenefit);
			buf.putInt(laborDemand);
			buf.putInt(laborDemandR); // Phase B2
			buf.putInt(laborDemandN); // Phase B2
			buf.putInt(fixedLaborDemand);
			buf.putInt(bondMaturity);
			buf.putInt(taxedPopulations.length);
			for(int i = 0 ; i < taxedPopulations.length ; i++){
				buf.putInt(taxedPopulations[i]);
			}
			out.write(buf.array());
			byte[] smBytes = super.getStockMatrixBytes();
			out.write(ByteBuffer.allocate(4).putInt(smBytes.length).array());
			out.write(smBytes);
			byte[] expBytes = super.getExpectationsBytes();
			out.write(ByteBuffer.allocate(4).putInt(expBytes.length).array());
			out.write(expBytes);
			byte[] passedValBytes = super.getPassedValuesBytes();
			out.write(ByteBuffer.allocate(4).putInt(passedValBytes.length).array());
			out.write(passedValBytes);
			byte[] stratsBytes = super.getStrategiesBytes();
			out.write(ByteBuffer.allocate(4).putInt(stratsBytes.length).array());
			out.write(stratsBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}
}
