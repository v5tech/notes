# 修改 git 提交的时间

有时候我们需要修改 `git commit` 时间，可以通过下面的方法解决。

修改当前本地提交的commit时间

```bash
git commit --amend --date="2019-01-01T00:00:00+0800" -am ":memo: 更新 TODO.md"
```

修改之前提交的某次commit时间，首先通过 `git log` 获取提交的唯一id，然后

```bash
git commit --amend --date="2019-01-01T00:00:00+0800" -C edd2dbbe31fbab492f337628011119493a12a9c6
```

对于之前已经提交到远程仓库的，需要再 `git push` 一次，即可推送到远程仓库
