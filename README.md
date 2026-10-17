# Payment System

Backend mô phỏng hệ thống thanh toán và chuyển tiền giữa các wallet, xây dựng bằng Spring Boot.

Project tập trung vào các vấn đề quan trọng của backend thanh toán: xác thực người dùng, OTP, transaction database, locking khi cập nhật số dư, idempotency cơ bản và append-only ledger.

## Công nghệ

- Java 25
- Spring Boot
- Spring Web MVC
- Spring Data JPA/Hibernate
- Spring Security
- JWT access token và refresh token
- PostgreSQL
- Flyway
- SMTP email cho OTP
- Docker Compose

## Phạm vi chức năng

- Đăng ký, đăng nhập, refresh token và logout.
- Quản lý thông tin user hiện tại.
- Xác thực OTP qua email cho giao dịch.
- Chuyển tiền giữa hai wallet gắn với card.
- Pessimistic row-level locking khi khóa wallet.
- Lock hai wallet theo thứ tự cố định để giảm nguy cơ deadlock.
- Cập nhật debit/credit trong cùng một database transaction.
- Ghi ledger gồm `DEBIT` và `CREDIT`.
- Top-up nội bộ để demo luồng wallet, OTP và ledger.

Top-up hiện chưa kết nối ngân hàng, card thật hoặc payment provider. Hệ thống chỉ cộng tiền trực tiếp vào wallet sau khi kiểm tra user, card và OTP hợp lệ.

## Kiến trúc luồng transfer

```text
Register/Login
    -> nhận JWT
    -> tạo OTP challenge qua email
    -> verify OTP
    -> POST /v1/payments
    -> kiểm tra user/card/currency/số dư
    -> lock source wallet và destination wallet
    -> debit source + credit destination
    -> ghi DEBIT + CREDIT ledger
    -> SUCCESS
```

Wallet có thêm JPA `@Version` để hỗ trợ optimistic version checking, nhưng transfer sử dụng pessimistic locking làm cơ chế đồng thời chính.

## Chạy project

### Yêu cầu

- JDK 25
- Docker Desktop
- Maven Wrapper hoặc Maven đã cài đặt

### Khởi động PostgreSQL

```bash
docker compose up -d
```

### Cấu hình môi trường

Tạo file `.env` ở thư mục gốc:

```properties
DB_URL=jdbc:postgresql://localhost:5432/payment_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

JWT_SECRET_KEY=replace-with-a-secret-at-least-32-characters
OTP_SECRET_KEY=replace-with-a-secret-at-least-32-characters

SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@example.com
SMTP_PASSWORD=your-smtp-password
```

Không commit `.env` hoặc secret thật lên GitHub.

### Chạy ứng dụng

```bash
./mvnw spring-boot:run
```

Trên Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Flyway sẽ tự chạy các migration trong `src/main/resources/db/migration`.

## API chính

### Authentication

```text
POST /v1/auth/register
POST /v1/auth/login
POST /v1/auth/refresh
POST /v1/auth/logout
```

Các API bảo vệ yêu cầu:

```http
Authorization: Bearer <access-token>
```

Refresh token được truyền qua header:

```http
X-Refresh-Token: <refresh-token>
```

### User

```text
GET    /v1/users/me
PUT    /v1/users/me
DELETE /v1/users/me
```

`DELETE` là soft delete và chuyển user sang trạng thái `BLOCKED`.

### OTP

```text
POST /v1/otp/challenges
POST /v1/otp/challenges/{otpId}/verify
```

OTP hiện chỉ gửi qua email, có thời hạn 5 phút và tối đa 5 lần thử.

### Transfer

```text
POST /v1/payments
GET  /v1/payments/{id}
```

Request mẫu:

```json
{
  "checkoutId": "checkout-001",
  "paymentOrderId": "payment-001",
  "sourceCardId": "card-source",
  "destinationCardId": "card-destination",
  "amount": "100000",
  "currency": "VND",
  "otpId": "00000000-0000-0000-0000-000000000000"
}
```

### Ledger

```text
GET /v1/ledger/orders/{id}
GET /v1/ledger/cards/{cardId}/net
```

### Top-up demo

```text
POST /v1/topups
GET  /v1/topups/{id}
```

Top-up chỉ là internal/demo flow và không charge tiền thật từ card.

## Database migration

Các migration hiện có:

- `V1__init.sql`: users, cards, wallets, payment events, payment orders và ledger.
- `V2__add_top_up.sql`: top-up orders và liên kết ledger với top-up.
- `V3__add_auth_refresh_and_otp.sql`: password hash, refresh tokens và OTP challenges.

Chi tiết schema và quan hệ nằm trong [DB.md](DB.md).
