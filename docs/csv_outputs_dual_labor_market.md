# Dual Labor Market CSV Outputs

This note summarizes the CSV series added for the dual labor market (R/N).

## Macro CSV files (data/*.csv)

- `unemploymentR.csv`: Unemployment rate for R-type households.
- `unemploymentN.csv`: Unemployment rate for N-type households.
- `avWageR.csv`: Average reservation wage for R-type households.
- `avWageN.csv`: Average reservation wage for N-type households.
- `employmentR.csv`: Employed headcount for R-type households.
- `employmentN.csv`: Employed headcount for N-type households.
- `laborForceR.csv`: Labor force size for R-type households.
- `laborForceN.csv`: Labor force size for N-type households.

## Definitions

- Unemployment rate: `1 - employed / laborForce` within each type.
- Average wage: mean of `Households.getWage()` within each type.
- Employment: count of `LaborSupplier.isEmployed()` within each type.
- Labor force: count of households within each type.
