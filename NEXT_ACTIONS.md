# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 8/8 (100.0%)
- **Function parity:** 165/332 matched (target 324) — 49.7%
- **Class/type parity:** 19/47 matched (target 58) — 40.4%
- **Combined symbol parity:** 184/379 matched (target 382) — 48.5%
- **Average inline-code cosine:** 0.17 (function body across 8 matched files)
- **Average documentation cosine:** 0.82 (doc text across 8 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 8 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. walk

- **Target:** `ignore.Walk`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 1226409.5
- **Functions:** 38/143 matched (target 54)
- **Missing functions:** `ino`, `is_dir`, `new_stdin`, `new_walkdir`, `new_raw`, `path`, `into_path`, `path_is_symlink`, `is_stdin`, `metadata`, `file_type`, `file_name`, `depth`, `ino`, `is_dir`, `fmt`, `path`, `into_path`, `path_is_symlink`, `metadata`, `metadata_internal`, `metadata_internal`, `file_type`, `file_name`, `depth`, `ino`, `from_entry`, `from_entry_os`, `from_entry_os`, `from_entry_os`, `from_path`, `from_path`, `from_path`, `fmt`, `sort_by_file_name`, `get_or_set_current_dir`, `new`, `skip_entry`, `next`, `from`, `next`, `is_continue`, `is_quit`, `build`, `build`, `visit`, `visit`, `threads`, `is_dir`, `is_symlink`, `add_parents`, `read_dir`, `new_for_each_thread`, `push`, `pop`, `steal`, `run_one`, `generate_work`, `get_work`, `quit_now`, `is_quit_now`, `send`, `send_quit`, `recv`, `deactivate_worker`, `activate_worker`, `check_symlink_loop`, `skip_filesize`, `should_skip_entry`, `stdout_handle`, `path_equals`, `never_equal`, `never_equal`, `walkdir_is_dir`, `is_same_file_system`, `device_num`, `device_num`, `device_num`, `wfile`, `wfile_size`, `symlink`, `mkdirp`, `normal_path`, `walk_collect`, `walk_collect_parallel`, `walk_collect_entries_parallel`, `mkpaths`, `tmpdir`, `assert_paths`, `no_ignores`, `custom_ignore`, `custom_ignore_exclusive_use`, `gitignore`, `explicit_ignore`, `explicit_ignore_exclusive_use`, `gitignore_parent`, `max_depth`, `min_depth`, `max_filesize`, `symlinks`, `first_path_not_symlink`, `symlink_loop`, `same_file_system`, `no_read_permissions`, `filter`
- **Types:** 5/21 matched (target 11)
- **Missing types:** `DirEntryInner`, `DirEntryRaw`, `Sorter`, `Filter`, `Item`, `WalkEventIter`, `WalkEvent`, `ParallelVisitorBuilder`, `ParallelVisitor`, `FnBuilder`, `FnVisitor`, `FnVisitorImp`, `Message`, `Work`, `Stack`, `Worker`
- **Tests:** 0/27 matched

### 2. dir

- **Target:** `ignore.Dir`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 407008.9
- **Functions:** 26/62 matched (target 34)
- **Missing functions:** `is_absolute_parent`, `add_parents`, `add_child_path`, `matched`, `absolute_base`, `next`, `new`, `build_with_cwd`, `parents`, `resolve_git_commondir`, `strip_if_is_prefix`, `wfile`, `mkdirp`, `partial`, `tmpdir`, `explicit_ignore`, `git_exclude`, `gitignore`, `gitignore_with_jj`, `gitignore_no_git`, `gitignore_allowed_no_git`, `ignore`, `custom_ignore`, `custom_ignore_over_ignore`, `custom_ignore_precedence`, `ignore_over_gitignore`, `exclude_lowest`, `errored`, `errored_both`, `errored_partial`, `errored_partial_and_ignore`, `not_present_empty`, `stops_at_git_dir`, `absolute_parent`, `absolute_parent_anchored`, `git_info_exclude_in_linked_worktree`
- **Types:** 4/8 matched (target 9)
- **Missing types:** `IgnoreMatchInner`, `IgnoreInner`, `Parents`, `Item`
- **Tests:** 0/25 matched

### 3. lib

- **Target:** `ignore.Error`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 113308.7
- **Functions:** 20/28 matched (target 25)
- **Missing functions:** `clone`, `from_walkdir`, `description`, `fmt`, `from`, `drop`, `new`, `path`
- **Types:** 2/5 matched (target 18)
- **Missing types:** `Error`, `Result`, `TempDir`
- **Tests:** 0/3 matched

### 4. gitignore

- **Target:** `ignore.Gitignore`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 94608.5
- **Functions:** 34/43 matched (target 117)
- **Missing functions:** `new`, `build_global`, `add_str`, `gitconfig_home_contents`, `gitconfig_xdg_contents`, `excludes_file_default`, `gi_from_str`, `bytes`, `path_string`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Tests:** 7/11 matched

### 5. types

- **Target:** `ignore.Types`
- **Similarity:** 0.14
- **Dependents:** 0
- **Priority Score:** 53008.6
- **Functions:** 22/24 matched (target 62)
- **Missing functions:** `new`, `types`
- **Types:** 3/6 matched (target 8)
- **Missing types:** `Glob`, `GlobInner`, `Selection`
- **Tests:** 1/2 matched

### 6. pathutil

- **Target:** `ignore.Pathutil`
- **Similarity:** 0.06
- **Dependents:** 0
- **Priority Score:** 50909.4
- **Functions:** 4/9 matched (target 6)
- **Missing functions:** `is_hidden`, `is_hidden`, `strip_prefix`, `is_file_name`, `file_name`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 7. overrides

- **Target:** `ignore.Overrides`
- **Similarity:** 0.38
- **Dependents:** 0
- **Priority Score:** 42606.2
- **Functions:** 20/22 matched (target 25)
- **Missing functions:** `new`, `ov`
- **Types:** 2/4 matched
- **Missing types:** `Glob`, `GlobInner`
- **Tests:** 9/10 matched

### 8. default_types

- **Target:** `ignore.DefaultTypes`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 106.5
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

