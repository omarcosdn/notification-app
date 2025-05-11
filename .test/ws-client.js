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
