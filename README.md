## 動作環境・セットアップ手順

### 前提条件
* Java Development Kit (JDK) 21 以上
* OpenAI API キー（環境変数への設定が必要）

### 環境変数の設定
セキュリティ保護のため、APIキーは以下の変数名でシステム環境変数に設定してください。

* **環境変数名**: `SPRING_AI_OPENAI_API_KEY`

### 起動方法
プロジェクトのルートディレクトリにて、以下のコマンドを実行してください。

```bash
# Windows環境 (PowerShell)
./mvnw.cmd spring-boot:run

# Mac/Linux環境
./mvnw spring-boot:run
```
