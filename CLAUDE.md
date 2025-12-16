# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an implementation of the Caiani & Godin (2016) benchmark model - a Stock-Flow Consistent Agent-Based Macroeconomic (AB-SFC) simulation. The model simulates a complete macroeconomic system with multiple agent types (firms, banks, households, government, central bank) interacting through various markets.

**Key Reference:** Caiani, A., Godin, A., et al. (2016). "Agent based-stock flow consistent macroeconomics: Towards a benchmark model." *Journal of Economic Dynamics and Control*, 69, 375-408.

## Architecture

### Three-Layer Structure

The project consists of three dependent components:

```
benchmark (application layer)
    ↓ depends on
jmab (macroeconomic modeling framework)
    ↓ depends on
jabm (general agent-based modeling toolkit)
```

**jabm** - Java Agent-Based Modelling toolkit (Maven project)
- General-purpose ABM framework using Spring dependency injection
- Built with Maven (`mvn clean install`)
- Provides core simulation infrastructure via `SimulationManager`

**jmab** - Java Macro Agent-Based toolkit (non-Maven)
- Stock-Flow Consistent macroeconomic agent framework
- Provides abstract agent types (AbstractBank, AbstractFirm, AbstractHousehold)
- Implements market mechanisms, population handlers, and reporting infrastructure
- Key packages: `jmab.agents`, `jmab.mechanisms`, `jmab.events`, `jmab.expectations`

**benchmark** - The specific Caiani 2016 model implementation (non-Maven)
- Concrete agent implementations for the benchmark model
- 6 agent types: CapitalFirmWagesEnd, ConsumptionFirmWagesEnd, Bank, HouseholdsWithDole, GovernmentAntiCyclical, CentralBank
- Economic strategies and reporting specific to the model

### Configuration System

The model uses Spring XML configuration files for all setup:

- **Main configs:** `benchmark/benchmark/Model/mainBenchmark_light.xml`, `mainBenchmark_full.xml`
  - Define simulation parameters (number of runs, rounds, output settings)
  - Reference the model and report configurations

- **Model configs:** `modelBenchmark_light.xml`, `modelBenchmark_full.xml`
  - Define all agents, populations, markets, strategies, and parameters
  - Control economic behavior (wages, prices, credit, investment, etc.)

- **Report configs:** `reports.xml`
  - Define 50+ macroeconomic indicators to output as CSV
  - Examples: GDP, unemployment, investment, credit supply, profits

### Simulation Flow

The simulation runs in discrete time steps ("tics"). Each round consists of 38 sequential tics defined in `StaticValues.java`:

1. **TIC_COMPUTEEXPECTATIONS** (0) - Agents form expectations
2. **TIC_CAPITALPRICE** (1) - Capital goods pricing
3. **TIC_CONSUMPTIONPRICE** (2) - Consumption goods pricing
4. **TIC_CAPITALMARKET1** (3) - First capital market
5. **TIC_RDDECISION** (4) - R&D decisions
6. **TIC_INVESTMENTDEMAND** (5) - Investment demand
7. **TIC_CREDITSUPPLY** (6) - Credit supply decisions
8. **TIC_DEPOSITSUPPLY** (7) - Deposit supply
9. **TIC_DEPINTERESTS** (8) - Deposit interest calculation
10. **TIC_CREDITDEMAND** (9) - Credit demand
11. **TIC_CREDITMARKET** (10) - Credit market clearing
12. **TIC_LABORSUPPLY** (11) - Labor supply
13. **TIC_LABORDEMAND** (12) - Labor demand
14. **TIC_GOVERNMENTLABOR** (13) - Government employment
15. **TIC_LABORMARKET** (14) - Labor market clearing
16. **TIC_PRODUCTION** (15) - Production occurs
17. **TIC_RDOUTCOME** (16) - R&D outcomes
18. **TIC_CONSUMPTIONDEMAND** (17) - Consumption demand
19. **TIC_CONSUMPTIONMARKET** (18) - Consumption market clearing
20. **TIC_CAPITALMARKET2** (19) - Second capital market
21. **TIC_CREDINTERESTS** (20) - Credit interest payment
22. **TIC_WAGEPAYMENT** (21) - Wage payment
23. **TIC_BONDINTERESTS** (22) - Bond interest
24. **TIC_ADVINTERESTS** (23) - Advance interest
25. **TIC_TAXES** (24) - Tax collection
26. **TIC_DIVIDENDS** (25) - Dividend distribution
27. **TIC_DEPOSITDEMAND** (26) - Deposit demand
28. **TIC_DEPOSITMARKET** (27) - Deposit market clearing
... continues through TIC 37

## Build and Test Commands

### Building jabm (Maven)

jabm must be built first as it's a dependency for the other components:

```bash
# Navigate to jabm directory
cd jabm

# Full build with tests
mvn clean install

# Build without tests (faster)
mvn clean install -DskipTests
```

**Important:** Before building jabm on Windows, you must apply several fixes:
1. Update `jabm/jabm-examples/pom.xml` version to `0.9.2-SNAPSHOT`
2. Configure Maven settings to allow HTTP for jabm repository (`~/.m2/settings.xml`)
3. Update `jabm/jabm/pom.xml` to reference local JAR files (bsh, idw, pf-joi) using `<systemPath>`

### Building jmab and benchmark

These are non-Maven projects. Build them in IntelliJ IDEA:
1. Open the root project in IntelliJ
2. Configure modules: jmab sources at `jmab/src`, benchmark sources at `benchmark/benchmark/src`
3. Add module dependencies: benchmark → jmab → jabm
4. Add JAR dependencies from `jmab/lib/` and `benchmark/benchmark/lib/`
5. Build → Rebuild Project

### Running the Simulation

**Main class:** `benchmark.Main`

**Required VM argument:** `-Djabm.config=Model/mainBenchmark_light.xml`

**Working directory:** `benchmark/benchmark/`

**IntelliJ Run Configuration:**
```
Name: Benchmark Main (Light)
Main class: benchmark.Main
VM options: -Djabm.config=Model/mainBenchmark_light.xml
Working directory: C:\Users\Akihi\Develop\seminar\caiani2016-benchmark\benchmark\benchmark
Use classpath of module: benchmark
```

**Available configuration files:**
- `Model/mainBenchmark_light.xml` - Light version (fewer agents, faster)
- `Model/mainBenchmark_full.xml` - Full version (complete simulation)
- `Model/mainSerialization.xml` - Serialization test

**Output:** CSV files in `benchmark/benchmark/data/` directory

### Testing

The jabm module contains JUnit tests. Run them via:
```bash
cd jabm
mvn test
```

Tests are located in `jabm/jabm/src/test/java/net/sourceforge/jabm/`

## Code Organization

### Agent Implementations (benchmark/benchmark/src/benchmark/agents/)

- `CapitalFirmWagesEnd.java` - Produces capital goods
- `ConsumptionFirmWagesEnd.java` - Produces consumption goods
- `Bank.java` - Provides credit and deposits
- `HouseholdsWithDole.java` - Workers/consumers with unemployment benefits
- `GovernmentAntiCyclical.java` - Government with fiscal policy
- `CentralBank.java` - Monetary authority

### Strategies (benchmark/benchmark/src/benchmark/strategies/)

Economic decision-making strategies:
- `AdaptiveMarkUpAveragePrice.java` - Price setting strategy
- `AdaptiveInterestRateAverageThreshold.java` - Interest rate strategy
- `InvestmentCapacityOperatingCashFlowExpected.java` - Investment decisions
- `BankBankruptcyBailout.java` - Bank failure handling
- `FirmBankruptcyFireSales.java` - Firm bankruptcy with asset sales
- `FullBondDemandStrategy.java` - Government bond demand
- `BaselIIIReserveRequirementsBLR.java` - Banking regulation

### Framework Components (jmab/src/jmab/)

**Agents (interfaces):**
- Abstract base classes for different agent roles
- Interfaces define market participation (GoodSupplier, GoodDemander, CreditSupplier, etc.)

**Mechanisms (jmab/mechanisms/):**
- Market clearing algorithms
- `CreditMechanism`, `LaborMechanism`, `GoodMechanism`, etc.
- Handle matching buyers/sellers and executing transactions

**Events (jmab/events/):**
- `MacroTicEvent` - Simulation time step
- `MarketInteractionsStartingEvent` / `MarketInteractionsFinishedEvent`
- `BadDebtEvent`, `DeadAgentEvent`, `TransactionOccuredEvent`

**Expectations (jmab/expectations/):**
- `AdaptiveExpectation` - Backward-looking expectations
- `TrendFollowingExpectation` - Extrapolative expectations
- Agents use these to form expectations about prices, demand, etc.

**Reports (jmab/report/):**
- Compute macroeconomic aggregates
- Micro-level data collection
- Output CSV time series

## Important Spring XML Fixes (Windows)

All XML files in `benchmark/benchmark/Model/` must have `<idref local=` replaced with `<idref bean=` due to Spring version compatibility:

```bash
# PowerShell
cd benchmark\benchmark\Model
Get-ChildItem -Recurse -Filter *.xml | ForEach-Object {
    (Get-Content $_.FullName) -replace '<idref local=', '<idref bean=' | Set-Content $_.FullName
}

# Git Bash
cd benchmark/benchmark/Model
find . -name "*.xml" -type f -exec sed -i 's/<idref local=/<idref bean=/g' {} +
```

## Parameter Modification

### Common Parameters to Modify

Economic parameters are defined in `modelBenchmark_light.xml` and `modelBenchmark_full.xml`:

**Population sizes:**
- `<entry key="cFirms" value="80"/>` - Number of consumption firms
- `<entry key="kFirms" value="40"/>` - Number of capital firms
- `<entry key="banks" value="10"/>` - Number of banks
- `<entry key="hhs" value="500"/>` - Number of households

**Household parameters:**
- `unemploymentBenefit` - Unemployment benefit rate (default: 0.4)
- `shareWorkers` - Share of working-age population (default: 1.0)
- `shareCapitalOwners` / `shareBankOwners` - Wealth ownership distribution

**Firm parameters:**
- `targetCapacityUtilization` - Target capacity utilization (default: 0.8)
- `targetLiquidity` - Target liquidity ratio
- `markupRate` - Price markup over costs
- `adaptiveParameter` - Speed of price/wage adjustment

**Bank parameters:**
- `riskAversion` - Credit rationing intensity
- `targetCapitalAdequacyRatio` - Capital requirement target
- `bailoutCost` - Cost of bank bailouts

**Government parameters:**
- `shareGDPGovernmentExpenditure` - Government spending as % of GDP
- `taxRateProfits` - Corporate tax rate
- `taxRateWages` - Income tax rate

**Central Bank:**
- `advancesRate` - Lending rate to banks
- `reserveInterestRate` - Interest on reserves

### How to Modify Parameters

1. Open `benchmark/benchmark/Model/modelBenchmark_light.xml`
2. Search for the bean definition (e.g., `<bean id="households"`)
3. Locate the parameter property (e.g., `<property name="unemploymentBenefit"`)
4. Modify the value: `<property name="unemploymentBenefit" value="0.5"/>`
5. Save and re-run the simulation

## Dependencies and Libraries

### jabm dependencies (Maven-managed)
- Spring Framework 3.2.3+ (dependency injection)
- Apache Commons Math 3.2 (numerical methods)
- Colt 1.2.0 (scientific computing)
- Log4j 1.2.16 (logging)
- JFreeChart 1.0.14 (charting)
- JUNG 2.0.1 (network/graph analysis)

### Manual JAR dependencies
- jmab: `jmab/lib/` - Spring, Colt, Log4j
- benchmark: `benchmark/benchmark/lib/` - All above plus JFreeChart, JUNG, Commons collections

## Output Analysis

Simulation results are written to `benchmark/benchmark/data/` as CSV files:

**Key aggregate indicators:**
- `nominalGDPCSVReport` - GDP over time
- `unemploymentCSVReport` - Unemployment rate
- `nominalInvestmentCSVReport` - Investment
- `inflationCSVReport` - Inflation rate
- `wagesBillCSVReport` - Total wages

**Micro-level data:**
- `microBankCreditCSVReport` - Individual bank credit supply
- `microProfitsCSVReport` - Firm-level profits
- `microDepositsCSVReport` - Household deposits

Each report generates a CSV with columns: [round, tick, value] or [round, tick, agent_id, value]

## Troubleshooting

### ClassNotFoundException for jabm classes
**Solution:** Ensure jabm is built with `mvn clean install` and the jabm module is added as a dependency to jmab and benchmark in IntelliJ Project Structure.

### FileNotFoundException for XML config
**Solution:** Verify the Run Configuration Working directory is set to `benchmark/benchmark/`

### Maven dependency resolution errors (infoNode, pf-joi, bsh)
**Solution:** Update `jabm/jabm/pom.xml` to use `<systemPath>` for these JARs pointing to local lib files, then clear Maven cache and reload.

### Spring XML parsing error ("Attribute 'local' not allowed")
**Solution:** Run the sed/PowerShell command to replace `<idref local=` with `<idref bean=` in all Model XML files.

### OutOfMemoryError during simulation
**Solution:** Increase heap size in VM options: `-Xmx4g -Djabm.config=Model/mainBenchmark_light.xml`

## Development Workflow

1. **Setup:** Follow `setup-windows.md` for complete environment setup
2. **Modify model:** Edit agent classes in `benchmark/benchmark/src/benchmark/agents/`
3. **Adjust parameters:** Edit XML in `benchmark/benchmark/Model/`
4. **Build:** Use IntelliJ "Build → Build Project"
5. **Run:** Execute Run Configuration with appropriate VM args
6. **Analyze:** Check CSV outputs in `data/` directory
7. **Iterate:** Adjust and re-run

## Key Entry Points for Code Navigation

- **Simulation start:** `benchmark.Main.main()` → `SimulationManager.main()`
- **Agent scheduling:** Check `modelBenchmark_light.xml` for `<bean id="scheduler">`
- **Market mechanisms:** `jmab.mechanisms.*` classes referenced in XML config
- **Event handling:** Look for `EventListener` beans in XML and corresponding handlers in `jmab.report.*`
- **Economic constants:** `benchmark.StaticValues` interface defines all tic IDs

## Documentation

Detailed documentation is available in:
- `docs/model-analysis.md` - Comprehensive model analysis (agent details, simulation flow, parameters)
- `docs/parameter-quick-reference.md` - Quick reference for parameter modification
- `setup-windows.md` - Windows-specific setup instructions
- `jmab/README.md` - JMAB framework overview
- `benchmark/README.md` - Benchmark model specific information
