export default {
  common: {
    loading: '加载中...', noData: '暂无数据', confirm: '确认', cancel: '取消', delete: '删除', edit: '编辑', save: '保存', search: '搜索', actions: '操作', status: '状态', time: '时间', details: '详情', view: '查看', close: '关闭', add: '添加', enable: '启用', disable: '禁用', all: '全部', success: '操作成功', error: '操作失败', warning: '警告', info: '信息', logout: '退出登录', uuid: 'UUID', csv: '导出CSV', view: '查看', delete: '删除', scanning: '扫描中', reason: '原因', player: '玩家', entitiesIndexed: '条记录'
  },
  nav: {
    dashboard: '仪表盘', players: '玩家管理', cheats: '作弊记录', reports: '举报管理', punishments: '封禁管理', whitelist: '白名单', settings: '系统设置', appeals: '申诉管理', audit: '审计日志', main: '主要', management: '管理'
  },
  auth: {
    login: '登录', logout: '退出登录', admin: '管理员', user: '用户', username: '用户名', password: '密码', confirmPassword: '确认密码', nickname: '昵称', email: '邮箱', register: '注册', noAccount: '没有账号？立即注册', defaultAccount: '默认账号: admin / (生产环境请修改)', forgotPassword: '忘记密码？', forgotPasswordTitle: '重置密码', forgotPasswordDesc: '请输入您的用户名，系统将为您生成新密码', resetPassword: '重置密码', resetting: '重置中...', resetSuccess: '密码已重置！', newPassword: '新密码', copyAndLogin: '复制后去登录', signingIn: '登录中...', registering: '注册中...', loginSuccess: '登录成功！', loginFailed: '登录失败，请检查用户名和密码', loginError: '登录失败，请检查网络连接', usernameRequired: '请输入用户名和密码', usernameMinLength: '用户名至少3个字符', passwordMinLength: '密码至少6个字符', passwordMismatch: '两次密码不一致', registerSuccess: '注册成功！请登录', usernamePlaceholder: '请输入用户名', passwordPlaceholder: '请输入密码', confirmPasswordPlaceholder: '确认密码', nicknamePlaceholder: '3-20个字符', passwordMinPlaceholder: '至少6个字符', entering: '正在穿过地狱门...'
  },
  dashboard: {
    title: '仪表盘', subtitle: '实时监控系统状态', totalPlayers: '总玩家数', cheatDetections: '作弊检测', highRiskPlayers: '高风险玩家', activeBans: '封禁中', pendingReports: '待处理举报', cheatTypesChart: '作弊类型分布', trendChart: '24小时检测趋势', riskChart: '风险等级分布', realtimeAlerts: '实时警报', connected: '已连接', disconnected: '未连接', refresh: '刷新', noAlerts: '暂无实时警报', severity: '严重程度', player: '玩家', cheatType: '作弊类型', serverStatus: '服务器状态', uptime: '运行时长', notifications: '通知中心', clearAll: '全部已读', noNotifications: '暂无通知', searchPlaceholder: '搜索玩家、记录...', quickNav: '快速导航', fullscreen: '全屏监控', toggleTheme: '切换主题',
    threatLevel: '威胁等级', events: '事件', realTimeMonitor: '实时监控台', live: '在线', offline: '离线', awaitingData: '等待数据流...', ai: 'AI', aiAnalysis: 'AI 分析', analysis: '分析', verdict: '判定', action: '建议操作', reasoning: '推理过程',
    severityLevel: '等级', severityCritical: '严重', severityHigh: '高', severityMedium: '中', severityLow: '低'
  },
  players: {
    title: '玩家管理', subtitle: '管理所有玩家信息', searchPlaceholder: '搜索玩家...', uuid: 'UUID', risk: '风险', kicks: '踢出次数', lastSeen: '最后在线', playerDetails: '玩家详情', basicInfo: '基本信息', cheatLogs: '作弊记录', punishments: '封禁记录', riskScore: '风险评分', kickCount: '踢出次数', banStatus: '封禁状态', banned: '已封禁', active: '正常', noCheatRecords: '暂无作弊记录', noPunishmentRecords: '暂无封禁记录', banPlayer: '封禁玩家', unban: '解封', confirmDelete: '确定要删除该玩家吗？', low: '低', medium: '中', high: '高'
  },
  cheats: {
    title: '作弊记录', subtitle: '所有作弊行为记录', allTypes: '全部类型', allSeverity: '全部严重度', severityLow: '低', severityMedium: '中', severityHigh: '高', severityCritical: '严重', cheatType: '作弊类型', severity: '严重度', searchPlaceholder: '搜索玩家或类型...', distributionChart: '作弊类型分布图', detailTitle: '作弊详情', confirmDelete: '确定要删除该记录吗？', flying: '飞行作弊', speed: '速度作弊', autoClick: '自动点击', killAura: '杀戮光环', detectedAt: '检测时间', noRecords: '暂无作弊记录', records: '条记录', first: '首页', last: '末页', prev: '上一页', next: '下一页', scanning: '扫描中', noDetections: '未检测到', view: '查看', delete: '删除'
  },
  reports: {
    title: '举报管理', subtitle: '处理玩家举报', pending: '待处理', allStatus: '全部状态', resolved: '已处理', rejected: '已驳回', reporter: '举报人', reported: '被举报人', type: '类型', reason: '原因', noReports: '暂无举报记录', handleReport: '处理举报', reportDetails: '举报详情', result: '处理结果', confirmViolation: '确认违规', rejectReport: '驳回举报', notes: '处理说明', banThisPlayer: '封禁该玩家', banReason: '封禁原因', handledBy: '处理人', handledAt: '处理时间', cheating: '作弊行为', hacking: '黑客行为', griefing: '破坏行为', harassment: '骚扰行为', other: '其他'
  },
  punishments: {
    title: '封禁管理', subtitle: '管理封禁记录', addBan: '添加封禁', playerName: '玩家名称', playerUUID: '玩家UUID', banType: '封禁类型', permanent: '永久封禁', temporary: '临时封禁', duration: '持续时间', bannedAt: '封禁时间', noRecords: '暂无封禁记录', confirmBan: '确认封禁', banning: '封禁中...', confirmUnban: '确认解封', unban: '解封', expired: '已过期', hour: '小时', hours: '小时', day: '天', days: '天'
  },
  whitelist: {
    title: '白名单', subtitle: '可信玩家列表', addToWhitelist: '添加白名单', addedBy: '添加者', addedAt: '添加时间', noEntries: '暂无白名单记录', whitelistReason: '添加白名单的原因', confirmDisable: '禁用白名单', confirmEnable: '启用白名单'
  },
  settings: {
    title: '系统设置', subtitle: '配置系统参数', save: '保存设置', reset: '重置设置', syncToPlugin: '同步到插件', syncTitle: '同步配置', syncDesc: '将设置同步到游戏服务器插件',
    saveSuccess: '设置已保存', syncSuccess: '同步成功', resetSuccess: '已重置为默认设置',

    detectionTitle: '检测设置', detectionDesc: '配置作弊检测参数',
    enableFlyingDetection: '飞行作弊检测', enableFlyingDetectionDesc: '启用飞行作弊检测功能',
    enableSpeedDetection: '速度作弊检测', enableSpeedDetectionDesc: '启用速度作弊检测功能',
    enableKillAuraDetection: '杀戮光环检测', enableKillAuraDetectionDesc: '启用杀戮光环检测功能',
    enableAutoClickDetection: '自动点击检测', enableAutoClickDetectionDesc: '启用自动点击作弊检测',
    detectionThreshold: '检测阈值', detectionThresholdDesc: '触发警告的检测次数阈值',
    alertCooldown: '警告冷却时间', alertCooldownDesc: '相同玩家两次警告的最小间隔(秒)',

    punishmentTitle: '惩罚设置', punishmentDesc: '配置自动处罚行为',
    autoBanEnabled: '自动封禁', autoBanEnabledDesc: '检测到作弊时自动封禁玩家',
    banThreshold: '封禁阈值', banThresholdDesc: '触发封禁所需的作弊检测次数',
    defaultBanDuration: '默认封禁时长', defaultBanDurationDesc: '自动封禁的默认时长',
    enableTempBan: '临时封禁', enableTempBanDesc: '首次违规使用临时封禁',

    notificationTitle: '通知设置', notificationDesc: '配置警报和通知偏好',
    enableSoundAlert: '声音警报', enableSoundAlertDesc: '检测到作弊时播放声音警报',
    enableDesktopAlert: '桌面通知', enableDesktopAlertDesc: '检测到作弊时显示桌面通知',
    highRiskAlert: '高风险警告', highRiskAlertDesc: '高风险玩家触发额外警告',
    reportAlert: '举报通知', reportAlertDesc: '收到新举报时发送通知',

    systemTitle: '系统设置', systemDesc: '配置服务器和系统偏好',
    autoRefresh: '自动刷新', autoRefreshDesc: '自动刷新数据统计',
    refreshInterval: '刷新间隔', refreshIntervalDesc: '数据自动刷新的时间间隔(秒)',
    maxLogEntries: '最大日志数量', maxLogEntriesDesc: '保留的最大日志记录数',
    language: '界面语言', languageDesc: '系统界面显示语言',

    advancedTitle: '高级设置', advancedDesc: '高级配置选项',
    enableDebug: '调试模式', enableDebugDesc: '启用调试日志输出',
    logLevel: '日志级别', logLevelDesc: '设置日志输出级别',
    apiTimeout: 'API超时', apiTimeoutDesc: 'API请求超时时间(秒)',
    maxConnections: '最大连接数', maxConnectionsDesc: '允许的最大WebSocket连接数',

    '1小时': '1小时', '6小时': '6小时', '24小时': '24小时', '7天': '7天', '30天': '30天', '永久': '永久',
    '中文': '中文', 'English': 'English',
    'Debug': '调试', 'Info': '信息', 'Warn': '警告', 'Error': '错误'
  },
  profile: {
    title: '个人设置', subtitle: '账户设置', profile: '个人资料', password: '修改密码', currentPassword: '当前密码', newPassword: '新密码', confirmNewPassword: '确认新密码', changePassword: '修改密码', saveChanges: '保存修改', saving: '保存中...', changing: '修改中...', createdAt: '创建时间', lastLogin: '最后登录', superAdmin: '超级管理员', admin: '管理员'
  },
  header: {
    connected: '已连接', disconnected: '未连接'
  },
  appeals: {
    title: '申诉管理', pending: '待处理', approved: '已批准', rejected: '已驳回',
    approve: '批准申诉', reject: '驳回申诉', confirmApprove: '确定批准此申诉？将解除对应处罚',
    confirmReject: '确定驳回此申诉？', noAppeals: '暂无申诉', response: '管理员回复',
    reason: '> 原因', adminResponse: '管理员回复...',
    PENDING: '待处理', APPROVED: '已批准', REJECTED: '已驳回'
  },
  audit: {
    title: '审计日志', noRecords: '暂无审计记录',
    records: '条记录',
    BAN: '封禁', UNBAN: '解封', DELETE_PLAYER: '删除玩家', DELETE_CHEAT: '删除作弊记录',
    HANDLE_REPORT: '处理举报', WHITELIST_ADD: '添加白名单', WHITELIST_REMOVE: '移除白名单',
    SETTINGS_UPDATE: '更新设置', LOGIN: '登录', LOGOUT: '退出', REGISTER: '注册'
  },
  ai: {
    assistant: 'AI 助手', online: '在线', offline: '离线', welcome: '你好！我是反作弊AI助手，可以帮你分析玩家行为、处理举报、回答系统相关问题。', inputPlaceholder: '输入你的问题...', disabledPlaceholder: 'AI功能未启用，请在设置中配置API Key', analyze: 'AI 分析', analysisResult: 'AI 分析结果', verdict: '判定', confidence: '置信度', suggestedAction: '建议操作', reasoning: '推理过程', error: 'AI分析失败', networkError: '网络错误，请检查AI服务配置', clearChat: '清空对话', quickQ1: '最近有哪些高风险玩家？', quickQ2: '帮我分析今天的检测趋势', quickQ3: '飞行作弊的检测原理是什么？', dashboardAnalysis: 'AI 仪表盘分析', comprehensiveAnalysis: '综合分析', riskLevel: '风险等级', suggestions: '优化建议', analyzing: 'AI 正在分析...', clickToAnalyze: '点击上方标签开始AI分析', evaluateBan: 'AI 评估', banEvaluation: 'AI 封禁评估', noPlayerData: '无法获取玩家数据'
  }
}
