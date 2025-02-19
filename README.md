# **🌟VitaQueue 프로젝트 소개**
**📅MVP 개발기간**: 2024.12.18 - 2025.01.18

- Vitaqueue는 한정된 재고를 가진 상품을 구매하려는 사용자들이 공정한 과정을 통해 구매할 수 있는 이커머스 플랫폼입니다.
- 이 프로젝트는 높은 동시성 환경에서 사용자가 겪는 불편함을 최소화하는 데 중점을 두어 설계되었습니다.

## 프로젝트 실행 방법

### 1. **환경 요구 사항**
- **Docker** 및 **Docker Compose** 설치
- MySQL Workbench 또는 기타 데이터베이스 클라이언트 (선택 사항)

### 2. **프로젝트 클론**
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

### 3. **Docker Compose로 실행**
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
![아키텍처 구조](https://github.com/user-attachments/assets/06cdb835-5893-4c96-9b40-1ce73386f49d)

## **⚙️주요 기능**

- 한정 수량 상품 구매 기능
- **Redis 캐싱**기반의 상품 재고 관리
  - 데이터베이스 부하 감소 및 재고 관리 성능 향상
- **Open Feign**을 활용한 MSA 서비스 모듈 간 통신 구현
- **API Gateway**를 JWT 검증 및 요청 라우팅 처리
  - 단일 진입점에서 JWT 인증과 요청 분배를 담당
- **Spring Security**를 활용하여 회원 가입 및 사용자 인증 관리
- Google SMTP를 활용하여 이메일 인증
- JWT를 활용하여 로그인 구현
- **스케줄러**를 통한 주문 상태 관리
  - 시간의 흐름에 따라 주문 상태 변경
- **Prometheus**와 **Grafana**를 활용하여 모니터링 시스템 구축

<details>
<summary>🏗 Sequence Diagram</summary>
<div markdown="1">

![Sequence Diagram](https://github.com/user-attachments/assets/32a8be83-2b84-45c8-af69-f2ac9c0d4b2c)

</div>
</details>

---

# 성능 개선
## Redis를 활용한 재고 동시성 제어

### **Redis 분산락(Redisson) 기반 재고 관리**
- **Redis를 활용한 분산락을 사용하여 동시성 문제 해결**
- **Redisson을 이용해 락을 걸고, 재고 차감 후 락을 해제**
- **DB 부하를 줄이기 위해 캐싱을 적용했으나, 락 경합이 발생할 가능성이 존재**

### **발생한 문제**
1. **Redis 락 획득 시간이 길어질 경우 성능 저하**  
2. **대량 요청이 발생하면 락 획득 실패율 증가**  
3. **락을 오래 유지하면 전체적인 TPS 저하 발생**  

## 🔧 **개선 과정**
### ✅ **Lua 스크립트 적용**
- **락 없이 Redis에서 원자적으로 재고 차감**
- **네트워크 왕복 없이 빠르게 동시 요청 처리**
- **DB 업데이트 실패 시 Redis 재고 복구 가능**  

## 📊 성능 비교

**Redis 분산락 → Lua 스크립트 적용 후 성능 변화 비교**
| 방식 | 재고 정합성 | 평균 응답 시간(ms) | TPS(처리량) |
|:----:|:------:|:------:|:------:|
| **Redis 분산락 적용** | ⚠️ (불일치 가능) | `{{ Redis 락 응답 시간 }}` | `{{ Redis TPS }}` |
| **Lua 스크립트 적용** | ✅ (완벽 유지) | `{{ Lua 응답 시간 }}` | `{{ Lua TPS }}` |

✅ **Redis 분산락을 적용하면 처리량(TPS)이 증가하지만, 락 경합으로 인한 성능 한계 존재**  
✅ **Lua 스크립트 적용 시 락 없이도 정합성을 유지하며, 처리 속도와 TPS 모두 크게 향상**  

---

### 🔥 개선 효과 정리
- **Redis 분산락 적용 후**  
  - 락을 활용해 동시 요청을 직렬화하여 DB 부하 감소  
  - 하지만, **락 획득 지연 및 Redis-DB 정합성 불일치 발생 가능**  

- **Lua 스크립트 적용 후**  
  - **락 없이도 원자적 연산을 수행하여 정합성 유지**  
  - **네트워크 왕복 횟수 감소 → 성능 향상**  
  - **처리량(TPS) 증가 및 평균 응답 시간 단축**  

### 🎯 최종 결론
✅ **Redis 분산락을 적용하면 동시성 처리는 개선되지만 락 경합 및 정합성 문제가 존재**  
✅ **Lua 스크립트를 활용하면 락 없이 빠르고 안전한 재고 처리가 가능하며, 성능이 크게 향상됨**

---

# **트러블슈팅**

- **재고 관리 동시성 문제**
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

- **모니터링 시스템 부재**
  - 문제: 시스템 장애 감지 및 분석 어려움
  - 원인: 모니터링 도구 부재
  - 해결: Prometheus와 Grafana를 활용한 모니터링 시스템 구축  
