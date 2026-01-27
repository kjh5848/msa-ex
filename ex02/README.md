#### 1 미니큐브 시작
```bash
minikube start
```
#### 2 이미지 빌드

```bash
# 프로젝트 루트에서 실행
minikube image build -t metacoding/db:1 ./db
minikube image build -t metacoding/gateway:1 ./api-gateway
minikube image build -t metacoding/order:1 ./order
minikube image build -t metacoding/product:1 ./product
minikube image build -t metacoding/user:1 ./user
minikube image build -t metacoding/delivery:1 ./delivery
```

#### 3 네임스페이스 생성

```bash
kubectl create namespace metacoding
```

#### 4 Kubernetes 리소스 배포

```bash
kubectl apply -f k8s/db
kubectl apply -f k8s/gateway
kubectl apply -f k8s/order
kubectl apply -f k8s/product
kubectl apply -f k8s/user
kubectl apply -f k8s/delivery
```

### 5. 서비스 접근

```bash
minikube service gateway-service -n metacoding --url
```

### 6. 상태 확인

```bash
# Pod 상태 확인
kubectl get pods -n metacoding

# Service 확인
kubectl get services -n metacoding

# Deployment 상태 확인
kubectl get deployments -n metacoding

# Pod 로그 확인
kubectl logs -n metacoding <pod-name>
```

### 7. 이미지 리스타트

```bash
# Deployment 재시작
kubectl rollout restart deployment/gateway-deployment -n metacoding
kubectl rollout restart deployment/order-deployment -n metacoding
kubectl rollout restart deployment/product-deployment -n metacoding
kubectl rollout restart deployment/user-deployment -n metacoding
kubectl rollout restart deployment/delivery-deployment -n metacoding

