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

import jmab.events.MacroTicEvent;
import jmab.population.MacroPopulation;
import net.sourceforge.jabm.agent.Agent;
import net.sourceforge.jabm.event.AgentArrivalEvent;
import net.sourceforge.jabm.event.RoundFinishedEvent;
import jmab.stockmatrix.Item;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class AbstractFirmCesTest {

	private static final double TOL = 1e-8;

	@Test
	public void computeEffectiveLaborIsNonNegativeAndMonotone() {
		TestFirm firm = new TestFirm();
		firm.setCesDelta(0.6);
		firm.setCesRho(-0.5);
		firm.setCesAR(1.0);
		firm.setCesAN(1.0);
		firm.setCesEpsilon(1e-8);

		double base = firm.effectiveLabor(0.0, 0.0);
		Assert.assertEquals(0.0, base, TOL);

		double nUpR = firm.effectiveLabor(5.0, 0.0);
		double nUpN = firm.effectiveLabor(0.0, 5.0);
		double bothUp = firm.effectiveLabor(5.0, 5.0);

		Assert.assertTrue(nUpR >= 0.0);
		Assert.assertTrue(nUpN >= 0.0);
		Assert.assertTrue(bothUp >= nUpR);
		Assert.assertTrue(bothUp >= nUpN);
	}

	@Test
	public void laborSplitRecoversTotalEffectiveLabor() {
		TestFirm firm = new TestFirm();
		firm.setCesDelta(0.5);
		firm.setCesRho(-0.5);
		firm.setCesAR(1.0);
		firm.setCesAN(1.0);
		firm.setCesEpsilon(1e-8);
		firm.setPhiMin(0.2);
		firm.setPhiMax(5.0);

		double nTotal = 100.0;
		double ratio = 2.0;
		double[] split = firm.laborSplit(nTotal, ratio);
		double effective = firm.effectiveLabor(split[0], split[1]);
		Assert.assertEquals(nTotal, effective, 1e-6);
	}

	@Test
	public void laborRatioIsClippedToBounds() {
		TestFirm firm = new TestFirm();
		firm.setCesDelta(0.5);
		firm.setCesRho(-0.5);
		firm.setCesAR(1.0);
		firm.setCesAN(1.0);
		firm.setCesEpsilon(1e-8);
		firm.setPhiMin(0.5);
		firm.setPhiMax(1.5);

		double ratio = firm.laborRatio(100.0, 1.0);
		Assert.assertTrue(ratio >= 0.5 && ratio <= 1.5);
	}

	@Test
	public void testComputeEffectiveLaborMinimumBound() {
		// 極小値が1.0に制限されることを確認（GDP問題対策）
		TestFirm firm = new TestFirm();
		firm.setCesDelta(0.5);
		firm.setCesRho(0.5);
		firm.setCesAR(1.0);
		firm.setCesAN(1.0);
		firm.setCesEpsilon(1e-8);

		// nR=1, nN=0のケース（理論値は0.25未満になり得る）
		double result = firm.effectiveLabor(1.0, 0.0);
		Assert.assertTrue("Effective labor should be >= 1.0 when workers exist", result >= 1.0);

		// nR=0, nN=1のケース
		result = firm.effectiveLabor(0.0, 1.0);
		Assert.assertTrue("Effective labor should be >= 1.0 when workers exist", result >= 1.0);

		// 両方がいる場合も確認
		result = firm.effectiveLabor(1.0, 1.0);
		Assert.assertTrue("Effective labor should be >= 1.0 when workers exist", result >= 1.0);
	}

	@Test
	public void testComputeEffectiveLaborZeroWorkers() {
		// 雇用者ゼロの場合は下限なし（0.0を返す）
		TestFirm firm = new TestFirm();
		firm.setCesDelta(0.5);
		firm.setCesRho(0.5);
		firm.setCesAR(1.0);
		firm.setCesAN(1.0);
		firm.setCesEpsilon(1e-8);

		double result = firm.effectiveLabor(0.0, 0.0);
		Assert.assertEquals("Effective labor should be 0.0 when no workers exist", 0.0, result, TOL);
	}

	private static final class TestFirm extends AbstractFirm {
		@Override
		public void onAgentArrival(AgentArrivalEvent event) {
			// No-op for test stub.
		}

		@Override
		protected void onTicArrived(MacroTicEvent event) {
			// No-op for test stub.
		}

		@Override
		public void populateAgent(byte[] content, MacroPopulation pop) {
			// No-op for test stub.
		}

		@Override
		public byte[] getBytes() {
			return new byte[0];
		}

		@Override
		public void populateStockMatrixBytes(byte[] content, MacroPopulation pop) {
			// No-op for test stub.
		}

		@Override
		public void initialiseCounterpart(Agent counterpart, int marketID) {
			// No-op for test stub.
		}

		@Override
		public void onRoundFinished(RoundFinishedEvent event) {
			// No-op for test stub.
		}

		@Override
		public void setLaborActive(boolean active) {
			// No-op for test stub.
		}

		@Override
		public List<Item> getPayingStocks(int idMarket, Item payableStock) {
			return null;
		}

		double effectiveLabor(double nR, double nN) {
			return computeEffectiveLabor(nR, nN);
		}

		double[] laborSplit(double nTotal, double ratio) {
			return computeLaborSplit(nTotal, ratio);
		}

		double laborRatio(double wageR, double wageN) {
			return computeLaborRatio(wageR, wageN);
		}
	}
}
