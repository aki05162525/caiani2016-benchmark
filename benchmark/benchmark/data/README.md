# CSV出力ファイル解説

このディレクトリには、シミュレーション実行時に出力されるCSVファイルが格納されます。
ファイル名の末尾の数字（例: `1`）はシミュレーション番号を表します。

## ファイル形式

すべてのCSVファイルは以下の形式です：
- 1列目: ラウンド番号（シミュレーション期間）
- 2列目以降: 各変数の値

---

## 1. マクロ経済指標

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `nominalGDP` | 名目GDP | 金額 |
| `nominalInvestment` | 名目投資額 | 金額 |

---

## 2. 価格指標

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `cAvPrice` | 消費財の平均価格（売上高加重） | 価格 |
| `kAvPrice` | 資本財の平均価格（売上高加重） | 価格 |

---

## 3. 労働市場指標

### 3.1 全体

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `unemployment` | 全体失業率 | 比率 |
| `hhAvWage` | 家計平均賃金 | 金額 |
| `hhAvgTenure` | 平均勤続年数（就業者のみ） | 期間数 |

### 3.2 正規労働者 (R: Regular)

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `laborForceR` | R労働力人口 | 人数 |
| `employmentR` | R雇用者数 | 人数 |
| `unemploymentR` | R失業率 | 比率 |
| `unemploymentCountR` | R失業者数 | 人数 |
| `unemploymentBoundsR` | R失業限界値（賃金調整閾値） | 比率 |
| `vacanciesR` | R求人数 | 件数 |
| `laborForceGapR` | R労働力ギャップ | 人数 |
| `avWageR` | R平均賃金 | 金額 |
| `hhAvgTenureR` | R平均勤続年数（就業者のみ） | 期間数 |

### 3.3 非正規労働者 (N: Non-regular)

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `laborForceN` | N労働力人口 | 人数 |
| `employmentN` | N雇用者数 | 人数 |
| `unemploymentN` | N失業率 | 比率 |
| `unemploymentCountN` | N失業者数 | 人数 |
| `unemploymentBoundsN` | N失業限界値（賃金調整閾値） | 比率 |
| `vacanciesN` | N求人数 | 件数 |
| `laborForceGapN` | N労働力ギャップ | 人数 |
| `avWageN` | N平均賃金 | 金額 |
| `hhAvgTenureN` | N平均勤続年数（就業者のみ） | 期間数 |

---

## 4. 家計セクター (hh)

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `hhAvWage` | 平均賃金 | 金額 |
| `hhAvNetIncome` | 平均純所得 | 金額 |
| `hhWages` | 各家計の賃金（ミクロデータ） | 金額 |
| `hhNetIncome` | 各家計の純所得（ミクロデータ） | 金額 |
| `hhDeposits` | 各家計の預金残高（ミクロデータ） | 金額 |
| `hhRealConsumption` | 各家計の実質消費（ミクロデータ） | 数量 |
| `hhRealDesiredConsumption` | 各家計の希望実質消費（ミクロデータ） | 数量 |
| `hhNominalConsumption` | 各家計の名目消費（ミクロデータ） | 金額 |
| `hhInterestsReceived` | 各家計の受取利子（ミクロデータ） | 金額 |
| `hhTaxes` | 各家計の納税額（ミクロデータ） | 金額 |

---

## 5. 消費財企業セクター (cFirms)

### 5.1 集計指標

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `cFirmsAggregateDebt` | 総負債 | 金額 |
| `cFirmsBankrupcty` | 倒産件数 | 件数 |
| `cAggConsCredit` | 信用制約された企業の割合 | 比率 |

### 5.2 企業別ミクロデータ

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `cFirmsOutput` | 生産量 | 数量 |
| `cFirmsDesiredOutput` | 希望生産量 | 数量 |
| `cFirmsCapacity` | 生産能力 | 数量 |
| `cFirmsCapacityUtilization` | 稼働率 | 比率 |
| `cFirmsDesiredGrowth` | 希望成長率 | 比率 |
| `cFirmsEmployees` | 従業員数 | 人数 |
| `cFirmsPrices` | 製品価格 | 価格 |
| `cFirmsRealSales` | 実質売上 | 数量 |
| `cFirmsNominalSales` | 名目売上 | 金額 |
| `cFirmsRealInventories` | 実質在庫 | 数量 |
| `cFirmsRealInvestment` | 実質投資 | 数量 |
| `cFirmsRealDesiredInvestment` | 希望実質投資 | 数量 |
| `cFirmsNominalInvestment` | 名目投資 | 金額 |
| `cFirmsLoans` | 借入金残高 | 金額 |
| `cFirmsCreditObtained` | 新規借入額 | 金額 |
| `cFirmsConstrainedCredit` | 信用制約フラグ (1=制約あり) | フラグ |
| `cFirmsLeverage` | レバレッジ比率 | 比率 |
| `cFirmsProfits` | 利益 | 金額 |
| `cFirmsOCF` | 営業キャッシュフロー | 金額 |
| `cFirmsDividends` | 配当金 | 金額 |
| `cFirmsWageBill` | 賃金支払総額 | 金額 |
| `cFirmsExpAverageVariableCosts` | 期待平均可変費用 | 金額 |
| `cFirmsInterestsReceived` | 受取利子 | 金額 |
| `cFirmsInterestsDue` | 支払利子 | 金額 |
| `cFirmsDebtService` | 債務返済額 | 金額 |
| `cFirmsTaxes` | 納税額 | 金額 |
| `cFirmsBailoutCost` | ベイルアウト費用 | 金額 |

---

## 6. 資本財企業セクター (kFirms)

### 6.1 集計指標

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `kFirmsAggregateDebt` | 総負債 | 金額 |
| `kFirmsBankrupcty` | 倒産件数 | 件数 |
| `kAggConsCredit` | 信用制約された企業の割合 | 比率 |

### 6.2 企業別ミクロデータ

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `kFirmsOutput` | 生産量 | 数量 |
| `kFirmsDesiredOutput` | 希望生産量 | 数量 |
| `kFirmsEmployees` | 従業員数 | 人数 |
| `kFirmsPrices` | 製品価格 | 価格 |
| `kFirmsRealSales` | 実質売上 | 数量 |
| `kFirmsNominalSales` | 名目売上 | 金額 |
| `kFirmsRealInventories` | 実質在庫 | 数量 |
| `kFirmsLoans` | 借入金残高 | 金額 |
| `kCreditObtained` | 新規借入額 | 金額 |
| `kFirmsConstrainedCredit` | 信用制約フラグ (1=制約あり) | フラグ |
| `kFirmsLeverage` | レバレッジ比率 | 比率 |
| `kFirmsProfits` | 利益 | 金額 |
| `kFirmsOCF` | 営業キャッシュフロー | 金額 |
| `kFirmsDividends` | 配当金 | 金額 |
| `kFirmsWageBill` | 賃金支払総額 | 金額 |
| `kFirmsExpAverageVariableCosts` | 期待平均可変費用 | 金額 |
| `kFirmsInterestsReceived` | 受取利子 | 金額 |
| `kFirmsInterestsDue` | 支払利子 | 金額 |
| `kFirmsDebtService` | 債務返済額 | 金額 |
| `kFirmsTaxes` | 納税額 | 金額 |
| `kFirmsBailoutCost` | ベイルアウト費用 | 金額 |

---

## 7. 銀行セクター (banks)

### 7.1 集計指標

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `banksBankrupcty` | 銀行倒産件数 | 件数 |
| `banksTotalCredit` | 総貸出残高 | 金額 |
| `banksCreditDegreeDistribution` | 貸出先数の分布 | 件数 |
| `banksLoanAvInterest` | 貸出平均金利 | 比率 |
| `banksDepAvInterest` | 預金平均金利 | 比率 |

### 7.2 銀行別ミクロデータ

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `banksProfits` | 利益 | 金額 |
| `banksDividends` | 配当金 | 金額 |
| `banksTaxes` | 納税額 | 金額 |
| `banksDeposits` | 預金残高 | 金額 |
| `banksLoanInterestRate` | 貸出金利 | 比率 |
| `banksDepositInterestRate` | 預金金利 | 比率 |
| `banksCapitalRatio` | 自己資本比率 | 比率 |
| `banksLiquidityRatio` | 流動性比率 | 比率 |
| `banksMaxExposure` | 最大エクスポージャー | 金額 |
| `banksLossBadDebt` | 不良債権損失 | 金額 |
| `banksBailoutCost` | ベイルアウト費用 | 金額 |

---

## 8. 政府セクター (government)

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `governmentWageBill` | 政府賃金支払総額 | 金額 |
| `governmentDoleExpenditure` | 失業手当支出 | 金額 |
| `governmentCBProfits` | 中央銀行からの受取利益 | 金額 |
| `governmentInterestsBonds` | 国債利払い | 金額 |

---

## 9. バランスシート関連

### 9.1 セクター別集計バランスシート (agg*BS)

各ファイルは `[round, 資産項目1, 資産項目2, ..., 負債項目1, ...]` の形式です。

| ファイル名 | 説明 |
|-----------|------|
| `aggHHBS` | 家計部門集計バランスシート |
| `aggCFBS` | 消費財企業部門集計バランスシート |
| `aggKFBS` | 資本財企業部門集計バランスシート |
| `aggBBS` | 銀行部門集計バランスシート |
| `aggGBS` | 政府部門集計バランスシート |
| `aggCBBS` | 中央銀行部門集計バランスシート |

### 9.2 フロー・ネットワーク

| ファイル名 | 説明 |
|-----------|------|
| `FlowNet` | エージェント間の資金フロー・ネットワーク |
| `BSNet` | エージェント間のバランスシート・ネットワーク |

### 9.3 Transaction Flow Matrix (TFM)

`TFM` ファイルは経済全体の取引フロー行列を記録します。

**列の構成（順序）:**
1. `consHH` - 家計消費支出
2. `wHH` - 家計賃金収入
3. `doleHH` - 家計失業手当収入
4. `tHH` - 家計納税額
5. `iDHH` - 家計預金利子収入
6. `divHH` - 家計配当収入
7. `consCF` - 消費財企業売上
8. `wCF` - 消費財企業賃金支払
9. `tCF` - 消費財企業納税額
10. `invCF` - 消費財企業投資支出
11. `caCF` - 消費財企業資本減価償却
12. `iDCF` - 消費財企業預金利子収入
13. `iLCF` - 消費財企業借入利子支払
14. `divCF` - 消費財企業配当支払
15. `reCF` - 消費財企業内部留保
16. `dInventCF` - 消費財企業在庫変動
17. `wKF` - 資本財企業賃金支払
18. `tKF` - 資本財企業納税額
19. `invKF` - 資本財企業売上（=消費財企業投資）
20. `iDKF` - 資本財企業預金利子収入
21. `iLKF` - 資本財企業借入利子支払
22. `divKF` - 資本財企業配当支払
23. `reKF` - 資本財企業内部留保
24. `dInventKF` - 資本財企業在庫変動
25. `tB` - 銀行納税額
26. `iDB` - 銀行預金利子支払
27. `iBB` - 銀行国債利子収入
28. `iAB` - 銀行中央銀行借入利子支払
29. `iLB` - 銀行貸出利子収入
30. `divB` - 銀行配当支払
31. `reB` - 銀行内部留保
32. `wG` - 政府賃金支払
33. `doleG` - 政府失業手当支払
34. `tG` - 政府税収
35. `iBG` - 政府国債利払
36. `fCBG` - 政府への中央銀行利益移転
37. `iBCB` - 中央銀行国債利子収入
38. `iACB` - 中央銀行貸出利子収入
39. `fCB` - 中央銀行利益

---

## 10. その他

| ファイル名 | 説明 | 単位 |
|-----------|------|------|
| `cFirmsRSalesExpError` | 消費財企業売上予測誤差 | 比率 |
| `kFirmsRSalesExpError` | 資本財企業売上予測誤差 | 比率 |
| `hhPricesExpError` | 家計価格予測誤差 | 比率 |

---

## 補足: レポート定義ファイル

CSV出力の詳細な定義は以下のファイルで確認できます：
- `Model/reports.xml` - レポート定義
- `benchmark/report/*.java` - カスタムレポートの実装
- `jmab/report/*.java` - 基本レポートの実装
