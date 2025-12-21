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
- `unemploymentCountR.csv`: Unemployed headcount for R-type households.
- `unemploymentCountN.csv`: Unemployed headcount for N-type households.
- `laborForceGapR.csv`: Labor force identity gap for R (LF - (E + U)).
- `laborForceGapN.csv`: Labor force identity gap for N (LF - (E + U)).
- `unemploymentBoundsR.csv`: 1 when u_R is out of bounds or LF_R=0, else 0.
- `unemploymentBoundsN.csv`: 1 when u_N is out of bounds or LF_N=0, else 0.
- `vacanciesR.csv`: Total vacancies (laborDemandR) across firms/government.
- `vacanciesN.csv`: Total vacancies (laborDemandN) across firms.

## Definitions

- Unemployment rate: `1 - employed / laborForce` within each type.
- Average wage: mean of `Households.getWage()` within each type.
- Employment: count of `LaborSupplier.isEmployed()` within each type.
- Labor force: count of households within each type.
- Unemployment count: count of households with `isEmployed() == false` within each type.
- Labor force identity gap: `LF - (E + U)` within each type (should be 0).
- Unemployment bounds violation: 1 if `LF==0` or unemployment rate outside [0,1], else 0.
- Vacancies: sum of `laborDemandR/N` across firms (and government for R).
