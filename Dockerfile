# 1. 実行環境として Java 21 を使用
FROM eclipse-temurin:21-jdk-alpine

# 2. コンテナ内の作業ディレクトリを設定
WORKDIR /app

# 3. プロジェクトファイルをコンテナにコピー
COPY . .

# 4. mvnw に実行権限を付与し、JARファイルをビルド
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# 5. 実行するポート番号（Renderのデフォルトは10000）
EXPOSE 10000

# 6. アプリケーションを実行
# ※targetディレクトリ内のJARファイル名はご自身の環境に合わせて確認してください
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar", "--server.port=10000"]