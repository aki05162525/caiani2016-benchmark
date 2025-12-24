# クレジット供給確率計算の無駄削減 進捗メモ

## 完了日
2025-12-24

## 概要
`ExpectedReturnCreditSupply` でデフォルト確率が要求額に依存しない実装 (`DeterministicLogisticLeverageDefaultProbabilityComputer`) の場合でも、101ステップの探索ごとに確率計算が繰り返され、`getNumericBalanceSheet` が毎回呼ばれていた。依存しないケースでは確率を1回だけ計算して使い回すようにし、重複計算を除去。

## 完了済み
- 確率が要求額に依存しない場合はループ外で一度だけ算出し、101ステップの全探索で再利用するフラグを追加

## 次の確認ポイント
- 他の `DefaultProbilityComputer` 実装で要求額依存がある場合にも挙動が変わらないこと（依存しないケースのみキャッシュする分岐になっている）
- ネットワーク制限で Gradle ビルド未実行のため、許可後に `:jmab:compileJava` を走らせてコンパイル確認する
