# SelectionRandomRobinBuyerMixer 最適化 進捗メモ

## 完了日
2025-12-24

## 概要
Selection 版の RandomRobin mixer が buyer ごとに seller をシャッフルしていたため、buyer×seller の二重ループで大きなオーバーヘッドが発生していた。一度だけ seller をシャッフルし、buyer ごとに開始オフセットをずらして循環参照する方式に変更し、`nbSellers` だけ先頭から選択するようにした。

## 完了済み
- seller のシャッフルをラウンド1回に集約
- アクティブ seller のみ抽出し、buyer ごとに開始位置をずらして `nbSellers` 件を選択するローテーション方式に変更

## 次の確認ポイント
- ランダム性・フェアネス（buyer が異なっても同じ seller に偏らないか）を runFull などの実行で確認
- ネットワーク制約で Gradle ビルド未実行。許可後に `bash gradlew :jmab:compileJava -x test` でコンパイル確認する
