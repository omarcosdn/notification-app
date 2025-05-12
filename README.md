# Serviço de Notificações via WebSocket

Este é um serviço de notificações em tempo real que utiliza WebSocket para entregar mensagens aos clientes. O serviço é construído com Spring Boot e utiliza Redis para gerenciamento de sessões, PostgreSQL para persistência de mensagens e RabbitMQ para processamento assíncrono.

## 🚀 Funcionalidades

- Conexão WebSocket persistente para entrega de mensagens em tempo real
- Suporte a múltiplos tenants (clientes)
- Persistência de mensagens não entregues
- Confirmação de recebimento (ACK) de mensagens
- Monitoramento de saúde das conexões via ping/pong
- Integração com RabbitMQ para processamento assíncrono

## 🛠️ Tecnologias Utilizadas

- Java 17
- Spring Boot 3.4.5
- WebSocket
- Redis
- PostgreSQL
- RabbitMQ
- Gradle

## 📋 Pré-requisitos

- Docker e Docker Compose
- Java 17 ou superior
- Gradle

## 🔧 Configuração do Ambiente

1. Clone o repositório:
```bash
git clone https://github.com/omarcosdn/notification-services.git
cd notification-services
```

2. Inicie os serviços de infraestrutura:
```bash
docker-compose up -d
```

3. Configure as variáveis de ambiente (opcional):
```bash
# application.yml já contém valores padrão para desenvolvimento
SERVER_PORT=8080
NOTIFICATION_APP_DATABASE_HOST=jdbc:postgresql://localhost:5432/notification_app
NOTIFICATION_APP_DATABASE_USER=postgres
NOTIFICATION_APP_DATABASE_PASSWORD=postgres
```

## 🚀 Executando o Projeto

1. Compile o projeto:
```bash
./gradlew build
```

2. Execute a aplicação:
```bash
./gradlew bootRun
```

## 📡 Conectando via WebSocket

### Endpoint
```
ws://localhost:8080/api/notification-services/messages
```

### Headers Necessários
- `Tenant-Id`: UUID do tenant (cliente)

### Exemplo de Cliente WebSocket (Node.js)
```javascript
const WebSocket = require('ws');

const tenantId = '8ebacf36-ca70-4b57-95cb-188d370fa873';

const ws = new WebSocket('ws://localhost:8080/api/notification-services/messages', {
    headers: {
        'Tenant-Id': tenantId
    }
});

ws.on('open', () => {
    console.log('✅ Connected to WebSocket server');
});

ws.on('message', (data) => {
    const message = data.toString();
    console.log('📩 Received text message:', message);
});

ws.on('close', () => {
    console.log('🔌 Connection closed');
});

ws.on('error', (err) => {
    console.error('❌ Error:', err);
});
```

## 📨 Formato das Mensagens

### Mensagem Recebida
```json
{
    "type": "message",
    "tenantId": "uuid-do-tenant",
    "messageId": "uuid-da-mensagem",
    "content": "conteúdo da mensagem"
}
```

### Confirmação de Recebimento (ACK)
```json
{
    "type": "ack",
    "messageId": "uuid-da-mensagem"
}
```

## 🔄 Fluxo de Funcionamento

1. **Conexão Inicial**:
   - Cliente se conecta ao WebSocket com um UUID de tenant
   - Servidor valida o tenant e registra a conexão
   - Mensagens não entregues são enviadas automaticamente

2. **Entrega de Mensagens**:
   - Mensagens são enviadas via RabbitMQ
   - Servidor verifica se o tenant está conectado
   - Se conectado, envia via WebSocket
   - Se desconectado, persiste para entrega posterior

3. **Confirmação de Recebimento**:
   - Cliente envia ACK após receber a mensagem
   - Servidor marca a mensagem como entregue

4. **Monitoramento de Conexão**:
   - Servidor envia PING a cada 30 segundos
   - Cliente deve responder com PONG
   - Conexão é encerrada após 60 segundos sem resposta

## 🔍 Endpoints de Monitoramento

### Health Check
```
GET /api/notification-services/health
```

Resposta:
```json
{
    "status": 200,
    "message": "everything is fine"
}
```

## 📊 Infraestrutura

- **Redis**: Gerenciamento de sessões WebSocket
- **PostgreSQL**: Persistência de mensagens
- **RabbitMQ**: Processamento assíncrono de mensagens
  - Exchange: `com.github.omarcosdn.exchange.notification.pubsub`
  - Queue: `com.github.omarcosdn.queue.notification.v1`

### Exemplo de Payloads RabbitMQ

#### Envio de Notificação
```json
{
   "tenantId": "8ebacf36-ca70-4b57-95cb-188d370fa873",
   "content": "{\"title\":\"New Message\",\"body\":\"You have received a new message\",\"priority\":\"HIGH\",\"metadata\":{\"source\":\"SYSTEM\",\"category\":\"ALERT\"}}"
}
```

### Estrutura do Payload

| Campo | Tipo | Descrição |
|-------|------|-----------|
| tenantId | UUID | Identificador único do tenant |
| content | Object | Conteúdo da mensagem |
| content.title | String | Título da mensagem |
| content.body | String | Corpo da mensagem |
| content.priority | String | Prioridade (HIGH, NORMAL, LOW) |
| content.metadata | Object | Metadados adicionais |

## 🔒 Segurança

### Autenticação Keycloak
- Autenticação via Keycloak usando client credentials flow
- Token JWT obrigatório para todas as APIs
- Token deve ser enviado no header `Authorization: Bearer <token>`
- Validação automática de token expirado
- Suporte a refresh token

### Headers Obrigatórios
- `Authorization`: Bearer token JWT
- `Tenant-Id`: UUID do tenant (cliente)

### Obtenção do Token JWT
```bash
curl --location 'http://localhost:8080/realms/notification-app/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'client_id=websocket-client' \
--data-urlencode 'client_secret=KAdfQnSFRyCWqqiKmkDKUvQ2RiaJqXlO'
```

Resposta:
```json
{
    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ...",
    "expires_in": 300,
    "refresh_expires_in": 1800,
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ...",
    "token_type": "bearer",
    "not-before-policy": 0,
    "session_state": "c2b4c3a1-2d3e-4f5g-6h7i-8j9k0l1m2n3o",
    "scope": "profile email"
}
```

### Exemplo de Cliente WebSocket (Node.js)
```javascript
const WebSocket = require('ws');

const tenantId = '8ebacf36-ca70-4b57-95cb-188d370fa873';
const jwtToken = 'seu-jwt-token-aqui';

const ws = new WebSocket('ws://localhost:8080/api/notification-services/messages', {
    headers: {
        'Tenant-Id': tenantId,
        'Authorization': `Bearer ${jwtToken}`
    }
});

ws.on('open', () => {
    console.log('✅ Connected to WebSocket server');
});

ws.on('message', (data) => {
    const message = data.toString();
    console.log('📩 Received text message:', message);
});

ws.on('close', () => {
    console.log('🔌 Connection closed');
});

ws.on('error', (err) => {
    console.error('❌ Error:', err);
});
```

### Refresh Token
```bash
curl --location 'http://localhost:8080/realms/notification-app/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=refresh_token' \
--data-urlencode 'refresh_token=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ...' \
--data-urlencode 'client_id=websocket-client' \
--data-urlencode 'client_secret=KAdfQnSFRyCWqqiKmkDKUvQ2RiaJqXlO'
```

### Configuração do Keycloak
- Realm: `notification-app`
- Client ID: `websocket-client`
- Client Secret: Configurado no Keycloak
- Grant Types: `client_credentials`
- Access Token Lifespan: 5 minutos
- Refresh Token Lifespan: 30 minutos

## 🐛 Depuração

Para testar o serviço, você pode:

1. Usar o cliente WebSocket de exemplo em `.test/ws-client.js`
2. Enviar mensagens via RabbitMQ Management Console (http://localhost:15672)
3. Monitorar logs da aplicação para debug

## 📝 Notas de Implementação

- O serviço utiliza Spring WebSocket para gerenciamento de conexões
- Redis é usado para controle de sessões com TTL de 120 segundos
- Mensagens não entregues são persistidas no PostgreSQL
- O sistema suporta reconexão automática e entrega de mensagens pendentes 