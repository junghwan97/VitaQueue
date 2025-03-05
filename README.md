# **🌟VitaQueue 프로젝트 소개**
**📅MVP 개발기간**: 2024.12.18 - 2025.02.20

- VitaQueue는 한정된 재고를 가진 상품을 보다 공정한 방식으로 제공하기 위한 이커머스 플랫폼입니다.  
  본 프로젝트는 고성능 트래픽 처리 및 동시성 문제 해결에 초점을 맞춰 설계되었으며 대량의 사용자 요청이 발생하는 환경에서도 원활한 서비스 제공이 가능하도록 아키텍처를 구성합니다.  
- 목표는 트랜잭션 일관성과 응답 속도를 보장하는 동시에, 사용자의 구매 경험을 최적화하는 것이며, 이를 위해 분산 시스템 설계, 캐싱 전략, 성능테스트를 활용하여 성능을 극대화 했습니다.

## 프로젝트 실행 방법

### Git 클론 후 env 파일 설정
```bash
git clone https://github.com/your-repository/VitaQueue.git
cd VitaQueue
```
<details>
<summary>env 파일 예시</summary>
<div markdown="1">
  
- 공통 설정
  - JWT_SECRET_KEY=your_secret_key

- User DB
  - USER_MYSQL_DATABASE=user_db
  - USER_MYSQL_USERNAME=user
  - USER_MYSQL_PASSWORD=user_password

- Product DB
  - PRODUCT_MYSQL_DATABASE=product_db
  - PRODUCT_MYSQL_USERNAME=product
  - PRODUCT_MYSQL_PASSWORD=product_password

- Order DB
  - ORDER_MYSQL_DATABASE=order_db
  - ORDER_MYSQL_USERNAME=order
  - ORDER_MYSQL_PASSWORD=order_password

- Wishlist DB
  - WISHLIST_MYSQL_DATABASE=wishlist_db
  - WISHLIST_MYSQL_USERNAME=wishlist
  - WISHLIST_MYSQL_PASSWORD=wishlist_password

</div>
</details>

### **Docker Compose로 실행**
```bash
docker-compose up --build
```

---
## 🛠️개발 환경 및 기술 스택

### 🖥️언어
- **Java**: 17
- **Gradle**: 8.11.1

### 🚀프레임워크
- **Spring Boot**: 3.3.6

### 📚라이브러리
- **Spring Data JPA(Hibernate)**
- **Spring Security**
- **Spring Cloud Gateway**
- **Spring Cloud Netflix Eureka**
- **Spring Cloud OpenFeign**

### 🗄️DB
- **MySQL**: 8.0
- **Redis**: 7.4.1

### 🛳️Infrastructure
- **Docker**


### **📑API 문서**
[API 문서 바로가기](https://documenter.getpostman.com/view/30963150/2sAYJ3DfzK)

---

# **📝VitaQueue 프로젝트 설명**


## **🏗️아키텍처**
![아키텍처 구조](https://github.com/user-attachments/assets/748f0819-9e1d-418a-a531-e8c3f7ffbe37)

## **⚙️주요 기능**
1. 한정 수량 상품 구매 최적화
- Redis 기반 상품 재고 관리로 데이터베이스 부하 감소 및 성능 향상
- 스케줄러를 활용한 주문 상태 자동 관리

2. MSA 아키텍처 기반 서비스 모듈 통신
- Open Feign을 활용한 마이크로서비스 간 연동
- API Gateway를 통한 JWT 인증 및 요청 라우팅

3. 보안 및 인증 시스템
- Spring Security 기반 회원 가입 및 사용자 인증
- JWT 로그인 및 인증 처리
- Google SMTP 이메일 인증

4. 운영 및 모니터링
- Prometheus + Grafana 기반 실시간 모니터링 시스템 구축

## Sequence Diagram
<details>
<summary>🏗 Sequence Diagram</summary>
<div markdown="1">

![Sequence Diagram](https://github.com/user-attachments/assets/042b3277-51ec-4748-b260-83cfc496ba73)
- alt == 분기 처리
</div>
</details>

---

# 성능 개선
## Redis를 활용한 재고 동시성 제어

### 문제점 & 개선 방안
🔻 락 획득 시간이 길어지면 TPS 저하  
✅ Lua 스크립트로 락 없이 원자적 재고 차감  

🔻 대량 요청 시 락 획득 실패율 증가  
✅ 네트워크 왕복 없이 Redis에서 바로 처리  


### 📊 성능 비교

**Redis 분산락 → Lua 스크립트 적용 후 성능 변화 비교**
| 방식 | 재고 정합성 | 평균 응답 시간(ms) | TPS |
|:----:|:------:|:------:|:------:|
| **Redis 분산락 적용** | ✅ (정합성 유지) | 1049 | 8 |
| **Lua 스크립트 적용** | ✅ (정합성 유지) | 117 | 72 |

### 🔍 결과 분석
✅ Lua 스크립트 적용 후 TPS가 약 9배 증가  
✅ 평균 응답 시간이 1049ms → 117ms로 88%감소  
✅ 동시성 성능 향상 결과로 처리 속도와 TPS 모두 대폭 향상

---

### 🎯 최종 결론
🔹 Redis 분산락 방식은 동시성 처리를 개선할 수 있지만, 락 경합과 성능 저하 문제가 발생할 수 있음.  
🔹 Lua 스크립트를 활용하면 락 없이 원자적으로 재고를 처리할 수 있어, 성능 향상과 정합성 유지를 동시에 달성할 수 있음.  
🔹 결과적으로, Lua 기반 재고 차감 방식이 더 빠르고 안정적인 해결책이 됨.

---

# **트러블슈팅**

 **1. 재고 관리 동시성 문제**
  - 문제:
    - MSA 환경에서 Feign Client 호출 시 재고 차감 트랜잭션 유지 실패
    - 주문 서비스와 상품 서비스 간 통신 과정에서 재고 정합성 깨짐
  - 원인: 
    - 비관적 락이 서비스 경계를 넘어 전파되지 않음
    - 분산 트랜잭션 미적용으로 인한 데이터 불일치
  - 해결: 
    - Redis를 사용한 재고 관리로 전환
    - 스케줄러를 통해 Redis의 재고 데이터를 주기적으로 DB와 동기화
    - 실시간 재고는 Redis에서 관리하고, DB는 최종 정합성 보장
  - 개선 효과: 
    - Redis의 빠른 처리 속도로 성능 향상
    - 주기적 동기화로 안정적인 데이터 관리

 **2. 모니터링 시스템 부재**
  - 문제: 시스템 장애 감지 및 분석 어려움
  - 원인: 모니터링 도구 부재
  - 해결: Prometheus와 Grafana를 활용한 모니터링 시스템 구축  
