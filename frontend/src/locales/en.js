export default {
  common: {
    loading: 'Loading...', noData: 'No data', confirm: 'Confirm', cancel: 'Cancel', delete: 'Delete', edit: 'Edit', save: 'Save', search: 'Search', actions: 'Actions', status: 'Status', time: 'Time', details: 'Details', view: 'View', close: 'Close', add: 'Add', enable: 'Enable', disable: 'Disable', all: 'All', success: 'Success', error: 'Error', warning: 'Warning', info: 'Info', logout: 'Logout'
  },
  nav: {
    dashboard: 'Dashboard', players: 'Players', cheats: 'Cheat Logs', reports: 'Reports', punishments: 'Punishments', whitelist: 'Whitelist', settings: 'Settings', main: 'Main', management: 'Management'
  },
  auth: {
    login: 'Sign In', logout: 'Logout', admin: 'Admin', user: 'User', username: 'Username', password: 'Password', confirmPassword: 'Confirm Password', nickname: 'Nickname', email: 'Email', register: 'Register', noAccount: "Don't have an account? Register", defaultAccount: 'Default: admin / admin123', forgotPassword: 'Forgot Password?', forgotPasswordTitle: 'Reset Password', forgotPasswordDesc: 'Enter your username and we\'ll generate a new password for you', resetPassword: 'Reset Password', resetting: 'Resetting...', resetSuccess: 'Password has been reset!', newPassword: 'New Password', copyAndLogin: 'Copy and Login', signingIn: 'Signing in...', registering: 'Registering...', loginSuccess: 'Login successful!', loginFailed: 'Login failed. Please check your credentials', loginError: 'Login failed. Please check your connection', usernameRequired: 'Please enter username and password', usernameMinLength: 'Username must be at least 3 characters', passwordMinLength: 'Password must be at least 6 characters', passwordMismatch: 'Passwords do not match', registerSuccess: 'Registration successful! Please login', usernamePlaceholder: 'Enter username', passwordPlaceholder: 'Enter password', confirmPasswordPlaceholder: 'Confirm password', nicknamePlaceholder: '3-20 characters', passwordMinPlaceholder: 'At least 6 characters', entering: 'Entering the Nether...'
  },
  dashboard: {
    title: 'Dashboard', subtitle: 'Real-time Monitoring', totalPlayers: 'Total Players', cheatDetections: 'Cheat Detections', highRiskPlayers: 'High Risk Players', activeBans: 'Active Bans', pendingReports: 'Pending Reports', cheatTypesChart: 'Cheat Types Distribution', trendChart: '24h Detection Trend', riskChart: 'Risk Level Distribution', realtimeAlerts: 'Real-time Alerts', connected: 'Connected', disconnected: 'Disconnected', refresh: 'Refresh', noAlerts: 'No recent alerts', severity: 'Severity', player: 'Player', cheatType: 'Cheat Type', serverStatus: 'Server Status', uptime: 'Uptime', notifications: 'Notifications', clearAll: 'Mark All Read', noNotifications: 'No notifications', searchPlaceholder: 'Search players, records...', quickNav: 'Quick Navigation', fullscreen: 'Fullscreen Monitor', toggleTheme: 'Toggle Theme',
    threatLevel: 'Threat Level', events: 'Events', realTimeMonitor: 'Real-time Monitor', live: 'Live', offline: 'Offline', awaitingData: 'Awaiting data stream...', ai: 'AI', aiAnalysis: 'AI Analysis', analysis: 'Analysis', verdict: 'Verdict', action: 'Action', reasoning: 'Reasoning',
    severityLevel: 'Level', severityCritical: 'Critical', severityHigh: 'High', severityMedium: 'Medium', severityLow: 'Low'
  },
  players: {
    title: 'Player Management', subtitle: 'Manage all players', searchPlaceholder: 'Search players...', uuid: 'UUID', risk: 'Risk', kicks: 'Kicks', lastSeen: 'Last Seen', playerDetails: 'Player Details', basicInfo: 'Basic Info', cheatLogs: 'Cheat Logs', punishments: 'Punishments', riskScore: 'Risk Score', kickCount: 'Kick Count', banStatus: 'Ban Status', banned: 'Banned', active: 'Active', noCheatRecords: 'No cheat records', noPunishmentRecords: 'No punishment records', banPlayer: 'Ban Player', unban: 'Unban', confirmDelete: 'Are you sure you want to delete this player?', low: 'Low', medium: 'Medium', high: 'High'
  },
  cheats: {
    title: 'Cheat Logs', subtitle: 'All cheat detection records', allTypes: 'All Types', allSeverity: 'All Severity', severityLow: 'Low', severityMedium: 'Medium', severityHigh: 'High', severityCritical: 'Critical', cheatType: 'Cheat Type', severity: 'Severity', searchPlaceholder: 'Search player or type...', distributionChart: 'Cheat Type Distribution', detailTitle: 'Cheat Detail', confirmDelete: 'Are you sure you want to delete this record?', flying: 'Flying', speed: 'Speed', autoClick: 'Auto Click', killAura: 'Kill Aura', detectedAt: 'Detected At', noRecords: 'No cheat records found', records: 'records', first: 'First', last: 'Last', prev: 'Prev', next: 'Next'
  },
  reports: {
    title: 'Reports', subtitle: 'Handle player reports', pending: 'Pending', allStatus: 'All Status', resolved: 'Resolved', rejected: 'Rejected', reporter: 'Reporter', reported: 'Reported', type: 'Type', reason: 'Reason', noReports: 'No reports found', handleReport: 'Handle Report', reportDetails: 'Report Details', result: 'Result', confirmViolation: 'Confirm Violation', rejectReport: 'Reject Report', notes: 'Notes', banThisPlayer: 'Ban this player', banReason: 'Ban Reason', handledBy: 'Handled By', handledAt: 'Handled At', cheating: 'Cheating', hacking: 'Hacking', griefing: 'Griefing', harassment: 'Harassment', other: 'Other'
  },
  punishments: {
    title: 'Punishments', subtitle: 'Manage bans and punishments', addBan: 'Add Ban', playerName: 'Player Name', playerUUID: 'Player UUID', banType: 'Ban Type', permanent: 'Permanent', temporary: 'Temporary', duration: 'Duration', bannedAt: 'Banned At', noRecords: 'No punishment records', confirmBan: 'Confirm Ban', banning: 'Banning...', confirmUnban: 'Confirm Unban', unban: 'Unban', expired: 'Expired', hour: 'Hour', hours: 'Hours', day: 'Day', days: 'Days'
  },
  whitelist: {
    title: 'Whitelist', subtitle: 'Trusted players list', addToWhitelist: 'Add to Whitelist', addedBy: 'Added By', addedAt: 'Added At', noEntries: 'No whitelist entries', whitelistReason: 'Reason for whitelisting', confirmDisable: 'Disable whitelist for', confirmEnable: 'Enable whitelist for'
  },
  settings: {
    title: 'System Settings', save: 'Save Settings', saveSuccess: 'Settings saved', syncToPlugin: 'Sync to Plugin', syncSuccess: 'Synced to plugin', reset: 'Reset Defaults', resetSuccess: 'Reset to defaults',
    flyDetection: 'Fly Detection', enableFlyDetection: 'Enable Fly Detection', enableFlyDetectionDesc: 'Detect if players use fly hacks',
    speedDetection: 'Speed Detection', enableSpeedDetection: 'Enable Speed Detection', enableSpeedDetectionDesc: 'Detect if players use speed hacks',
    killAuraDetection: 'KillAura Detection', enableKillAuraDetection: 'Enable KillAura Detection', enableKillAuraDetectionDesc: 'Detect if players use KillAura',
    autoClickDetection: 'AutoClick Detection', enableAutoClickDetection: 'Enable AutoClick Detection', enableAutoClickDetectionDesc: 'Detect if players use auto-clickers',
    triggerThreshold: 'Trigger Threshold', triggerThresholdDesc: 'Trigger punishment when violations reach this value',
    autoKick: 'Auto Kick', autoKickDesc: 'Automatically kick player when threshold is reached',
    cpsThreshold: 'CPS Threshold', cpsThresholdDesc: 'Trigger when clicks per second exceed this value',
    punishmentSettings: 'Punishment Settings', enableAutoBan: 'Enable Auto Ban', enableAutoBanDesc: 'Auto ban when risk score reaches threshold',
    autoBanThreshold: 'Auto Ban Threshold', autoBanThresholdDesc: 'Auto ban when risk score reaches this value',
    banType: 'Ban Type', banTypeDesc: 'Type of automatic ban', permanentBan: 'Permanent Ban', temporaryBan: 'Temporary Ban',
    tempBanDuration: 'Temp Ban Duration', tempBanDurationDesc: 'Duration of temporary ban in days', days: 'days',
    enableAutoKick: 'Enable Auto Kick', enableAutoKickDesc: 'Auto kick when threshold is reached',
    autoKickThreshold: 'Auto Kick Threshold', autoKickThresholdDesc: 'Kick when violations reach this value',
    messageSettings: 'Message Settings', warningMessage: 'Warning Message', warningMessageDesc: 'Message sent to player when cheat detected',
    kickMessage: 'Kick Message', kickMessageDesc: 'Message shown when player is kicked', banMessage: 'Ban Message', banMessageDesc: 'Message shown when player is banned',
    notificationSettings: 'Notification Settings', alertSound: 'Alert Sound', alertSoundDesc: 'Play sound on high severity alerts',
    onlyHighSeverity: 'High Severity Only', onlyHighSeverityDesc: 'Only notify on high severity alerts'
  },
  profile: {
    title: 'Profile', subtitle: 'Account settings', profile: 'Profile', password: 'Password', currentPassword: 'Current Password', newPassword: 'New Password', confirmNewPassword: 'Confirm New Password', changePassword: 'Change Password', saveChanges: 'Save Changes', saving: 'Saving...', changing: 'Changing...', createdAt: 'Created At', lastLogin: 'Last Login', superAdmin: 'Super Admin', admin: 'Admin'
  },
  header: {
    connected: 'Connected', disconnected: 'Disconnected'
  },
  ai: {
    assistant: 'AI Assistant', online: 'Online', offline: 'Offline', welcome: 'Hello! I am the anti-cheat AI assistant. I can help you analyze player behavior, handle reports, and answer system questions.', inputPlaceholder: 'Type your question...', disabledPlaceholder: 'AI is not enabled. Please configure API Key in settings', analyze: 'AI Analyze', analysisResult: 'AI Analysis Result', verdict: 'Verdict', confidence: 'Confidence', suggestedAction: 'Suggested Action', reasoning: 'Reasoning', error: 'AI analysis failed', networkError: 'Network error, please check AI service config', clearChat: 'Clear Chat', quickQ1: 'Who are the high-risk players recently?', quickQ2: 'Analyze today\'s detection trends', quickQ3: 'How does fly cheat detection work?', dashboardAnalysis: 'AI Dashboard Analysis', comprehensiveAnalysis: 'Comprehensive Analysis', riskLevel: 'Risk Level', suggestions: 'Suggestions', analyzing: 'AI is analyzing...', clickToAnalyze: 'Click the tab above to start AI analysis', evaluateBan: 'AI Evaluate', banEvaluation: 'AI Ban Evaluation', noPlayerData: 'Cannot get player data'
  }
}
