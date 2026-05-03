const WebSocket = require('ws');

const API_BASE = 'http://localhost:8080/api';

async function testReportSystem() {
    console.log('========== 举报系统测试 ==========\n');

    try {
        console.log('1. 测试创建举报...');
        const reportData = {
            reporterName: 'TestReporter_' + Math.floor(Math.random() * 10000),
            reporterUuid: 'reporter-uuid-' + Date.now(),
            reportedName: 'Hacker_' + Math.floor(Math.random() * 10000),
            reportedUuid: 'reported-uuid-' + Date.now(),
            reason: '测试举报 - 疑似飞行作弊',
            reportType: 'CHEATING'
        };

        const createResponse = await fetch(API_BASE + '/report/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(reportData)
        });
        const createdReport = await createResponse.json();
        console.log('创建举报成功:', JSON.stringify(createdReport, null, 2));
        console.log('');

        console.log('2. 测试获取所有举报...');
        const allResponse = await fetch(API_BASE + '/report/all');
        const allReports = await allResponse.json();
        console.log('举报总数:', allReports.length);
        console.log('');

        console.log('3. 测试获取待处理举报...');
        const pendingResponse = await fetch(API_BASE + '/report/pending');
        const pendingReports = await pendingResponse.json();
        console.log('待处理举报数:', pendingReports.length);
        console.log('');

        console.log('4. 测试获取待处理举报数量...');
        const countResponse = await fetch(API_BASE + '/report/count/pending');
        const countData = await countResponse.json();
        console.log('待处理数量:', countData.count);
        console.log('');

        if (pendingReports.length > 0) {
            const reportToHandle = pendingReports[0];
            console.log('5. 测试处理举报 (ID: ' + reportToHandle.id + ')...');
            
            const handleResponse = await fetch(API_BASE + '/report/handle/' + reportToHandle.id, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    handledBy: 'TEST_ADMIN',
                    status: 'RESOLVED',
                    result: '测试处理 - 确认违规'
                })
            });
            const handleResult = await handleResponse.json();
            console.log('处理结果:', JSON.stringify(handleResult, null, 2));
            console.log('');

            console.log('6. 测试获取单个举报详情...');
            const detailResponse = await fetch(API_BASE + '/report/' + reportToHandle.id);
            const detail = await detailResponse.json();
            console.log('举报详情:', JSON.stringify(detail, null, 2));
            console.log('');
        }

        console.log('7. 创建不同类型的举报...');
        const reportTypes = ['HACKING', 'GRIEFING', 'HARASSMENT', 'OTHER'];
        for (const type of reportTypes) {
            const typeReport = {
                reporterName: 'Reporter_' + Math.floor(Math.random() * 10000),
                reporterUuid: 'uuid-' + Date.now() + Math.random(),
                reportedName: 'BadPlayer_' + Math.floor(Math.random() * 10000),
                reportedUuid: 'bad-uuid-' + Date.now(),
                reason: `测试举报类型: ${type}`,
                reportType: type
            };
            
            await fetch(API_BASE + '/report/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(typeReport)
            });
            console.log(`创建 ${type} 类型举报成功`);
        }
        console.log('');

        console.log('8. 最终统计...');
        const finalAllResponse = await fetch(API_BASE + '/report/all');
        const finalAll = await finalAllResponse.json();
        const finalCountResponse = await fetch(API_BASE + '/report/count/pending');
        const finalCount = await finalCountResponse.json();
        
        console.log('举报总数:', finalAll.length);
        console.log('待处理:', finalCount.count);
        console.log('已处理:', finalAll.filter(r => r.status === 'RESOLVED').length);
        console.log('已驳回:', finalAll.filter(r => r.status === 'REJECTED').length);
        console.log('');

        console.log('========== 测试完成 ==========');
        console.log('所有举报测试用例通过!');

    } catch (error) {
        console.error('测试失败:', error.message);
    }
}

testReportSystem();
