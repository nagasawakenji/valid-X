# valid_x Secure Authentication System with Magic Link & JWT

valid-X は、**Spring Boot + MyBatis + PostgreSQL**を基礎に構築された、一般的なSNSアプリケーションの機能を備えたアプリケーションです。  

マジックリンクによるワンクリック認証、JWTなどの基本的な認証システムを使用しており、バックエンドエンジニアリングで用いる技術的要素を大体網羅している気がします。  

システムの挙動は別に用意された簡易フロントエンド開発ツールから確認できます。  
(フロントエンド開発ツール URL:https://github.com/nagasawakenji/valid-X-front-dev-tool)

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
  CI/CDを効率的に行うためのテストコードがあります(結合テストは随時追加していきます)。これで滞りなく安全性を保証できます。

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

以下の例を参考にして、`.env`ファイルをvalid-X直下に作成してください。  
`.env` の例：
```bash
# --- DB ---
SPRING_DATASOURCE_URL=jdbc:postgresql://postgre:5432/validx
SPRING_DATASOURCE_USERNAME=yourname
SPRING_DATASOURCE_PASSWORD=yourpassword

# --- JWT / HMAC ---
APP_JWT_SECRET=CHANGE_ME
APP_HMAC_K1=CHNGE_ME

# --- Storage ---
APP_STORAGE_PATH=/Users/you/Downloads/valid-x/storage
APP_PUBLIC_URL=https://localhost:8443/media/

# --- Mail ---
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your-username
MAIL_PASSWORD=your-password
```
(ローカル環境で起動する場合は、上の`SPRING_DATASOURCE_URL`を
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/validx
```
などに変更してください)
> ⚠️ 本番環境では `.env` を絶対に公開しないでください。

---

### 🐳 ビルドと実行(Docker)
ローカルの環境にDockerアプリをインストールしてください
```bash
docker compose up -d
```
でビルドができます。  
(初回起動時は5分くらい時間がかかります)

### 🏗️ ビルドと実行(ローカル環境)

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
# 1. 環境変数を読み込む (.env がプロジェクト直下にあることを確認)
export $(grep -v '^#' .env | xargs)

# 2. HTTPS プロファイルでアプリを起動
mvn spring-boot:run -Dspring-boot.run.profiles=https -Dspring-boot.run.fork=false
```

起動後、`https://localhost:8443` にアクセスします。(フロント画面からapiを実行する際はこれをしないと認証が通らないので注意してください)

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
### フロントエンドの設定は以下のurl  
### (https://github.com/nagasawakenji/valid-X-front-dev-tool)  
### に記載してあります。

### ユーザー登録方法
#### 1. 認証メールリクエスト
```bash
curl -v -sk -X POST https://localhost:8443/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user_for_readme",
    "display_name": "テストユーザー(レジュメ用)",
    "email": "testforreadme@example.com",
    "password": "testforreadme123",
    "locale": "ja_JP",
    "timezone": "Asia/Tokyo"
  }'
```
このcurlコマンドを実行することにより認証メールが届きます。そのメールに記載されているリンクにアクセスすることで、ユーザー登録が完了します。  
また、登録に失敗した際は、15分後にもう一度認証プロセスを実行してください。

#### 2. マジックリンク認証
1が成功すると、自動的に同じメールアドレスにマジックリンク認証のメールが届きます。  
リンクにアクセスすることで、ACCESS_TOKENが付与され、下記のapiが実行できるようになります。
___
### ログイン方法
#### 1. マジックリンク認証リクエスト
```bash
curl -sk -c cookies.txt -b cookies.txt https://localhost:8443/v1/auth/magic-link/request \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'
```
MailHogを使用している場合は、  
`http://localhost:8025` にアクセスしてメールを確認できます。
メールを設定していない場合は、サーバー側のログに一時的にurltokenが表示されるようになっているので、コピペして使ってください。  
(本番環境ではログを**絶対に**削除してください)

#### 2. 認証リンクをクリック（もしくは cURL でアクセス）
```bash
curl -sk -c cookies.txt -b cookies.txt https://localhost:8443/v1/auth/magic-link/consume \
  -H "Content-Type: application/json" \
  -d '{"token":"<入手したurlトークンを貼り付けてください>"}'
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
その後は
```bash
ACCESS_TOKEN=<入手したアクセストークン>
```
としてください。以降の認証が必要なapiが実行できるようになります。

---

- /v1/auth/post  
これはポストを投稿するapiです。
```bash
curl -v -sk -X POST https://localhost:8443/v1/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt \
  -d '{
        "content": "初めての投稿です！",
        "inReplyToTweet": null,
        "medias": []
      }'
```
---

- /v1/tweets/{tweetId}/replies
このapiではすでに存在するポストに対してリプライを実行します。
```bash
curl -v -sk -X POST https://localhost:8443/v1/tweets/25/reply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt \
  -d '{
        "content": "その通りですね！",
        "inReplyToTweet": null,
        "medias": []
      }'
```

---

- /v1/tweets/{tweetId}/repost  
リポスト機能を実装しています。
```bash
curl -v -sk -X PUT https://localhost:8443/v1/tweets/25/repost \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt
```
PUTを指定することでリポスト、
```bash
curl -v -sk -X DELETE https://localhost:8443/v1/tweets/25/repost \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt
```
DELETEを指定することでリポスト解除を実行します。

---

- /v1/tweets/{tweetId}/like  
いいねが実行できます。
```bash
curl -v -sk -X PUT https://localhost:8443/v1/tweets/25/like \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt
```

___

- /v1/timeline  
フォローユーザーのポストが新着順で表示されます。
```bash
 curl -v -sk -G https://localhost:8443/v1/timeline \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt \
  --data-urlencode "limit=30" \
  --data-urlencode "cursor=100"
```

- /v1/tweets/{tweetId}/replies
tweetIdで指定したポストへの返信を表示します
```bash
curl -v -sk -G https://localhost:8443/v1/tweets/25/replies \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt \
  --data-urlencode "limit=30" \
  --data-urlencode "cursor=200"
```

- /v1/users/{userId}/tweets
userIdで指定したユーザーのポストを新着順で表示します
```bash
curl -v -sk -G https://localhost:8443/v1/users/1/tweets \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt \
  --data-urlencode "limit=30" \
  --data-urlencode "cursor=150"
```
- /v1/tweets/popular
人気のポスト(cursor_likeで指定したいいね数以上)を新着順で取得します
```bash
 curl -v -sk -G https://localhost:8443/v1/tweets/popular \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt \
  --data-urlencode "limit=30" \
  --data-urlencode "cursor_like=500" \
  --data-urlencode "cursor_id=1000" \
  --data-urlencode "day_count=15"
```

---

- /v1/tweets/{tweetId}/delete
tweetIdで指定したポストを削除します(自分のポストでないと削除できません)
```bash
curl -v -sk -X DELETE https://localhost:8443/v1/tweets/25/delete \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt 
```

---

- /v1/users/{userId}/follow
PUT指定でuserIdで指定したユーザーをフォローします
```bash
 curl -v -sk -X PUT https://localhost:8443/v1/users/42/follow \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt
```

DELETE指定の場合はuserIdで指定したユーザーのフォローを解除します
```bash
curl -v -sk -X DELETE https://localhost:8443/v1/users/42/follow \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt
```

- /v1/users/{userId}/followers
userIdで指定したユーザーのフォロワーを一覧表示します。  
また、limitを指定することで、一度に表示するユーザー数の上限を指定できます。
```bash
 curl -v -sk -G https://localhost:8443/v1/users/42/followers \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt \
  --data-urlencode "limit=30" \
  --data-urlencode "cursor=100"
```

- /v1/users/{userId}/following
userIdで指定したユーザーのフォローユーザーを一覧表示します。
limitの使用は/followersと同じです。
```bash
 curl -v -sk -G https://localhost:8443/v1/users/42/following \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-XSRF-TOKEN: $XSRF_MASKED" \
  -b cookies.txt -c cookies.txt \
  --data-urlencode "limit=30" \
  --data-urlencode "cursor=100"
```








### 🧰 今後の予定
- 🧪 テストの充実
- 🏃‍♀️主要apiのドキュメント充実
- 📺 フロントエンド機能の拡張

---

### 👤 開発者
**長澤健二 (Kenji Nagasawa)**  


