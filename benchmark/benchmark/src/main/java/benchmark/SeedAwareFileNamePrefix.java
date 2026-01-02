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
package benchmark;

import java.io.File;

import net.sourceforge.jabm.prng.MersenneTwister;
import net.sourceforge.jabm.util.MutableStringWrapper;

import org.springframework.beans.factory.InitializingBean;

/**
 * MutableStringWrapperを拡張し、seed値に基づいたディレクトリパスを生成する。
 * 出力先: {basePath}/seed_{seedValue}/
 */
public class SeedAwareFileNamePrefix extends MutableStringWrapper implements InitializingBean {

	private String basePath = "../../data/";
	private MersenneTwister prng;
	private boolean directoryCreated = false;

	public SeedAwareFileNamePrefix() {
		super();
	}

	public SeedAwareFileNamePrefix(String basePath) {
		super();
		this.basePath = basePath;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		updatePath();
	}

	private void updatePath() {
		if (prng != null) {
			int seed = prng.getSeed();
			this.value = basePath + "seed_" + seed + "/";
			createDirectoryIfNeeded();
		} else {
			this.value = basePath;
		}
	}

	private void createDirectoryIfNeeded() {
		if (!directoryCreated && this.value != null) {
			File dir = new File(this.value);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			directoryCreated = true;
		}
	}

	@Override
	public String toString() {
		if (!directoryCreated) {
			createDirectoryIfNeeded();
		}
		return this.value;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
		updatePath();
	}

	public MersenneTwister getPrng() {
		return prng;
	}

	public void setPrng(MersenneTwister prng) {
		this.prng = prng;
		updatePath();
	}
}
