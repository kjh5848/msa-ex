# 프론트 -> 게이트웨이 라우팅 설명

프론트엔드는 모든 백엔드 요청을 **게이트웨이로만** 보내고, 게이트웨이가 내부 서비스로 라우팅합니다.  
즉, **프론트는 여러 마이크로서비스의 주소를 몰라도** 되고, 게이트웨이가 단일 진입점 역할을 합니다.

## 구성도 (Mermaid, LR)

### 요약 구성도

```mermaid
flowchart LR
  B[브라우저] -->|포트포워드 3000| FE[Frontend]
  FE -->|/api/* 프록시| GW[Gateway]
  subgraph KIND[Kind 클러스터]
    FE
    GW
    S[내부 서비스들]
  end
  GW --> S
```

### 상세 구성도

```mermaid
flowchart LR
  subgraph DEV[로컬 개발 환경]
    B[브라우저]
    D[Docker Desktop]
  end

  subgraph KIND[Kind 클러스터]
    N1[노드: msa-cluster-control-plane]

    subgraph NS[네임스페이스: metacoding]
      GW[Gateway Service]
      FE[Frontend Service]
      O[Order Service]
      P[Product Service]
      U[User Service]
      DL[Delivery Service]
      K[Kafka Service]
      DB[DB Service]
      OR[Orchestrator Service]
    end
  end

  B -- 포트포워드 3000 --> FE
  FE -- /api/* 프록시 --> GW
  GW --> O
  GW --> P
  GW --> U
  GW --> DL
  O --> DB
  P --> DB
  U --> DB
  OR --> K
  DL --> K
  D --> KIND
```

## 왜 필요한가?

1. **단일 진입점 (Single Entry Point)**
   - 프론트는 `gateway` 주소만 알면 됩니다.
   - 서비스가 늘어나도 프론트 코드는 거의 바뀌지 않습니다.

2. **보안/인증 처리의 중앙화**
   - JWT 검사, 사용자 식별 등을 게이트웨이에서 한 번만 처리합니다.
   - 각 서비스에 인증 로직을 중복으로 넣지 않아도 됩니다.

3. **라우팅 규칙의 일관성**
   - `/api/orders`, `/api/users`, `/api/deliveries` 같은 규칙을 게이트웨이에서 관리합니다.
   - 프론트는 규칙에 맞춰 호출만 하면 됩니다.

4. **운영 편의성**
   - 서비스의 실제 주소가 바뀌어도 게이트웨이 설정만 바꾸면 됩니다.
   - 로깅/모니터링 포인트를 한 곳에 모을 수 있습니다.

## 이 프로젝트에서의 실제 흐름

- **브라우저** → `frontend-service`
- **프론트(Nginx)** → `gateway-service`로 `/api/*` 요청 프록시
- **게이트웨이** → 각 마이크로서비스로 라우팅

예시:
```
브라우저 -> Frontend(/api/orders) -> Gateway(/api/orders) -> Order Service(/orders)
```

## 관련 설정 위치

- 프론트 프록시: `frontend/nginx.conf`
- 게이트웨이 라우팅: `api-gateway/src/main/java/com/metacoding/gateway/controller/GatewayController.java`

## 예시 흐름 (Mermaid, LR)

### 주문 생성 흐름 (Order)

```mermaid
flowchart LR
  B[브라우저] -->|POST /api/orders| FE[Frontend]
  FE -->|/api/* 프록시| GW[Gateway]
  GW -->|/api/orders -> /orders| OS[Order Service]
  OS --> DB[(DB)]
  OS --> K[(Kafka)]
  OS --> GW
  GW --> FE
  FE --> B
```

### 배달 완료 흐름 (Delivery Complete)

```mermaid
flowchart LR
  B[브라우저] -->|PUT /api/deliveries/orderId/complete| FE[Frontend]
  FE -->|/api/* 프록시| GW[Gateway]
  GW -->|/api/deliveries/* -> /deliveries/*| DS[Delivery Service]
  DS --> K[(Kafka: complete-delivery-command)]
  DS --> GW
  GW --> FE
  FE --> B
```

## 시퀀스 다이어그램 (Mermaid)

### 주문 생성 시퀀스

```mermaid
sequenceDiagram
  participant B as 브라우저
  participant FE as Frontend
  participant GW as Gateway
  participant O as Order Service
  participant DB as DB
  participant K as Kafka

  B->>FE: POST /api/orders
  FE->>GW: 프록시 /api/orders
  GW->>O: POST /orders
  O->>DB: 주문 저장
  O->>K: 주문 이벤트 발행
  O-->>GW: 응답
  GW-->>FE: 응답
  FE-->>B: 응답
```

### 배달 완료 시퀀스

```mermaid
sequenceDiagram
  participant B as 브라우저
  participant FE as Frontend
  participant GW as Gateway
  participant D as Delivery Service
  participant K as Kafka

  B->>FE: PUT /api/deliveries/orderId/complete
  FE->>GW: 프록시 /api/deliveries/orderId/complete
  GW->>D: PUT /deliveries/orderId/complete
  D->>K: complete-delivery-command 발행
  D-->>GW: 응답
  GW-->>FE: 응답
  FE-->>B: 응답
```

## Mermaid 다이어그램

### Flow

```mermaid
flowchart LR
  B[Browser] --> FE[Frontend]
  FE -->|/api/*| GW[Gateway]
  GW --> O[Order]
  GW --> P[Product]
  GW --> U[User]
  GW --> D[Delivery]
```

### Sequence

```mermaid
sequenceDiagram
  participant B as Browser
  participant FE as Frontend
  participant GW as Gateway
  participant O as Order Service

  B->>FE: POST /api/orders
  FE->>GW: Proxy /api/orders
  GW->>O: POST /orders
  O-->>GW: 201 Created
  GW-->>FE: 201 Created
  FE-->>B: 201 Created
```

