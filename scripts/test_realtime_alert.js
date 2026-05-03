const WebSocket = require('ws');

const WS_URL = 'ws://localhost:8080/ws/cheats';

const ws = new WebSocket(WS_URL);

ws.on('open', () => {
    console.log('WebSocket连接已建立');
    
    const cheatData = {
        playerName: 'TestPlayer_RT_' + Math.floor(Math.random() * 10000),
        uuid: 'test-uuid-realtime-' + Date.now(),
        cheatType: '飞行作弊',
        severity: 3,
        details: '实时警报测试 - ' + new Date().toLocaleString()
    };
    
    console.log('发送作弊数据:', JSON.stringify(cheatData, null, 2));
    ws.send(JSON.stringify(cheatData));
    
    setTimeout(() => {
        ws.close();
        console.log('测试完成，连接已关闭');
    }, 2000);
});

ws.on('message', (data) => {
    console.log('收到服务器消息:', data.toString());
});

ws.on('error', (error) => {
    console.error('WebSocket错误:', error.message);
});

ws.on('close', () => {
    console.log('WebSocket连接已关闭');
});
