# Java 17 (軽量 Alpine ベース)
FROM eclipse-temurin:17-jdk-jammy

# 作業ディレクトリ
WORKDIR /app

# Maven Wrapper をコピー
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

# 実行権限を付与して依存関係を先に取得
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# ソースをコピー
COPY src/ src/

# アプリ起動（開発モード）
EXPOSE 8080
CMD ["./mvnw", "spring-boot:run"]