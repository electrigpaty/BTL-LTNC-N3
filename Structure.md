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