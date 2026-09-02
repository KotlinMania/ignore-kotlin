# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 8/8 (100.0%)
- **Function parity:** 155/273 matched (target 316) — 56.8%
- **Class/type parity:** 20/47 matched (target 53) — 42.6%
- **Combined symbol parity:** 175/320 matched (target 369) — 54.7%
- **Average inline-code cosine:** 0.49 (function body across 8 matched files)
- **Average documentation cosine:** 0.82 (doc text across 8 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 6 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. walk

- **Target:** `ignore.Walk`
- **Similarity:** 0.17
- **Dependents:** 0
- **Priority Score:** 832408.3
- **Functions:** 37/103 matched (target 46)
- **Missing functions:** `ino`, `is_dir`, `new_stdin`, `new_walkdir`, `new_raw`, `fmt`, `metadata_internal`, `from_entry`, `from_entry_os`, `from_path`, `sort_by_file_name`, `get_or_set_current_dir`, `skip_entry`, `next`, `from`, `is_continue`, `is_quit`, `visit`, `is_symlink`, `add_parents`, `read_dir`, `new_for_each_thread`, `push`, `pop`, `steal`, `run_one`, `generate_work`, `get_work`, `quit_now`, `is_quit_now`, `send`, `send_quit`, `recv`, `deactivate_worker`, `activate_worker`, `check_symlink_loop`, `skip_filesize`, `should_skip_entry`, `stdout_handle`, `path_equals`, `never_equal`, `walkdir_is_dir`, `is_same_file_system`, `device_num`, `wfile`, `wfile_size`, `symlink`, `mkdirp`, `normal_path`, `walk_collect`, `walk_collect_parallel`, `walk_collect_entries_parallel`, `mkpaths`, `tmpdir`, `assert_paths`, `no_ignores`, `custom_ignore`, `custom_ignore_exclusive_use`, `explicit_ignore`, `explicit_ignore_exclusive_use`, `gitignore_parent`, `symlinks`, `first_path_not_symlink`, `symlink_loop`, `no_read_permissions`, `filter`
- **Types:** 5/21 matched (target 8)
- **Missing types:** `DirEntryInner`, `DirEntryRaw`, `Sorter`, `Filter`, `Item`, `WalkEventIter`, `WalkEvent`, `ParallelVisitorBuilder`, `ParallelVisitor`, `FnBuilder`, `FnVisitor`, `FnVisitorImp`, `Message`, `Work`, `Stack`, `Worker`
- **Tests:** 0/22 matched

### 2. dir

- **Target:** `ignore.Dir`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 366206.8
- **Functions:** 22/54 matched (target 35)
- **Missing functions:** `is_absolute_parent`, `add_parents`, `add_child_path`, `matched`, `absolute_base`, `next`, `new`, `build_with_cwd`, `resolve_git_commondir`, `strip_if_is_prefix`, `wfile`, `mkdirp`, `partial`, `tmpdir`, `explicit_ignore`, `gitignore_with_jj`, `gitignore_no_git`, `gitignore_allowed_no_git`, `custom_ignore`, `custom_ignore_over_ignore`, `custom_ignore_precedence`, `ignore_over_gitignore`, `exclude_lowest`, `errored`, `errored_both`, `errored_partial`, `errored_partial_and_ignore`, `not_present_empty`, `stops_at_git_dir`, `absolute_parent`, `absolute_parent_anchored`, `git_info_exclude_in_linked_worktree`
- **Types:** 4/8 matched (target 9)
- **Missing types:** `IgnoreMatchInner`, `IgnoreInner`, `Parents`, `Item`
- **Tests:** 0/22 matched

### 3. lib

- **Target:** `ignore.Lib`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 103305.3
- **Functions:** 20/28 matched (target 24)
- **Missing functions:** `clone`, `from_walkdir`, `description`, `fmt`, `from`, `drop`, `new`, `path`
- **Types:** 3/5 matched (target 16)
- **Missing types:** `Result`, `TempDir`
- **Tests:** 0/3 matched

### 4. gitignore

- **Target:** `ignore.Gitignore`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 84405.3
- **Functions:** 33/41 matched (target 117)
- **Missing functions:** `build_global`, `add_str`, `gitconfig_home_contents`, `gitconfig_xdg_contents`, `excludes_file_default`, `gi_from_str`, `bytes`, `path_string`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Tests:** 6/10 matched

### 5. types

- **Target:** `ignore.Types`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 52804.8
- **Functions:** 20/22 matched (target 62)
- **Missing functions:** `new`, `types`
- **Types:** 3/6 matched (target 8)
- **Missing types:** `Glob`, `GlobInner`, `Selection`
- **Tests:** 1/2 matched

### 6. overrides

- **Target:** `ignore.Overrides`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 42402.6
- **Functions:** 18/20 matched (target 25)
- **Missing functions:** `new`, `ov`
- **Types:** 2/4 matched
- **Missing types:** `Glob`, `GlobInner`
- **Tests:** 7/8 matched

### 7. pathutil

- **Target:** `ignore.Pathutil`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 406.7
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 8. default_types

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

