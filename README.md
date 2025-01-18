# **VitaQueue 프로젝트 소개**
**기간**: 2024.12.18 - 2025.01.00

- Vitaqueue는 한정된 재고를 가진 상품을 구매하려는 사용자들이 공정한 과정을 통해 구매할 수 있는 이커머스 플랫폼입니다.
- 이 프로젝트는 높은 동시성 환경에서 사용자가 겪는 불편함을 최소화하는 데 중점을 두어 설계되었습니다.

## **개발 환경 및 기술 스택**

### **개발 환경**

#### **언어**
![Java 17](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=OpenJDK&logoColor=white)
![Gradle 8.11.1](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white)

#### **프레임워크**
![Spring Boot 3.3.6](https://img.shields.io/badge/Spring%20Boot%203.3.6-6DB33F?style=for-the-badge&logo=Spring%20Boot&logoColor=white)

### **라이브러리**
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=Spring%20Security&logoColor=white)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-6DB33F?style=for-the-badge&logo=Spring%20Cloud%20Gateway&logoColor=white)
![Spring Cloud Netflix Eureka](https://img.shields.io/badge/Spring%20Cloud%20Netflix%20Eureka-6DB33F?style=for-the-badge&logo=Spring%20Cloud%20Netflix%20Eureka&logoColor=white)
![Spring Cloud OpenFeign](https://img.shields.io/badge/Spring%20Cloud%20OpenFeign-6DB33F?style=for-the-badge&logo=OpenFeign&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)


### **DB**
![MySQL 8.0](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### **infrastructure**
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white)

## **API 문서**

[API 문서](https://documenter.getpostman.com/view/30963150/2sAYJ3DfzK)

---

# **VitaQueue 프로젝트 설명**

## **아키텍처**

![아키텍처 구조](https://github.com/user-attachments/assets/94ce572b-622e-49b4-bfe5-4cb55678f55a)

## **주요 기능**

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
- **Prometheus**와 **Grafana**를 활용하여 모니터링 시스템 구축 v

---

# **성능 개선**
- **Redis 분산락 적용을 통한 재고 동시성 처리 개선**
- 기술적 의사결정:
  - 비관적 락 사용 시 락 획득 지연에 따른 성능 저하 발생
  - 낙관적 락 사용 시 많은 요청으로 인한 충돌 가능성이 높기 때문에 적절하지 않음
  - Redis 분산락을 활용하여 더욱 빠른 락 획득 / 해제 가능
- **인덱스 적용을 통한 조회 성능 최적화** v 
- **메시지 큐를 활용한 데이터 일관성 강화** v

---

# **트러블슈팅**
- **수평 확장 시 @scheduled 어노테이션 사용 시 발생하는 문제점 개선** v
- - **캐시 DB  Replication** v
