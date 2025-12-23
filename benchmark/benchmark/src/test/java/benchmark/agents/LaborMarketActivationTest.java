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
import org.junit.Assert;
import org.junit.Test;

public class LaborMarketActivationTest {

	private static final int NUM_MARKETS = 9;

	@Test
	public void householdsActivateOnlyTheirTypeMarket() {
		Households regular = new Households();
		regular.setNumberMarkets(NUM_MARKETS);
		regular.setLaborType(StaticValues.LABOR_TYPE_R);
		regular.setLaborActive(true);
		Assert.assertTrue(regular.isActive(StaticValues.MKT_LABOR_R));
		Assert.assertFalse(regular.isActive(StaticValues.MKT_LABOR_N));
		Assert.assertFalse(regular.isActive(StaticValues.MKT_LABOR));

		Households nonRegular = new Households();
		nonRegular.setNumberMarkets(NUM_MARKETS);
		nonRegular.setLaborType(StaticValues.LABOR_TYPE_N);
		nonRegular.setLaborActive(true);
		Assert.assertFalse(nonRegular.isActive(StaticValues.MKT_LABOR_R));
		Assert.assertTrue(nonRegular.isActive(StaticValues.MKT_LABOR_N));
		Assert.assertFalse(nonRegular.isActive(StaticValues.MKT_LABOR));
	}

	@Test
	public void consumptionFirmActivatesOnlyDemandedMarkets() {
		ConsumptionFirm firm = new ConsumptionFirm();
		firm.setNumberMarkets(NUM_MARKETS);
		firm.setLaborDemandR(2);
		firm.setLaborDemandN(0);
		firm.setLaborActive(true);
		Assert.assertTrue(firm.isActive(StaticValues.MKT_LABOR_R));
		Assert.assertFalse(firm.isActive(StaticValues.MKT_LABOR_N));
		Assert.assertFalse(firm.isActive(StaticValues.MKT_LABOR));

		firm.setLaborDemandR(0);
		firm.setLaborDemandN(3);
		firm.setLaborActive(true);
		Assert.assertFalse(firm.isActive(StaticValues.MKT_LABOR_R));
		Assert.assertTrue(firm.isActive(StaticValues.MKT_LABOR_N));
		Assert.assertFalse(firm.isActive(StaticValues.MKT_LABOR));
	}

	@Test
	public void governmentActivatesOnlyRegularMarket() {
		Government govt = new Government();
		govt.setNumberMarkets(NUM_MARKETS);
		govt.setLaborActive(true);
		Assert.assertTrue(govt.isActive(StaticValues.MKT_LABOR_R));
		Assert.assertFalse(govt.isActive(StaticValues.MKT_LABOR));
		Assert.assertFalse(govt.isActive(StaticValues.MKT_LABOR_N));
	}
}
