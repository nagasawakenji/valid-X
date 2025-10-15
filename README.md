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


