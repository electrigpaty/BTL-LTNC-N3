# BTL-LTNC-N3
Hướng dẫn sử dụng git và commit code
---

## 📂 1. Cấu trúc Thư mục & Phân chia Module

Dự án được chia thành 3 module chính

*   **`auction-common/`**: Chứa các Model (OOP), DTO, và Enum chung cho cả Client và Server.
*   **`auction-server/`**: Chứa mã nguồn Server Socket, kết nối Database (DAO), các thuật toán xử lý đồng thời, Anti-sniping và Auto-Bidding.
*   **`auction-client/`**: Chứa giao diện JavaFX/FXML, điều hướng màn hình và Socket Client kết nối lên Server.

  online-auction-system/              <-- Thư mục gốc Repo
├── .github/
│   └── workflows/
│       └── ci.yml                 <-- CI/CD GitHub Actions (TV1 thiết lập)
├── .gitignore                      <-- Bỏ qua .idea, target/, .DS_Store...
├── README.md                       <-- Mô tả dự án, phân công & hướng dẫn chạy
├── pom.xml                         <-- POM tổng (Parent Maven)
│
├── auction-common/                 <-- [TV3 & TV4 phụ trách]
│   ├── pom.xml
│   └── src/main/java/com/auction/common/
│       ├── model/                  <-- Cây kế thừa OOP
│       │   ├── Entity.java         <-- Abstract base
│       │   ├── user/
│       │   │   ├── User.java       <-- Abstract
│       │   │   ├── Bidder.java
│       │   │   ├── Seller.java
│       │   │   └── Admin.java
│       │   └── item/
│       │       ├── Item.java       <-- Abstract
│       │       ├── Electronics.java
│       │       ├── Art.java
│       │       └── Vehicle.java
│       ├── dto/                    <-- Dữ liệu truyền qua Socket (Request/Response)
│       │   ├── BidRequest.java
│       │   └── AuctionStatusDTO.java
│       └── enums/                  <-- OPEN, RUNNING, FINISHED, PAID, CANCELED
│           └── AuctionState.java
│
├── auction-server/                 <-- [TV1 phụ trách chính + TV4 hỗ trợ]
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/auction/server/
│       │   ├── config/             <-- Config Server, DB connection (Singleton)
│       │   ├── dao/                <-- Database Access Objects (UserDAO, ItemDAO...)
│       │   ├── net/                <-- Server Socket Listener, ThreadPool
│       │   ├── service/            <-- Logic core
│       │   │   ├── AuctionManager.java   <-- Anti-sniping, Concurrent Bidding
│       │   │   └── AutoBidEngine.java    <-- TV4: PriorityQueue cho Auto-Bidding
│       │   └── observer/           <-- Observer Pattern quản lý danh sách Client
│       └── test/java/              <-- TV4: Unit Test JUnit
│           └── AuctionManagerTest.java
│
└── auction-client/                 <-- [TV2 phụ trách chính]
    ├── pom.xml
    └── src/main/
        ├── java/com/auction/client/
        │   ├── App.java            <-- Hàm main() chạy JavaFX
        │   ├── net/                <-- SocketClient kết nối lên Server
        │   └── controller/         <-- Controller các màn hình
        │       ├── LoginController.java
        │       ├── AuctionListController.java
        │       ├── ItemDetailController.java
        │       └── LiveBiddingController.java  <-- Vẽ biểu đồ LineChart Realtime
        └── resources/
            └── fxml/               <-- Các file giao diện JavaFX (.fxml)
                ├── login.fxml
                ├── auction_list.fxml
                └── live_bidding.fxml

---

## 👩‍💻 2. Phân công Công việc

### 🔹 Thành viên 1: Backend & Database (`auction-server`)
*   **Nhiệm vụ chính:** Xây dựng Server Socket lắng nghe kết nối[cite: 9, 12], thiết lập kết nối Database (áp dụng *Singleton Pattern*)[cite: 10, 12].
*   **Xử lý kỹ thuật:** Lập trình xử lý đồng thời (*Concurrency*: `synchronized` / `ReentrantLock`) để tránh mất dữ liệu (*lost update* / *race condition*)[cite: 8, 11, 12].
*   **Thuật toán nâng cao:** Cài đặt thuật toán gia hạn phiên đấu giá (*Anti-sniping*)[cite: 8, 12].
*   **CI/CD:** Thiết lập GitHub Actions chạy test tự động[cite: 10, 12].

### 🔹 Thành viên 2: Client & Giao diện UI (`auction-client`)
*   **Nhiệm vụ chính:** Thiết kế toàn bộ giao diện JavaFX / FXML theo mô hình MVC Client[cite: 7, 10, 12].
*   **Các màn hình bắt buộc:**
    1. Màn hình Đăng nhập / Đăng ký[cite: 5, 7]
    2. Màn hình Danh sách phiên đấu giá & Chi tiết sản phẩm[cite: 5, 7]
    3. Màn hình Đấu giá trực tiếp (*Live Bidding*) kèm **Biểu đồ giá Realtime (`LineChart`)**[cite: 7, 8, 12]
    4. Màn hình Quản lý sản phẩm dành cho Seller[cite: 5, 7]

### 🔹 Thành viên 3: OOP Models & Core Logic (`auction-common`)
*   **Nhiệm vụ chính:** Xây dựng cây phân cấp hướng đối tượng chuẩn OOP trong module `auction-common`[cite: 6, 9, 12]:
    *   `Entity` $\rightarrow$ `User` (`Bidder`, `Seller`, `Admin`)[cite: 6, 9]
    *   `Entity` $\rightarrow$ `Item` (`Electronics`, `Art`, `Vehicle`, ...)[cite: 6, 9]
    *   `Auction`, `BidTransaction`[cite: 6, 9]
*   **Design Pattern:** Áp dụng *Factory Method Pattern* để tạo các loại sản phẩm và tuân thủ chặt chẽ các nguyên tắc OOP (*Encapsulation, Inheritance, Polymorphism, Abstraction*)[cite: 6, 10, 11, 12].

### 🔹 Thành viên 4: Realtime, Auto-Bidding & Testing (`auction-server` & `auction-client`)
*   **Nhiệm vụ chính:** Xây dựng cơ chế cập nhật thời gian thực (*Realtime Update*) sử dụng *Observer Pattern* kết hợp Socket[cite: 8, 10, 12].
*   **Thuật toán nâng cao:** Xây dựng tính năng **Auto-Bidding** (Đấu giá tự động dùng `PriorityQueue`)[cite: 7, 11, 12].
*   **Kiểm thử:** Viết Unit Test bằng *JUnit* cho toàn bộ các luồng logic quan trọng[cite: 10, 11, 12].

---

## 🛠️ 3. Hướng dẫn tạo Package trung gian chuẩn Java (Dành cho người mới)

Vì Java tuân thủ quy tắc nghiêm ngặt: **Tên gói (Package) trên file code phải khớp tuyệt đối với cấu trúc thư mục vật lý**, nhóm sẽ tuân theo cấu trúc đặt tên chung là `com.auction.[tên_module]`.

### Hướng dẫn thao tác trên IntelliJ IDEA:
1. Mở thư mục module tương ứng (ví dụ `auction-server`), tìm đến thư mục:
   `src/main/java`
2. Bấm chuột phải vào `java`  Chọn **New** **Package**.
3. Nhập tên gói trung gian đầu tiên: `com` Bấm Enter.
4. Bấm chuột phải tiếp vào gói `com` vừa tạo Chọn **New** **Package** Nhập tiếp: `auction`.
5. Bấm chuột phải vào gói `auction` Chọn **New** **Package** Nhập tên module cụ thể, ví dụ: `server` (hoặc `client`, `common`).
6. **Bên trong package cuối cùng (`server`), bạn mới bắt đầu tạo các thư mục chức năng con** (cũng bằng cách bấm chuột phải chọn New Package) gồm:
   * `dao` (để chứa các file kết nối cơ sở dữ liệu)
   * `net` (để chứa các file Socket xử lý mạng)
   * `service` (để chứa các file xử lý logic nghiệp vụ)
   * `observer` (để chứa các file thông báo thời gian thực)
---

## 📌 4. Quy định làm việc nhóm với Git & GitHub

1. **Không bao giờ Push trực tiếp lên nhánh `main`**.
2. Khi bắt đầu làm một tính năng hoặc task mới, hãy tự tạo một nhánh riêng (*Branch*) trên máy của mình:
   ```bash
   git checkout -b feature/ten-tinh-nang
