# Database và API Reference

## 1. Database configuration

- Database: PostgreSQL
- Schema migration: Flyway
- Hibernate setting: `ddl-auto=validate`
- Tiền tệ: `CHAR(3)`
- Số tiền: `NUMERIC(19,4)`
- Thời gian: `TIMESTAMP`
- Ledger được bảo vệ append-only bằng PostgreSQL trigger, không cho UPDATE/DELETE.

## 2. Entity relationship

```text
users
  ├── cards
  │     └── wallets       (hiện tại mỗi card có tối đa một wallet)
  ├── payment_events
  ├── top_up_orders
  ├── refresh_tokens
  └── otp_challenges

payment_events
  └── payment_orders
        └── ledger_entries

top_up_orders
  └── ledger_entries
```

## 3. Tables

### `users`

| Column | Type | Mô tả |
|---|---|---|
| `user_id` | `VARCHAR(36)` PK | ID user |
| `full_name` | `VARCHAR(255)` | Họ tên |
| `email` | `VARCHAR(255)` UNIQUE | Email đăng nhập |
| `password_hash` | `VARCHAR(255)` | BCrypt password hash |
| `phone` | `VARCHAR(20)` | Số điện thoại |
| `national_id` | `VARCHAR(255)` | Giấy tờ định danh |
| `address` | `VARCHAR(500)` | Địa chỉ |
| `status` | `VARCHAR(20)` | `ACTIVE`, `BLOCKED` |
| `created_at` | `TIMESTAMP` | Thời điểm tạo |

### `cards`

| Column | Type | Mô tả |
|---|---|---|
| `card_id` | `VARCHAR(36)` PK | ID card |
| `user_id` | `VARCHAR(36)` FK | Chủ sở hữu card |
| `issuer` | `VARCHAR(100)` | Đơn vị phát hành |
| `card_token` | `VARCHAR(255)` UNIQUE | Token card nội bộ |
| `last4` | `CHAR(4)` | Bốn số cuối |
| `card_type` | `VARCHAR(10)` | Loại card |
| `issue_date` | `DATE` | Ngày phát hành |
| `expiry_date` | `DATE` | Ngày hết hạn |
| `status` | `VARCHAR(20)` | `ACTIVE`, `BLOCKED` |

`card_token` hiện chỉ là dữ liệu nội bộ; project chưa kết nối card thật hoặc ngân hàng.

### `wallets`

| Column | Type | Mô tả |
|---|---|---|
| `wallet_id` | `VARCHAR(36)` PK | ID wallet |
| `card_id` | `VARCHAR(36)` UNIQUE FK | Card liên kết |
| `balance` | `NUMERIC(19,4)` | Số dư, không âm |
| `currency` | `CHAR(3)` | Currency của wallet |
| `version` | `BIGINT` | JPA optimistic version |

Wallet được lock bằng `PESSIMISTIC_WRITE` trong transfer và top-up.

### `payment_events`

| Column | Type | Mô tả |
|---|---|---|
| `checkout_id` | `VARCHAR(64)` PK | ID checkout |
| `user_id` | `VARCHAR(36)` FK | User tạo checkout |
| `is_payment_done` | `BOOLEAN` | Checkout đã hoàn thành chưa |
| `created_at` | `TIMESTAMP` | Thời điểm tạo |

### `payment_orders`

| Column | Type | Mô tả |
|---|---|---|
| `payment_order_id` | `VARCHAR(64)` PK | Idempotency/order ID hiện tại |
| `checkout_id` | `VARCHAR(64)` FK | Checkout liên quan |
| `source_card_id` | `VARCHAR(36)` FK | Card nguồn |
| `destination_card_id` | `VARCHAR(36)` FK | Card đích |
| `amount` | `NUMERIC(19,4)` | Số tiền transfer |
| `currency` | `CHAR(3)` | Currency |
| `payment_order_status` | `VARCHAR(20)` | `NOT_STARTED`, `EXECUTING`, `SUCCESS`, `FAILED` |
| `failure_reason` | `VARCHAR(255)` | Lý do thất bại |
| `wallet_updated` | `BOOLEAN` | Đã cập nhật wallet chưa |
| `ledger_updated` | `BOOLEAN` | Đã ghi ledger chưa |
| `created_at` | `TIMESTAMP` | Thời điểm tạo |

Database có constraint không cho source card và destination card giống nhau.

### `top_up_orders`

| Column | Type | Mô tả |
|---|---|---|
| `top_up_order_id` | `VARCHAR(64)` PK | ID top-up |
| `user_id` | `VARCHAR(36)` FK | User tạo top-up |
| `card_id` | `VARCHAR(36)` FK | Card nguồn nội bộ |
| `wallet_id` | `VARCHAR(36)` FK | Wallet nhận tiền |
| `amount` | `NUMERIC(19,4)` | Số tiền |
| `currency` | `CHAR(3)` | Currency |
| `status` | `VARCHAR(20)` | `EXECUTING`, `SUCCESS`, `FAILED` |
| `failure_reason` | `VARCHAR(255)` | Lý do thất bại |
| `wallet_updated` | `BOOLEAN` | Đã cộng wallet chưa |
| `ledger_updated` | `BOOLEAN` | Đã ghi ledger chưa |
| `created_at` | `TIMESTAMP` | Thời điểm tạo |

Đây là bảng của internal/demo top-up. Chưa có provider transaction ID hoặc callback từ PSP.

### `ledger_entries`

| Column | Type | Mô tả |
|---|---|---|
| `entry_id` | `BIGSERIAL` PK | ID ledger entry |
| `payment_order_id` | `VARCHAR(64)` FK nullable | Payment order liên quan |
| `top_up_order_id` | `VARCHAR(64)` FK nullable | Top-up order liên quan |
| `card_id` | `VARCHAR(36)` FK | Card/wallet representation hiện tại |
| `entry_type` | `VARCHAR(6)` | `DEBIT` hoặc `CREDIT` |
| `amount` | `NUMERIC(19,4)` | Số tiền |
| `currency` | `CHAR(3)` | Currency |
| `created_at` | `TIMESTAMP` | Thời điểm ghi |

Constraint yêu cầu mỗi ledger entry thuộc đúng một loại order: payment hoặc top-up.

Transfer thành công tạo hai entry:

```text
DEBIT  source card
CREDIT destination card
```

### `refresh_tokens`

| Column | Type | Mô tả |
|---|---|---|
| `token_id` | `VARCHAR(36)` PK | ID token |
| `user_id` | `VARCHAR(36)` FK | User sở hữu |
| `token_hash` | `VARCHAR(64)` UNIQUE | SHA-256 hash của refresh token |
| `expires_at` | `TIMESTAMP` | Thời điểm hết hạn |
| `revoked_at` | `TIMESTAMP` nullable | Thời điểm revoke |
| `created_at` | `TIMESTAMP` | Thời điểm tạo |

### `otp_challenges`

| Column | Type | Mô tả |
|---|---|---|
| `otp_id` | `UUID` PK | ID OTP |
| `user_id` | `VARCHAR(36)` FK | User yêu cầu OTP |
| `purpose` | `VARCHAR(20)` | `TOP_UP`, `TRANSFER`, `LOGIN` |
| `reference_id` | `VARCHAR(64)` | ID giao dịch/user liên quan |
| `channel` | `VARCHAR(10)` | `EMAIL` hoặc `SMS` |
| `recipient_hint` | `VARCHAR(255)` | Email/phone đã mask |
| `otp_hash` | `VARCHAR(128)` | HMAC hash |
| `expires_at` | `TIMESTAMP` | Thời điểm hết hạn |
| `attempt_count` | `INT` | Số lần thử |
| `max_attempts` | `INT` | Giới hạn lần thử |
| `used_at` | `TIMESTAMP` nullable | Thời điểm verify thành công |
| `created_at` | `TIMESTAMP` | Thời điểm tạo |

## 4. API reference

### Public API

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/v1/auth/register` | Đăng ký user |
| `POST` | `/v1/auth/login` | Đăng nhập và cấp token |
| `POST` | `/v1/auth/refresh` | Rotate refresh token |
| `POST` | `/v1/auth/logout` | Revoke refresh token |
| `GET` | `/actuator/health` | Health check |

### Protected API

Tất cả endpoint dưới đây yêu cầu:

```http
Authorization: Bearer <access-token>
```

#### User

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/v1/users/me` | Lấy profile |
| `PUT` | `/v1/users/me` | Cập nhật profile |
| `DELETE` | `/v1/users/me` | Block user hiện tại |

#### OTP

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/v1/otp/challenges` | Tạo và gửi OTP email |
| `POST` | `/v1/otp/challenges/{otpId}/verify` | Verify OTP |

#### Transfer

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/v1/payments` | Thực hiện transfer |
| `GET` | `/v1/payments/{id}` | Lấy payment order |

#### Ledger

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/v1/ledger/orders/{id}` | Lấy ledger theo payment order |
| `GET` | `/v1/ledger/cards/{cardId}/net` | Tính net ledger theo card |

#### Top-up demo

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/v1/topups` | Tạo internal top-up |
| `GET` | `/v1/topups/{id}` | Lấy top-up order |

## 5. Trạng thái và giới hạn hiện tại

- Chưa có withdrawal.
- Chưa có payment provider/bank integration.
- Chưa có provider callback và reconciliation.
- Chưa có API list card/wallet và lịch sử có pagination.
- Các API đọc payment/top-up/ledger cần được bổ sung ownership filtering trước production.
- Idempotency hiện ở mức cơ bản theo order ID; cần bổ sung request fingerprint và conflict handling.

