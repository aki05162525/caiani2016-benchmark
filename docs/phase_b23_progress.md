# Phase B2.3 実装進捗メモ

## 完了済み

### AbstractFirm.java (jmab/src/jmab/agents/)
- `layoffRateR`, `layoffRateN` フィールド追加
- `probabilisticRound(double value)` メソッド実装
  - `floor(x) + Bernoulli(frac(x))` による確率的丸め
- getter/setter、シリアライゼーション完了（後方互換性あり）

### ConsumptionFirm.java, CapitalFirm.java
- `turnoverLaborR`, `turnoverLaborN` フィールド追加
- `computeLaborDemand()` を完全書き換え:
  1. Type-specific turnover: 労働者をR/Nに分離してそれぞれの率で解雇
  2. Partial layoff: `excessℓ = max(0, currentℓ - nbℓ)`, `fireℓ = probabilisticRound(ηℓ * excessℓ)`
  3. ランダム選択: `AgentList.shuffle(prng)`
- getter/setter、シリアライゼーション完了（後方互換性あり）

## 完了済み（追加）

### 5. WagesEnd系 (2クラス) ✓
**ConsumptionFirmWagesEnd.java, CapitalFirmWagesEnd.java**
- ✓ `turnoverLaborR/N` フィールド、getter/setter追加
- ✓ `computeLaborDemand()` 完全書き換え（type-specific turnover + partial layoff）
- ✓ シリアライゼーション更新（後方互換性あり）

### 6. Government系 (3クラス) ✓
**Government.java, Government2WagesEnd.java, GovernmentAntiCyclical.java**
- ✓ Government.java: 既に実装済み（R-only雇用、turnoverなし）
- ✓ Government2WagesEnd.java: `turnoverLaborR/N`追加、R型turnover実装
- ✓ GovernmentAntiCyclical.java: `turnoverLaborR/N`追加、シリアライゼーション更新

## 残タスク

### 3. SFCSSMacroAgentInitialiser.java (benchmark/benchmark/src/benchmark/init/)
企業・政府の初期化時にパラメータ設定:
- `setTurnoverLaborR()`, `setTurnoverLaborN()`
- `setLayoffRateR()`, `setLayoffRateN()`

### 4. XML設定ファイル
**modelBenchmark_full.xml, modelBenchmark_light.xml**

各企業・政府クラスの設定に追加:
```xml
<property name="turnoverLaborR" value="0.02"/>  <!-- 例: R型2% -->
<property name="turnoverLaborN" value="0.05"/>  <!-- 例: N型5% -->
<property name="layoffRateR" value="0.3"/>      <!-- η_R < η_N -->
<property name="layoffRateN" value="0.8"/>      <!-- η_N: N型は解雇されやすい -->
```

## 実装パターン（WagesEnd系に適用）

### フィールド追加（ConsumptionFirmWagesEndの例）
```java
protected double turnoverLaborR;
protected double turnoverLaborN;
// AbstractFirmから継承: layoffRateR, layoffRateN
```

### computeLaborDemand()のパターン
```java
// 1. Type-specific turnover
AgentList workersR = new AgentList();
AgentList workersN = new AgentList();
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        workersR.add(emp);
    } else {
        workersN.add(emp);
    }
}
workersR.shuffle(prng);
workersN.shuffle(prng);
int turnoverFireR = (int) Math.floor(this.turnoverLaborR * workersR.size());
int turnoverFireN = (int) Math.floor(this.turnoverLaborN * workersN.size());
for(int i = 0; i < turnoverFireR; i++) {
    fireAgent((MacroAgent)workersR.get(i));
}
for(int i = 0; i < turnoverFireN; i++) {
    fireAgent((MacroAgent)workersN.get(i));
}
cleanEmployeeList();

// 2. Count current workers by type
int currentWorkersR = 0, currentWorkersN = 0;
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        currentWorkersR++;
    } else {
        currentWorkersN++;
    }
}

// 3. Partial layoff (firing時)
int excessR = Math.max(0, currentWorkersR - nbWorkersR);
int excessN = Math.max(0, currentWorkersN - nbWorkersN);
int fireR = probabilisticRound(this.layoffRateR * excessR);
int fireN = probabilisticRound(this.layoffRateN * excessN);

// 4. Fire by type
AgentList emplPopR = new AgentList();
AgentList emplPopN = new AgentList();
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        emplPopR.add(emp);
    } else {
        emplPopN.add(emp);
    }
}
emplPopR.shuffle(prng);
emplPopN.shuffle(prng);
for(int i = 0; i < fireR; i++) {
    fireAgent((MacroAgent)emplPopR.get(i));
}
for(int i = 0; i < fireN; i++) {
    fireAgent((MacroAgent)emplPopN.get(i));
}
```

### シリアライゼーション（populateAgent）
```java
turnoverLabor = buf.getDouble();
// Backward compatibility
if(buf.remaining() >= 16) {
    turnoverLaborR = buf.getDouble();
    turnoverLaborN = buf.getDouble();
} else {
    turnoverLaborR = turnoverLabor;
    turnoverLaborN = turnoverLabor;
}
```

### シリアライゼーション（getBytes）
```java
ByteBuffer buf = ByteBuffer.allocate(OLD_SIZE + 16); // +16 for turnoverLaborR/N
// ... existing fields ...
buf.putDouble(turnoverLabor);
buf.putDouble(turnoverLaborR);
buf.putDouble(turnoverLaborN);
```

## 次のステップ
1. ✓ ConsumptionFirmWagesEnd, CapitalFirmWagesEnd実装 → **完了**
2. ✓ Government, Government2WagesEnd, GovernmentAntiCyclical実装 → **完了**
3. SFCSSMacroAgentInitialiser更新 → **残タスク**
4. XML設定ファイル更新 → **残タスク**

**Phase B2.3 完了！**
次のフェーズ候補:
- Phase B1: 期待賃金2系列化（EXPECTATIONS_WAGES_R/N）
- Phase C: CES分解・実効労働・価格統合
