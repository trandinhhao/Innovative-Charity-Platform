# Hướng dẫn Truy cập MySQL Database

## 📋 Thông tin Database

- **Database Name**: `charity_platform`
- **Host**: `localhost`
- **Port**: `3307` (mapped từ container port 3306)
- **Username**: Từ file `.env` (biến `DBMS_USERNAME`) hoặc mặc định `root`
- **Password**: Từ file `.env` (biến `DBMS_PASSWORD`) hoặc mặc định `root`

---

## 🔧 Cách 1: Kết nối qua MySQL Command Line (CLI)

### Nếu MySQL đang chạy trong Docker:

```bash
# Kết nối vào container MySQL
docker exec -it charity-mysql mysql -u root -p

# Hoặc nếu biết password (từ .env)
docker exec -it charity-mysql mysql -u root -p${DBMS_PASSWORD}

# Sau khi vào, chọn database
USE charity_platform;

# Xem danh sách tables
SHOW TABLES;

# Xem dữ liệu trong một table
SELECT * FROM users LIMIT 10;
```

### Nếu MySQL cài đặt local:

```bash
# Kết nối trực tiếp
mysql -h localhost -P 3307 -u root -p

# Hoặc nếu dùng port mặc định 3306
mysql -h localhost -P 3306 -u root -p
```

---

## 🖥️ Cách 2: Dùng MySQL Workbench (GUI)

1. **Download MySQL Workbench**: https://dev.mysql.com/downloads/workbench/

2. **Tạo Connection mới**:
   - Click `+` để tạo connection mới
   - Đặt tên: `Charity Platform`
   - **Hostname**: `localhost`
   - **Port**: `3307`
   - **Username**: `root` (hoặc từ `.env` file)
   - **Password**: Click "Store in Vault" và nhập password
   - Click "Test Connection" để kiểm tra
   - Click "OK" để lưu

3. **Kết nối**:
   - Double-click vào connection vừa tạo
   - Chọn database `charity_platform` từ dropdown

4. **Xem dữ liệu**:
   - Trong panel bên trái, expand `charity_platform`
   - Click vào `Tables` để xem danh sách tables
   - Right-click vào table → `Select Rows - Limit 1000` để xem dữ liệu

---

## 💻 Cách 3: Dùng DBeaver (Free, Cross-platform)

1. **Download DBeaver**: https://dbeaver.io/download/

2. **Tạo Connection**:
   - Click "New Database Connection" (icon ổ cắm)
   - Chọn "MySQL"
   - **Host**: `localhost`
   - **Port**: `3307`
   - **Database**: `charity_platform`
   - **Username**: `root`
   - **Password**: Nhập password
   - Click "Test Connection" → Download driver nếu cần
   - Click "Finish"

3. **Sử dụng**:
   - Expand connection → `charity_platform` → `Tables`
   - Right-click table → `View Data` để xem dữ liệu

---

## 🐳 Cách 4: Dùng Docker Exec với MySQL Client

```bash
# Vào MySQL shell trong container
docker exec -it charity-mysql mysql -u root -p charity_platform

# Hoặc với password trực tiếp (nếu có trong .env)
docker exec -it charity-mysql mysql -u root -p${DBMS_PASSWORD} charity_platform
```

**Các lệnh MySQL hữu ích**:

```sql
-- Xem danh sách databases
SHOW DATABASES;

-- Chọn database
USE charity_platform;

-- Xem danh sách tables
SHOW TABLES;

-- Xem cấu trúc table
DESCRIBE users;
DESCRIBE challenges;
DESCRIBE skill_auctions;
DESCRIBE bids;
DESCRIBE transactions;

-- Xem dữ liệu (giới hạn 10 rows)
SELECT * FROM users LIMIT 10;
SELECT * FROM challenges LIMIT 10;
SELECT * FROM skill_auctions LIMIT 10;
SELECT * FROM bids LIMIT 10;
SELECT * FROM user_challenges LIMIT 10;

-- Đếm số records
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM challenges;
SELECT COUNT(*) FROM skill_auctions;

-- Xem các auctions đang ACTIVE
SELECT * FROM skill_auctions WHERE status = 'ACTIVE';

-- Xem các verification đang PROCESSING
SELECT * FROM user_challenges WHERE verification_status = 'PROCESSING';
```

---

## 📊 Cách 5: Dùng VS Code Extension

1. **Cài extension**: "MySQL" hoặc "Database Client" trong VS Code

2. **Tạo connection**:
   - Mở Command Palette (`Ctrl+Shift+P`)
   - Chọn "MySQL: Add Connection"
   - Nhập thông tin:
     - Host: `localhost`
     - Port: `3307`
     - User: `root`
     - Password: (từ .env)
     - Database: `charity_platform`

3. **Sử dụng**:
   - Mở MySQL panel
   - Expand connection → `charity_platform` → `Tables`
   - Click vào table để xem dữ liệu

---

## 🔍 Kiểm tra Database đang chạy

### Kiểm tra container MySQL:

```bash
# Xem container đang chạy
docker ps | grep mysql

# Xem logs
docker logs charity-mysql

# Kiểm tra health
docker inspect charity-mysql | grep -A 10 Health
```

### Kiểm tra kết nối từ host:

```bash
# Test connection (Windows PowerShell)
Test-NetConnection -ComputerName localhost -Port 3307

# Hoặc dùng telnet (nếu có)
telnet localhost 3307
```

---

## 🛠️ Troubleshooting

### Lỗi: "Can't connect to MySQL server"

**Giải pháp**:
```bash
# Kiểm tra container có đang chạy không
docker ps

# Nếu không chạy, start lại
docker-compose up -d mysql

# Kiểm tra logs
docker logs charity-mysql
```

### Lỗi: "Access denied for user"

**Giải pháp**:
- Kiểm tra username/password trong file `.env`
- Hoặc reset password:
```bash
docker exec -it charity-mysql mysql -u root -p
ALTER USER 'root'@'%' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

### Lỗi: "Unknown database 'charity_platform'"

**Giải pháp**:
- Database chưa được tạo. Spring Boot sẽ tự tạo khi chạy lần đầu với `ddl-auto: create-drop`
- Hoặc tạo thủ công:
```bash
docker exec -it charity-mysql mysql -u root -p
CREATE DATABASE charity_platform;
```

---

## 📝 Lưu ý

1. **Port 3307**: Được map từ container port 3306 để tránh conflict với MySQL local (nếu có)

2. **File .env**: Nếu chưa có, tạo file `.env` với:
   ```env
   DBMS_USERNAME=root
   DBMS_PASSWORD=your_password
   DBMS_CONNECTION=jdbc:mysql://localhost:3307/charity_platform
   ```

3. **Security**: Không commit file `.env` vào git (đã có trong `.gitignore`)

4. **Backup Database**:
   ```bash
   # Export database
   docker exec charity-mysql mysqldump -u root -p charity_platform > backup.sql
   
   # Import database
   docker exec -i charity-mysql mysql -u root -p charity_platform < backup.sql
   ```

---

## 🎯 Quick Start

**Cách nhanh nhất để xem database**:

```bash
# 1. Vào MySQL shell
docker exec -it charity-mysql mysql -u root -p

# 2. Chọn database
USE charity_platform;

# 3. Xem tables
SHOW TABLES;

# 4. Xem dữ liệu
SELECT * FROM users LIMIT 5;
```

---

**Last Updated**: 2024-11-26

