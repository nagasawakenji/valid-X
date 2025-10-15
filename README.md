# valid_x Secure Authentication System with Magic Link & JWT

valid-X は、**Spring Boot + MyBatis + PostgreSQL**を基礎に構築された、一般的なSNSアプリケーションの機能を備えたアプリケーションです。  

マジックリンクによるワンクリック認証、JWTなどの基本的な認証システムを使用しており、バックエンドエンジニアリングで用いる技術的要素を大体網羅している気がします。  

システムの挙動は別に用意された簡易フロントエンド開発ツールから確認できます。

___

## 🏠 良いところ
- 👁️ **Magic Link Authentication**  
  メールアドレスに認証リンクを送信し、クリックのみでログインが可能となっています。パスワード入力の手間が省け、楽です。
- 🔒 **JWT Access Token**  
  有効期限付きトークンの発行・更新を実装。  
  `JwtService` でクレーム構造（issuer / subject / sid / sv / roles）を制御しています。
- ♻️ **Refresh with Rotation**  
  有効期限の切れたJWTはリフレッシュ機能を用いて更新できます。
- 🖐️ **CSRF Security**  
  CSRFトークンを用いた認証を実装しています。これでCSRF攻撃も怖くないはずです。
- 🧪 **Unit Test With JUnit5 + Mockito**
  CI/CDを効率的に行うためのテストコードがあります(今後より実装していく予定です)。これで滞りなく安全性を保証できます。

___

## 🧩 Architecture Overview
valid-X/  
├── application/  
│   ├── service/         # ビジネスロジック層  
│   │   ├── SignupService.java  
│   │   ├── VerifyService.java  
│   │   ├── VerificationService.java  
│   │   ├── JwtService.java  
│   │   └── RefreshService.java  
│   └── mapper/          # MapStruct コンバータ  
│  
├── domain/  
│   ├── model/           # ドメインモデル (User, PendingUser, Token, Session …)  
│   └── dto/             # 返却用 DTO  
│  
├── infra/  
│   └── mybatis/mapper/  # Mapper 層 (SQLマッピング)  
│  
└── test/  
└── service/         # 単体テスト (Mockito + JUnit5)  

___

## 🔧 使用技術

| Category | Technology |
|-----------|-------------|
| Language | Java 24 |
| Framework | Spring Boot 3.5 |
| ORM | MyBatis |
| Database | PostgreSQL |
| Auth | JWT, Magic Link, CSRF |
| Build Tool | Maven |
| Test | JUnit5, Mockito |
| CI/CD | GitHub Actions |
| Others | MapStruct, Lombok, Flyway |

---


## 🪫セットアップ方法

### 🧰 前提条件
このプロジェクトを動かすには、以下の環境が必要です。

| Tool | Version | 備考 |
|------|----------|------|
| Java | 17 以上（推奨: 24） | `JAVA_HOME` 設定が必要 |
| Maven | 3.9+ | ビルド・依存関係管理 |
| PostgreSQL | 14 以上 | `validx` データベースを作成しておく |
| MailHog | 任意 | 開発環境でのメール送信確認用 |
| Docker (任意) | コンテナ起動を行う場合に使用 |

---

### ⚙️ 環境変数の設定
セキュリティ情報は `.env` ファイルまたは環境変数で管理します。

`.env.example` をコピーして `.env` を作成し、必要な値を設定してください。

```bash
cp .env.example .env
```

`.env` の例：
```bash
DB_URL=jdbc:postgresql://localhost:5432/validx
DB_USER=validxadmin
DB_PASS=postgre
JWT_SECRET=CHANGE_ME_TO_LONG_RANDOM_SECRET
HMAC_KEY1=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
MAIL_HOST=localhost
MAIL_PORT=1025
```

> ⚠️ 本番環境では `.env` を絶対に公開しないでください。

---

### 🏗️ ビルドと実行

#### 1. データベース初期化
Flyway によりスキーマが自動でマイグレーションされます。

```bash
mvn clean flyway:migrate
```

#### 2. アプリケーション起動
```bash
mvn spring-boot:run
```

デフォルトでは以下のプロファイルが読み込まれます：
- `application.properties`
- `application-dev.properties`
- `application-https.properties`（HTTPS利用時）

HTTPSを有効にして起動する場合：
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=https
```

起動後、`https://localhost:8443` にアクセスします。

---

### 🧪 テスト実行

ユニットテストおよび統合テストは JUnit5 + Mockito により実装されています。

```bash
mvn test
```

CI環境（GitHub Actionsなど）では自動で実行されます。

---

### 📮 動作確認

### (フロントエンド側からapiの挙動を確認することができます)  
### フロントエンドの設定は以下のurl(ここにはる)に記載してあります。

#### 1. マジックリンク認証リクエスト
```bash
curl -X POST http://localhost:8080/v1/auth/magic-link/request \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```
MailHogを使用している場合は、  
`http://localhost:8025` にアクセスしてメールを確認できます。
メールを設定していない場合は、サーバー側のログに一時的にurltokenが表示されるようになっているので、コピペして使ってください。  
(本番環境ではログを**絶対に**削除してください)

#### 2. 認証リンクをクリック（もしくは cURL でアクセス）
```bash
curl -sk -c cookies.txt -b cookies.txt https://localhost:8443/v1/auth/magic-link/consume \
  -H "Content-Type: application/json" \
  -d '{"token":<入手したurlトークンを貼り付けてください>}'
```

#### 3. JWT付きAPIアクセス例
まずは、/v1/csrfにアクセスしてcsrfトークンを取得してください。
```bash
XSRF_MASKED=$(curl -sk -c cookies.txt -b cookies.txt \
 https://localhost:8443/v1/auth/csrf | jq -r '.csrfToken') 
```
このcurlコマンドを実行することで、csrf認証に用いるcsrfトークンがcookieに付与されます。
```bash
curl -v -sk -X POST https://localhost:8443/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -H "Origin: https://localhost:8443" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt \
  -d '{}'
```
curlコマンドを取得したら、ヘッダにcsrfトークン入力してapiを実行してください。

---

### 🏃‍♀️主要apiの概要
- /v1/auth/refresh  
このapiはJwtのttlが切れた時に使用します。ログインした後に使用してください。  
Jwtアクセストークンの更新を行います。curlコマンドでの操作方法は以下です。
```bash
XSRF_MASKED=$(curl -sk -c cookies.txt -b cookies.txt \
 https://localhost:8443/v1/auth/csrf | jq -r '.csrfToken')
```
でcookieにcsrfトークンを付与してください。
```bash
curl -v -sk -X POST https://localhost:8443/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -H "Origin: https://localhost:8443" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt \
  -d '{}'

```
を実行してください。Jwtトークンが付与されます。




### 🧰 今後の予定 
- 🧱 Docker Compose による一括起動  
- 🧪 テストの追加
- 🏃‍♀️主要apiのドキュメント追加
- 📺 フロントエンド機能の拡張

---

### 👤 開発者
**長澤健二 (Kenji Nagasawa)**  


