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
package jmab.agents;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cern.jet.random.engine.RandomEngine;
import jmab.population.MacroPopulation;
import jmab.stockmatrix.Item;
import net.sourceforge.jabm.agent.Agent;
import net.sourceforge.jabm.agent.AgentList;

/**
 * @author Alessandro Caiani and Antoine Godin
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractFirm extends SimpleAbstractAgent implements LaborDemander {

	protected ArrayList<MacroAgent> employees;
	protected List<MacroAgent> clients;
	protected int laborDemand; // Legacy: will be deprecated in favor of laborDemandR/N
	protected int laborDemandR; // Phase A4: Regular labor demand
	protected int laborDemandN; // Phase A4: Non-regular labor demand
	protected double desiredOutput;
	protected int productionStockId;
	protected int loanLength;
	protected int loanAmortizationType;
	protected double dividends;
	protected double bailoutCost;
	protected RandomEngine prng;
	
	/**
	 * 
	 */
	public AbstractFirm() {
		super();
		this.employees = new ArrayList <MacroAgent>();
		this.clients = new ArrayList <MacroAgent>();
	}

	/**
	 * @return the employees
	 */
	public List<MacroAgent> getEmployees() {
		return employees;
	}

	/**
	 * @param employees the employees to set
	 */
	public void setEmployees(ArrayList<MacroAgent> employees) {
		this.employees = employees;
	}
	
	/**
	 * @return the clients
	 */
	public List<MacroAgent> getClients() {
		return clients;
	}

	/**
	 * @param clients the clients to set
	 */
	public void setClients(List<MacroAgent> clients) {
		this.clients = clients;
	}
	
	/* (non-Javadoc)
	 * @see jmab.agents.CreditDemander#getLoanLength()
	 */
	
	public int decideLoanLength(int idLoanSM) {
		return this.loanLength;
	}

	/* (non-Javadoc)
	 * @see jmab.agents.CreditDemander#getLoanAmortization()
	 */
	
	public int decideLoanAmortizationType(int idLoanSM) {
		return this.loanAmortizationType;
	}
	/**
	 * @return the loanLength
	 */
	public int getLoanLength() {
		return loanLength;
	}

	/**
	 * @param loanLength the loanLength to set
	 */
	public void setLoanLength(int loanLength) {
		this.loanLength = loanLength;
	}

	/**
	 * @return the loanAmortization
	 */
	public int getLoanAmortizationType() {
		return loanAmortizationType;
	}

	/**
	 * @param loanAmortizationType the loanAmortization to set
	 */
	public void setLoanAmortizationType(int loanAmortizationType) {
		this.loanAmortizationType = loanAmortizationType;
	}

	/**
	 * Fires the employee and removes the connection between employee and employer
	 * @param employee the employee to fire
	 */
	public void fireAgent(MacroAgent employee){
		LaborSupplier emp = (LaborSupplier) employee;
		emp.setEmployer(null);
		emp.setLaborActive(true);//the fired workers is reactivated in the labor market
	}
	
	/* (non-Javadoc)
	 * @see jmab.agents.LaborDemander#addEmployee(jmab.agents.LaborSupplier)
	 */
	public void addEmployee(LaborSupplier worker) {
		// Phase A4: Decrease type-specific labor demand
		int laborType = worker.getLaborType();
		if(laborType == 0) { // LABOR_TYPE_R
			this.laborDemandR -= 1;
		} else { // LABOR_TYPE_N
			this.laborDemandN -= 1;
		}
		// Legacy: also decrease total demand for backward compatibility
		this.laborDemand -= 1;
		this.employees.add(worker);
		worker.setEmployer(this);
	}

	/**
	 * @return the laborDemand
	 */
	public int getLaborDemand() {
		return laborDemand;
	}

	/**
	 * @param laborDemand the laborDemand to set
	 */
	public void setLaborDemand(int laborDemand) {
		this.laborDemand = laborDemand;
	}

	/**
	 * @return the laborDemandR
	 */
	public int getLaborDemandR() {
		return laborDemandR;
	}

	/**
	 * @param laborDemandR the laborDemandR to set
	 */
	public void setLaborDemandR(int laborDemandR) {
		this.laborDemandR = laborDemandR;
	}

	/**
	 * @return the laborDemandN
	 */
	public int getLaborDemandN() {
		return laborDemandN;
	}

	/**
	 * @param laborDemandN the laborDemandN to set
	 */
	public void setLaborDemandN(int laborDemandN) {
		this.laborDemandN = laborDemandN;
	}

	/**
	 * @return the productionStockId
	 */
	public int getProductionStockId() {
		return productionStockId;
	}

	/**
	 * @param productionStockId the productionStockId to set
	 */
	public void setProductionStockId(int productionStockId) {
		this.productionStockId = productionStockId;
	}
	
	/**
	 * @return the desiredOutput
	 */
	public double getDesiredOutput() {
		return desiredOutput;
	}

	/**
	 * @param desiredOutput the desiredOutput to set
	 */
	public void setDesiredOutput(double desiredOutput) {
		this.desiredOutput = desiredOutput;
	}
	
	/**
	 * @return
	 */
	public double getWageBill() {
		double wageBill=0;
		for(MacroAgent employee:employees){
			wageBill+=((LaborSupplier)employee).getWage();
		}
		return wageBill;
	}
	
	protected void payWages(Item payingItem, int idMarket) {
		int currentWorkers = this.employees.size();
		AgentList emplPop = new AgentList();
		for(MacroAgent ag : this.employees)
			emplPop.add(ag);
		emplPop.shuffle(prng);
		for(int i=0;i<currentWorkers;i++){
			LaborSupplier employee = (LaborSupplier) emplPop.get(i);
			double wage = employee.getWage();
			if(wage<payingItem.getValue()){
				Item payableStock = employee.getPayableStock(idMarket);
				LiabilitySupplier payingSupplier = (LiabilitySupplier) payingItem.getLiabilityHolder();
				payingSupplier.transfer(payingItem, payableStock, wage);
			}else{
				// Phase A4: Re-hire based on worker's labor type
				int laborType = employee.getLaborType();
				this.setLaborActive(true);
				fireAgent(employee);
				// Increase type-specific demand
				if(laborType == 0) { // LABOR_TYPE_R
					this.laborDemandR += 1;
				} else { // LABOR_TYPE_N
					this.laborDemandN += 1;
				}
				// Legacy: also increase total demand for backward compatibility
				this.laborDemand += 1;
			}

		}
	}
	
	public void cleanEmployeeList(){
		ArrayList<MacroAgent> newEmployee = new ArrayList<MacroAgent>();
		for(MacroAgent employee:employees){
			if(((LaborSupplier) employee).getEmployer()!=null){
				newEmployee.add(employee);
			}
		}
		this.employees=newEmployee;
	}

	/**
	 * @return the dividends
	 */
	public double getDividends() {
		return dividends;
	}

	/**
	 * @param dividends the dividends to set
	 */
	public void setDividends(double dividends) {
		this.dividends = dividends;
	}

	/**
	 * @return the bailoutCost
	 */
	public double getBailoutCost() {
		return bailoutCost;
	}

	/**
	 * @param bailoutCost the bailoutCost to set
	 */
	public void setBailoutCost(double bailoutCost) {
		this.bailoutCost = bailoutCost;
	}
	
	


	/**
	 * Generates the byte array representing the characteristics of the agent. The structure is the following
	 * [superStructSize][superStruct][desiredOutput][laborDemand][laborDemandR][laborDemandN][productionStockId][loanLength][loanAmortizationType][clientSize]
	 * for each client
	 * 	[clientPopId][clientId]
	 * end for
	 * @return the byte array
	 */
	@Override
	public byte[] getAgentCharacteristicsBytes(){
		byte[] superStruct = super.getAgentCharacteristicsBytes();
		int nbClients = this.clients.size();
		ByteBuffer buf = ByteBuffer.allocate(superStruct.length+40+12*nbClients); // Phase A4: 32+8 for laborDemandR/N
		buf.putInt(superStruct.length);
		buf.put(superStruct);
		buf.putDouble(desiredOutput);
		buf.putInt(laborDemand);
		buf.putInt(laborDemandR); // Phase A4
		buf.putInt(laborDemandN); // Phase A4
		buf.putInt(productionStockId);
		buf.putInt(loanLength);
		buf.putInt(loanAmortizationType);
		buf.putInt(nbClients);
		for(MacroAgent client:clients){
			buf.putInt(client.getPopulationId());
			buf.putLong(client.getAgentId());
		}
		return buf.array();
	}

	/**
	 * Populates the characteristics of the agent using the byte array content. The structure is the following
	 * [superStructSize][superStruct][desiredOutput][laborDemand][laborDemandR][laborDemandN][productionStockId][loanLength][loanAmortizationType]
	 * [clientSize]
	 * for each client
	 * 	[clientPopId][clientId]
	 * end for
	 * @param the byte array
	 */
	public void populateCharacteristics(byte[] content, MacroPopulation pop){
		ByteBuffer buf = ByteBuffer.wrap(content);
		int superStrucSize = buf.getInt();
		byte[] superStruct = new byte[superStrucSize];
		buf.get(superStruct);
		super.populateCharacteristics(superStruct, pop);
		this.desiredOutput = buf.getDouble();
		this.laborDemand = buf.getInt();

		// Phase B2: Backward compatibility check for laborDemandR/N
		// Check if buffer has enough data for new fields (need 2 ints = 8 bytes before productionStockId)
		int positionBeforeNewFields = buf.position();
		if(buf.remaining() >= 8) {
			// Try to peek ahead - if the next values look like valid labor demands, read them
			// Otherwise, assume old format
			this.laborDemandR = buf.getInt();
			this.laborDemandN = buf.getInt();
		} else {
			// Old format: initialize to zero, will be set in computeLaborDemand
			this.laborDemandR = 0;
			this.laborDemandN = 0;
		}

		this.productionStockId = buf.getInt();
		this.loanLength = buf.getInt();
		this.loanAmortizationType = buf.getInt();
		int nbClients = buf.getInt();
		for(int i = 0 ; i < nbClients ; i++){
			int popId = buf.getInt();
			long clientId = buf.getLong();
			Collection<Agent> potClients = pop.getPopulation(popId).getAgents();
			for(Agent a:potClients){
				MacroAgent potClient = (MacroAgent) a;
				if(potClient.getAgentId()==clientId){
					clients.add(potClient);
					break;
				}
			}
		}
	}

	public RandomEngine getPrng() {
		return prng;
	}

	public void setPrng(RandomEngine prng) {
		this.prng = prng;
	}
	
	
	
}
