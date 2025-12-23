# GDPマイナス対策 進捗メモ

## 実施内容
- CapitalFirm/ConsumptionFirm の `produce()` で `outputQty > 0` の場合のみ `unitCost` を更新するようにガードを追加
- 実装計画の Phase 1 タスクを完了として更新

## 目的
- `outputQty` が 0 近傍のときに `unitCost` が発散し、在庫評価が過大になる経路を抑制する

## 未対応
- GDP推移の再検証（nominalGDPの初期数期の確認）

