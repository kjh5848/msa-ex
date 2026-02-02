# EKS 배포 가이드 - MSA 이커머스 프로젝트

## 📋 프로젝트 개요

| 구성 요소 | 기술 스택 |
|-----------|-----------|
| **언어** | Java 21 |
| **프레임워크** | Spring Boot 3.2.9 |
| **메시지 브로커** | Apache Kafka (KRaft 모드) |
| **데이터베이스** | Amazon RDS (MySQL) |
| **컨테이너 레지스트리** | Amazon ECR |
| **오케스트레이션** | Amazon EKS |

### 서비스 목록

| 서비스 | 포트 | 설명 |
|--------|------|------|
| api-gateway | 8080 | JWT 인증, 라우팅 |
| order | 8081 | 주문 서비스 |
| product | 8082 | 상품 서비스 |
| user | 8083 | 사용자 서비스 |
| delivery | 8084 | 배송 서비스 |
| orchestrator | - | Saga 패턴 조정자 |
| frontend | 80 | Nginx 프론트엔드 |

---

## 🔐 1단계: AWS 자격증명 설정

### AWS 자격증명 방법 (5가지)

| 순위 | 방법 | 설명 | 적용 범위 |
|------|------|------|-----------|
| 1️⃣ | **환경변수** | `$env:AWS_ACCESS_KEY_ID` | 현재 세션만 |
| 2️⃣ | **Credentials 파일** | `~/.aws/credentials` | 영구 저장 ✅ |
| 3️⃣ | **Config 파일** | `~/.aws/config` | 영구 저장 |
| 4️⃣ | **IAM Role** | EC2/EKS 자동 부여 | AWS만 |
| 5️⃣ | **SSO** | `aws sso login` | 기업 환경 |

> **💡 개인 로컬에서는 Credentials 파일 방식이 가장 편합니다!**

### Credentials 파일 설정 (권장)

```powershell
# 자격증명 설정
aws configure

# 입력 예시:
# AWS Access Key ID: AKIA...
# AWS Secret Access Key: ...
# Default region name: ap-northeast-2
# Default output format: json
```

### 설정 확인

```powershell
# 자격증명 목록 확인
aws configure list

# 계정 정보 확인
aws sts get-caller-identity
```

---

## 📦 2단계: 준비물 체크리스트

### ✅ 필수 준비물

| 항목 | 설명 | 확인 방법 |
|------|------|-----------|
| **AWS Access Key ID** | IAM 사용자 액세스 키 | `aws configure list` |
| **AWS Secret Access Key** | IAM 사용자 비밀 키 | `aws configure list` |
| **AWS Account ID** | AWS 계정 번호 (12자리) | `aws sts get-caller-identity` |
| **Docker Desktop** | 이미지 빌드용 (WSL2 백엔드) | `docker info` |
| **kubectl** | K8s 클러스터 제어 | `kubectl version --client` |
| **eksctl** | EKS 클러스터 생성 (선택) | `eksctl version` |

### ✅ 생성할 AWS 리소스

| 리소스 | 용도 | 비용 (월 추정) |
|--------|------|----------------|
| **EKS 클러스터** | Kubernetes 관리형 서비스 | $73 (클러스터) + 노드 비용 |
| **EC2 노드그룹** | 워커 노드 (t3.medium x 2~3) | ~$60~90 |
| **ECR 리포지토리** | Docker 이미지 저장소 (7개) | ~$1~5 |
| **RDS MySQL** | 데이터베이스 (db.t3.micro) | ~$15~20 |
| **ALB/NLB** | 로드밸런서 (선택) | ~$20 |

> ⚠️ **비용 주의**: 실습 후 반드시 리소스 삭제!

---

## 🚀 3단계: EKS 클러스터 생성

### 방법 1: eksctl 사용 (권장)

```powershell
# EKS 클러스터 생성 (15~20분 소요)
eksctl create cluster `
    --name metacoding `
    --region ap-northeast-2 `
    --version 1.28 `
    --nodegroup-name standard-workers `
    --node-type t3.medium `
    --nodes 2 `
    --nodes-min 2 `
    --nodes-max 3 `
    --managed

# kubeconfig 자동 등록 확인
kubectl get nodes
```

### 방법 2: AWS 콘솔 사용

1. **EKS 콘솔** → **클러스터 생성**
2. 이름: `metacoding`
3. Kubernetes 버전: `1.28`
4. 노드그룹 추가: `t3.medium` x 2

### kubeconfig 수동 등록

```powershell
aws eks update-kubeconfig --name metacoding --region ap-northeast-2
kubectl get nodes
```

---

## 🗄️ 4단계: RDS MySQL 생성

### AWS 콘솔에서 RDS 생성

1. **RDS 콘솔** → **데이터베이스 생성**
2. 엔진: **MySQL 8.0**
3. 템플릿: **프리 티어** (db.t3.micro)
4. DB 인스턴스 식별자: `metacoding-db`
5. 마스터 사용자 이름: `admin`
6. 마스터 암호: `원하는 비밀번호`
7. **퍼블릭 액세스**: 아니요 (EKS에서만 접근)
8. **VPC**: EKS와 동일한 VPC 선택
9. **생성** 클릭 (5~10분 소요)

### RDS 보안 그룹 설정

1. RDS 상세 → **보안 그룹** 클릭
2. **인바운드 규칙 편집**
3. 규칙 추가:
   - 유형: `MySQL/Aurora`
   - 포트: `3306`
   - 소스: EKS 노드 보안 그룹 또는 `0.0.0.0/0` (테스트용)

### RDS 엔드포인트 확인

```
예시: metacoding-db.abc123xyz.ap-northeast-2.rds.amazonaws.com
```

### 초기 스키마 생성

mysql-client 파드로 접속하여 `db/init.sql` 실행:

```powershell
# 임시 MySQL 클라이언트 파드 생성
kubectl run mysql-client --image=mysql:8 --restart=Never --command -- sleep 3600

# 파드 준비 대기
kubectl wait --for=condition=ready pod/mysql-client --timeout=60s

# RDS 접속 및 스키마 생성
kubectl exec -it mysql-client -- mysql -h <RDS_ENDPOINT> -u admin -p

# MySQL 프롬프트에서 실행:
CREATE DATABASE metacoding_msa;

# init.sql 실행
Get-Content ./db/init.sql | kubectl exec -i mysql-client -- mysql -h metacoding-db.cfcouus4ix90.ap-northeast-2.rds.amazonaws.com -u admin -pmetacoding metacoding_msa\

# 데이터 확인
kubectl exec -it mysql-client -- mysql -h metacofing-msa.cfcouus4ix90.ap-northeast-2.rds.amazonaws.com -u metacoding -pmetacoding -e "USE metacoding_msa; SHOW TABLES; SELECT * FROM orders LIMIT 10;"

# 임시 파드 삭제
kubectl delete pod mysql-client
```

---

## 🛡️ 4-1단계: 보안 그룹(Security Group) 통합 설정 (중요)

EKS 환경에서 통신이 되려면 포트를 열어주는 "방화벽 규칙(Security Group)"이 가장 중요합니다.

### 보안 그룹 구성도

| 보안 그룹 이름 | 용도 | 인바운드 규칙 (누구를 들여보낼까?) |
|----------------|------|-----------------------------------|
| **EKS-Cluster-SG** | EKS 제어 | eksctl이 자동 생성 (건드리지 않음) |
| **Worker-Node-SG** | 실제 서버들 | - **Self**: 자기들끼리 모든 통신 허용<br>- **EKS-Cluster**: 443 (제어 신호)<br>- **NLB IP**: 30000-32767 (로드밸런서와 통신) |
| **RDS-SG** | 데이터베이스 | - **Worker-Node-SG**: 3306 (필수)<br>- ※ 만약 로컬에서 DB 접속하고 싶다면 `내 IP` / `0.0.0.0/0` 추가 |
| **LoadBalancer-SG** | 외부 접속 | - **Anywhere (0.0.0.0/0)**: 80 (HTTP) |

> **💡 핵심 체크 포인트**
> 1. **RDS 보안 그룹**에 **Worker Node 보안 그룹 ID**가 인바운드 3306으로 등록되어 있어야 합니다. (이게 없으면 파드에서 DB 연결 실패)
> 2. **Worker Node SG**는 로드밸런서의 상태 검사(Health Check)를 통과하기 위해 노드 포트(30000번대)를 열어두어야 합니다. (자동 설정되지만 문제 시 확인)

---

## 📦 5단계: ECR 리포지토리 생성 및 이미지 Push

### ECR 리포지토리 생성

```powershell
# 리전 설정
$REGION = "ap-northeast-2"

# 각 서비스별 리포지토리 생성
aws ecr create-repository --repository-name metacoding/gateway --region $REGION
aws ecr create-repository --repository-name metacoding/order --region $REGION
aws ecr create-repository --repository-name metacoding/product --region $REGION
aws ecr create-repository --repository-name metacoding/user --region $REGION
aws ecr create-repository --repository-name metacoding/delivery --region $REGION
aws ecr create-repository --repository-name metacoding/orchestrator --region $REGION
aws ecr create-repository --repository-name metacoding/frontend --region $REGION
```

### ECR 로그인

```powershell
# 계정 ID 확인
$ACCOUNT_ID = (aws sts get-caller-identity --query Account --output text)
$ECR_URI = "$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com"

# ECR 로그인
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $ECR_URI
```

### Docker 이미지 빌드

```powershell
# 프로젝트 루트 디렉토리에서 실행
docker build -t metacoding/gateway:1 ./api-gateway
docker build -t metacoding/order:1 ./order
docker build -t metacoding/product:1 ./product
docker build -t metacoding/user:1 ./user
docker build -t metacoding/delivery:1 ./delivery
docker build -t metacoding/orchestrator:1 ./orchestrator
docker build -t metacoding/frontend:1 ./frontend
```

### 이미지 태그 및 Push

```powershell
# 이미지 태그
docker tag metacoding/gateway:1 $ECR_URI/metacoding/gateway:1
docker tag metacoding/order:1 $ECR_URI/metacoding/order:1
docker tag metacoding/product:1 $ECR_URI/metacoding/product:1
docker tag metacoding/user:1 $ECR_URI/metacoding/user:1
docker tag metacoding/delivery:1 $ECR_URI/metacoding/delivery:1
docker tag metacoding/orchestrator:1 $ECR_URI/metacoding/orchestrator:1
docker tag metacoding/frontend:1 $ECR_URI/metacoding/frontend:1

# 이미지 Push
docker push $ECR_URI/metacoding/gateway:1
docker push $ECR_URI/metacoding/order:1
docker push $ECR_URI/metacoding/product:1
docker push $ECR_URI/metacoding/user:1
docker push $ECR_URI/metacoding/delivery:1
docker push $ECR_URI/metacoding/orchestrator:1
docker push $ECR_URI/metacoding/frontend:1
```

---

## ⚙️ 6단계: K8s 매니페스트 수정

### 6-1. ConfigMap 수정 (RDS 연결)

각 서비스의 ConfigMap에서 DB URL을 RDS 엔드포인트로 변경:

**`k8s/order/order-configmap.yml`** (예시):
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: order-configmap
  namespace: metacoding
data:
  DB_URL: "jdbc:mysql://metacofing-msa.cfcouus4ix90.ap-northeast-2.rds.amazonaws.com:3306/metacoding_msa?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
  DB_DRIVER: "com.mysql.cj.jdbc.Driver"
  DDL_AUTO: "validate"
  SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka-service:9092"
```

> **수정 필요한 파일들**: 
> - `k8s/order/order-configmap.yml`
> - `k8s/product/product-configmap.yml`
> - `k8s/user/user-configmap.yml`
> - `k8s/delivery/delivery-configmap.yml`

### 6-2. Secret 수정 (RDS 자격증명)

**`k8s/order/order-secret.yml`** (예시):
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: order-secret
  namespace: metacoding
type: Opaque
stringData:
  DB_USERNAME: "admin"
  DB_PASSWORD: "<RDS_PASSWORD>"
```

### 6-3. Deployment 이미지 경로 수정

각 Deployment의 이미지를 ECR 경로로 변경:

**수정 전**:
```yaml
image: metacoding/order:1
```

**수정 후**:
```yaml
image: <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/metacoding/order:1
```

> **수정 필요한 파일들**: 모든 `*-deploy.yml` 파일

---

## 🚢 7단계: Kubernetes 리소스 배포

### 배포 순서

```powershell
# 1. 네임스페이스 생성
kubectl create ns metacoding

# 2. Kafka 먼저 배포 (다른 서비스들이 의존)
kubectl apply -f k8s/kafka/

# 3. Kafka 준비 대기
kubectl wait --for=condition=ready pod -l app=kafka -n metacoding --timeout=180s

# 4. 나머지 서비스 배포
kubectl apply -f k8s/gateway/
kubectl apply -f k8s/order/
kubectl apply -f k8s/product/
kubectl apply -f k8s/user/
kubectl apply -f k8s/delivery/
kubectl apply -f k8s/orchestrator/
kubectl apply -f k8s/frontend/
```

### 배포 상태 확인

```powershell
# 모든 파드 상태 확인
kubectl get pods -n metacoding

# 모든 서비스 확인
kubectl get svc -n metacoding

# 파드 로그 확인 (문제 발생 시)
kubectl logs -n metacoding <pod-name>

# 파드 상세 정보 (이벤트 확인)
kubectl describe pod -n metacoding <pod-name>
```

---

---

## 📊 8단계: 모니터링 배포 (Prometheus + Grafana)

### 모니터링 구성 요소

| 구성 요소 | 포트 | 설명 |
|-----------|------|------|
| **Prometheus** | 9090 | 메트릭 수집/저장 |
| **Grafana** | 3000 | 시각화 대시보드 |

### 모니터링 배포

```powershell
# 1. 모니터링 네임스페이스 생성
kubectl apply -f k8s/monitoring/namespace.yml

# 2. Prometheus 배포
kubectl apply -f k8s/monitoring/prometheus/

# 3. Prometheus 준비 대기
kubectl wait --for=condition=ready pod -l app=prometheus -n monitoring --timeout=120s

# 4. Grafana 배포
kubectl apply -f k8s/monitoring/grafana/

# 5. Grafana 준비 대기
kubectl wait --for=condition=ready pod -l app=grafana -n monitoring --timeout=120s

# 상태 확인
kubectl get pods -n monitoring
```

### 모니터링 접속

```powershell
# Prometheus UI 접속 (http://localhost:9090)
kubectl port-forward svc/prometheus -n monitoring 9090:9090

# Grafana UI 접속 (http://localhost:3000)
kubectl port-forward svc/grafana -n monitoring 3000:3000
```

### Grafana 로그인

- **ID**: `admin`
- **PW**: `admin`

> **💡 Tip**: Grafana에서 Prometheus 데이터소스는 자동 등록되어 있습니다.

---

## 🌐 9단계: 외부 접속 확인 (NLB)

**Frontend 서비스**는 `Network Load Balancer (NLB)` 타입으로 배포되어 외부에서 직접 접속이 가능합니다.
(반면, **Gateway 서비스**는 `ClusterIP` 타입으로 설정되어 내부 통신만 담당합니다.)

```powershell
# 1. 외부 IP (DNS Name) 확인
kubectl get svc frontend-service -n metacoding -w

# 출력 예시:
# NAME               TYPE           CLUSTER-IP      EXTERNAL-IP                                                         PORT(S)        AGE
# frontend-service   LoadBalancer   10.100.x.x      k8s-metacoding-frontend-xxxxxxxx.elb.ap-northeast-2.amazonaws.com   80:3xxxx/TCP   2m
```

> **✅ 접속 방법**:
> 위 명령어 결과의 `EXTERNAL-IP` 주소를 복사하여 브라우저 주소창에 입력하세요.
> (NLB 생성 및 DNS 전파에 약 3~5분 정도 소요될 수 있습니다)

---
---

## 🧪 10단계: 실전 통합 테스트 (Web + Postman)

웹 화면(사용자)과 포스트맨(배달 기사)을 오가며 전체 흐름을 테스트합니다.

> **⚠️ 중요: 프론트엔드 최신화 (EKS 배포)**
> 방금 `index.html`을 수정했으므로, **반드시 이미지를 재배포해야** 새로운 로그인 화면이 보입니다!
>
> ```powershell
> # 1. 이미지 재빌드 & 푸시
> # (ECR_URI는 5단계에서 설정한 변수 사용)
> docker build -t metacoding/frontend:1 ./frontend
> docker push $ECR_URI/metacoding/frontend:1
> 
> # 2. 파드 재시작 (새 이미지 받아오기)
> kubectl rollout restart deployment frontend -n metacoding
> ```

### 1단계: 웹에서 로그인 및 주문 (사용자)
1. **접속**: 웹 브라우저를 켜고, 9단계에서 확인한 **로드밸런서 DNS 주소(EXTERNAL-IP)**로 접속합니다.
   - 예시: `http://k8s-metacoding-frontend-xxxx.elb.ap-northeast-2.amazonaws.com` (localhost 아님!)
2. **로그인 섹션**:
   - Username: `ssar`
   - Password: `1234`
   - **[로그인]** 버튼 클릭 → "✅ 로그인 성공!" 확인 (토큰 자동 저장됨)
3. **주문 섹션**:
   - **[주문 요청하기]** 버튼 클릭
   - "✅ 주문 성공! (Order ID: N)" 메시지 확인
   - 잠시 후 "🚀 배달 상태 대기중..." 및 알림 확인

### 2단계: 포스트맨에서 배달 확인 (배달 기사)
1. **Postman** 열기
2. **배달 목록 조회**:
   - Method: `GET`
   - URL: `http://<로드밸런서-주소>/api/deliveries`
     - **주의**: 주소창의 로드밸런서 URL을 복사해서 넣으세요.
   - Headers: `Authorization: Bearer <토큰>`
     - 토큰은 어디서? 브라우저 개발자 도구(F12) -> Network -> 로그인 요청 헤더에서 복사!
   - Response Body에서 방금 주문한 건의 `id` (deliveryId) 복사.
3. **배달 완료 처리**:
   - Method: `PUT`
   - URL: `http://<로드밸런서-주소>/api/deliveries/{deliveryId}/complete`
     - 예: `http://k8s-metaco.../api/deliveries/1/complete`
   - Body: 없음
   - Send 클릭 -> Status: `200 OK` 확인

### 3단계: 웹에서 알림 확인 (피드백)
1. 다시 웹 브라우저 화면을 확인합니다.
2. 화면 하단에 **"🔔 [알림] 배달이 완료되었습니다"** 메시지가 자동으로 떴다면 성공! 🎉

---

## 🧹 11단계: 리소스 정리 (실습 후 필수!)

```powershell
# 1. K8s 리소스 삭제
kubectl delete namespace metacoding
kubectl delete namespace monitoring

# 2. EKS 클러스터 삭제 (eksctl 사용 시)
eksctl delete cluster --name metacoding --region ap-northeast-2

# 3. ECR 이미지 삭제 (AWS 콘솔 또는 CLI)
aws ecr delete-repository --repository-name metacoding/gateway --force --region ap-northeast-2
aws ecr delete-repository --repository-name metacoding/order --force --region ap-northeast-2
aws ecr delete-repository --repository-name metacoding/product --force --region ap-northeast-2
aws ecr delete-repository --repository-name metacoding/user --force --region ap-northeast-2
aws ecr delete-repository --repository-name metacoding/delivery --force --region ap-northeast-2
aws ecr delete-repository --repository-name metacoding/orchestrator --force --region ap-northeast-2
aws ecr delete-repository --repository-name metacoding/frontend --force --region ap-northeast-2

# 4. RDS 삭제 (AWS 콘솔에서)
# RDS 콘솔 → 데이터베이스 → 삭제
```

---

## 🔧 트러블슈팅

| 문제 | 원인 | 해결 방법 |
|------|------|-----------|
| `ImagePullBackOff` | ECR 이미지 경로 오류 | 이미지 경로 확인, ECR 로그인 재시도 |
| `CrashLoopBackOff` | 애플리케이션 오류 | `kubectl logs` 로 로그 확인 |
| DB 연결 실패 | RDS 보안그룹 | 인바운드 3306 허용 확인 |
| Kafka 연결 실패 | Kafka 미준비 | Kafka Pod Ready 상태 확인 |
| LoadBalancer Pending | 권한 부족 | IAM 역할에 ELB 권한 추가 |

---

## 📂 프로젝트 구조

```
ex04/
├── QUICK-START.md              # 이 가이드
├── EKS-ARCHITECTURE.md         # 아키텍처 문서
├── EKS-REQUEST-FLOW.md         # 요청 흐름 문서
├── SCENARIO-LOGIN-ORDER-DELIVERY.md # 시나리오 문서
├── README.md                   # 기존 minikube 가이드
├── api-gateway/                # API Gateway 서비스
├── order/                      # 주문 서비스
├── product/                    # 상품 서비스
├── user/                       # 사용자 서비스
├── delivery/                   # 배송 서비스
├── orchestrator/               # Saga Orchestrator
├── frontend/                   # 프론트엔드 (Nginx)
├── db/                         # DB 초기화 스크립트
└── k8s/                        # Kubernetes 매니페스트
```

---

## 🔗 참고 문서

- [EKS-ARCHITECTURE.md](./EKS-ARCHITECTURE.md) - 전체 아키텍처 다이어그램
- [EKS-REQUEST-FLOW.md](./EKS-REQUEST-FLOW.md) - 요청 흐름 상세
- [SCENARIO-LOGIN-ORDER-DELIVERY.md](./SCENARIO-LOGIN-ORDER-DELIVERY.md) - 비즈니스 시나리오
- [README.md](./README.md) - 로컬 minikube 가이드
