# Security Vulnerability Reports

## 🔍 **Latest OWASP Vulnerability Scan**

### 📊 **GitHub Actions Report**
- **Pipeline:** Click here for latest scan results
- **Direct Report:** [View OWASP Scan Results](../../../actions)
- **Artifact:** `dependency-check-report.html`

### 🚀 **How to Run New Scan**

**Option 1: GitHub Actions (Recommended)**
```bash
git commit --allow-empty -m "Trigger security scan"
git push origin main
```

**Option 2: Local Scan**
```bash
# Set NVD API key
$env:NVD_API_KEY="your_api_key"

# Run scan
mvn dependency-check:check

# View report
start target/dependency-check-report.html
```

### 📋 **Report Information**

- **Tool:** OWASP Dependency Check 9.0.9
- **Fail Threshold:** CVSS ≥ 7.0 (Critical/Blocker)
- **Frequency:** Every push/PR
- **Coverage:** All Maven dependencies

### 🔐 **Security Compliance**

- ✅ No plain text passwords in code
- ✅ Automated vulnerability scanning
- ✅ Build fails on critical issues
- ✅ Public audit trail

### 📞 **Interview Ready**

**Share this link with interviewer:**
```
https://github.com/priyag82/TradeStore/actions
```

**Shows:** Real-time security status, vulnerability details, and compliance evidence.
