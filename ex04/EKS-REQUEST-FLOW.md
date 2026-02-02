# EKS 요청 흐름 시나리오

이 문서는 `msa-ex` 프로젝트의 EKS 아키텍처에서 요청이 어떻게 흐르는지 설명합니다. 사용자의 요청이 인터넷에서 데이터베이스까지 도달하는 전 과정을 다룹니다.

## 아키텍처 개요

**클라이언트** → **NLB** (AWS) → **프론트엔드** (Nginx Pod) → **게이트웨이** (Spring Gateway Pod) → **마이크로서비스** (Spring Boot Pod) → **RDS** (AWS DB)

## 상세 단계별 흐름

### 1단계: 외부 진입 (AWS 네트워크 레벨)
1.  **클라이언트 (브라우저/앱)**
    *   사용자가 NLB(Network Load Balancer) URL을 통해 애플리케이션에 접속합니다.
2.  **AWS NLB (Network Load Balancer)**
    *   리스너 포트(TCP 80)에서 트래픽을 수신합니다.
    *   TCP 패킷의 소스 IP를 **변경하지 않고(Client IP 보존)** EKS 워커 노드로 그대로 전달합니다.

### 2단계: 클러스터 진입 (쿠버네티스 서비스 레벨)
3.  **프론트엔드 서비스 (NodePort/LoadBalancer)**
    *   서비스가 NLB로부터 트래픽을 받습니다.
    *   상태가 정상인 **프론트엔드 Pod** 중 하나로 트래픽을 라우팅합니다.
4.  **프론트엔드 Pod (Nginx)**
    *   Nginx가 **리버스 프록시(Reverse Proxy)** 역할을 수행합니다.
    *   요청 URI를 검사합니다.
        *   `/api/*`: 내부 **게이트웨이 서비스**로 프록시(전달)합니다.
        *   `/*`: 정적 파일(React/HTML)을 제공합니다.
    *   **핵심 설정**: `nginx.conf` 내부의 `proxy_pass http://gateway-service:8080;`

### 3단계: 내부 라우팅 (서비스 간 통신)
5.  **게이트웨이 서비스 (ClusterIP)**
    *   게이트웨이 Pod들을 위한 고정된 내부 IP 주소입니다.
6.  **게이트웨이 Pod (Spring Cloud Gateway)**
    *   **인증/인가(Authentication/Authorization)**를 처리합니다 (JWT 검증).
    *   경로에 따라 특정 마이크로서비스로 라우팅합니다 (예: `/user/**` -> `user-service`).
    *   **핵심 설정**: `application.properties`의 `gateway.services.user=http://user-service:8083`

### 4단계: 비즈니스 로직 및 데이터 영속성
7.  **마이크로서비스 Pod (예: User 서비스)**
    *   비즈니스 로직을 실행합니다 (Java/Spring Boot).
8.  **AWS RDS (데이터베이스)**
    *   애플리케이션이 JDBC를 통해 AWS RDS 인스턴스에 연결합니다.
    *   SQL 쿼리를 실행하고 데이터를 반환합니다.

### 5단계: 응답 경로
*   응답은 요청과 동일한 경로를 역순으로 이동합니다:
    *   RDS → 마이크로서비스 → 게이트웨이 → 프론트엔드(Nginx) → NLB → 클라이언트.

## 보안 요약
*   **Public Zone (공용 영역)**: 오직 NLB만 인터넷에 노출됩니다.
*   **Private Zone (사설 영역)**: EKS 클러스터, 마이크로서비스, RDS는 사설 서브넷(Private Subnet)에 위치하거나 내부 보안 그룹으로 보호됩니다. 프론트엔드(Nginx)가 HTTP 트래픽의 유일한 진입점입니다.
