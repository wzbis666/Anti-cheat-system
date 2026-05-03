const WebSocket = require('ws');

const WS_URL = 'ws://localhost:8080/ws/cheats';

console.log('正在连接WebSocket:', WS_URL);

const ws = new WebSocket(WS_URL);

ws.on('open', () => {
    console.log('✅ WebSocket连接成功!');
    
    // 模拟发送作弊数据
    const cheatData = {
        playerName: 'TestPlayer',
        uuid: 'test-uuid-' + Date.now(),
        cheatType: '飞行作弊',
        severity: 3,
        details: '测试作弊检测'
    };
    
    console.log('发送测试数据:', JSON.stringify(cheatData, null, 2));
    ws.send(JSON.stringify(cheatData));
});

ws.on('message', (data) => {
    console.log('收到消息:', data.toString());
});

ws.on('error', (error) => {
    console.error('❌ WebSocket错误:', error.message);
});

ws.on('close', () => {
    console.log('WebSocket连接已关闭');
});

// 30秒后关闭
setTimeout(() => {
    console.log('测试完成，关闭连接');
    ws.close();
    process.exit(0);
}, 30000);
