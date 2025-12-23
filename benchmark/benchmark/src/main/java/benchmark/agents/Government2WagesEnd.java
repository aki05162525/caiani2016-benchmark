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

import benchmark.StaticValues;
import jmab.agents.BondSupplier;
import jmab.agents.LaborDemander;
import jmab.agents.MacroAgent;
import jmab.events.MacroTicEvent;
import jmab.stockmatrix.Deposit;
import net.sourceforge.jabm.agent.AgentList;

/**
 * @author Alessandro Caiani and Antoine Godin
 * Note that the government uses a reserve account in the central bank rather than a deposit account due to
 * the bond market.
 */
@SuppressWarnings("serial")
public class Government2WagesEnd extends Government implements LaborDemander, BondSupplier{

	protected double turnoverLaborR;
	protected double turnoverLaborN;


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
			determineBondsInterestRate();
			emitBonds();
			break;
		case StaticValues.TIC_WAGEPAYMENT:
			payWages();
			break;
		case StaticValues.TIC_UPDATEEXPECTATIONS:
			this.updateAggregateVariables();
			break;
		}
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
}
