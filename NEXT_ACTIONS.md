# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/8 (37.5%)
- **Function parity:** 25/327 matched (target 28) — 7.6%
- **Class/type parity:** 3/47 matched (target 17) — 6.4%
- **Combined symbol parity:** 28/374 matched (target 45) — 7.5%
- **Average inline-code cosine:** 0.57 (function body across 3 matched files)
- **Average documentation cosine:** 0.89 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 2 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. lib

- **Target:** `ignore.Lib`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 103305.3
- **Functions:** 20/28 matched (target 21)
- **Missing functions:** `clone`, `from_walkdir`, `description`, `fmt`, `from`, `drop`, `new`, `path`
- **Types:** 3/5 matched (target 15)
- **Missing types:** `Result`, `TempDir`
- **Tests:** 0/3 matched

### 2. pathutil

- **Target:** `ignore.Pathutil`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 406.7
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 3. default_types

- **Target:** `ignore.DefaultTypes`
- **Similarity:** 0.91
- **Dependents:** 0
- **Priority Score:** 100.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

