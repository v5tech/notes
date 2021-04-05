#!/usr/bin/env bash
export JAVA_HOME=$(/usr/libexec/java_home)
export CLASS_PATH=$JAVA_HOME/lib

export MAVEN_OPTS="-Xms256m -Xmx512m"
export M2_HOME=~/develop/maven

export GOROOT=~/develop/go
export GOPATH=~/develop/gowork
export GOBIN=$GOPATH/bin
export GO111MODULE="on"
export GOPROXY="https://goproxy.cn,direct"
export GOSUMDB="sum.golang.google.cn"
export GOPRIVATE="gitlab.51idc.com,git.code.oa.com"

export PUB_HOSTED_URL=https://pub.flutter-io.cn
export FLUTTER_STORAGE_BASE_URL=https://storage.flutter-io.cn
export FLUTTER_HOME=~/develop/flutter

export ANDROID_HOME=~/develop/Android/sdk
export GRADLE_HOME=~/develop/gradle

export NODE_HOME=~/develop/node

export HOMEBREW_BOTTLE_DOMAIN=https://mirrors.ustc.edu.cn/homebrew-bottles

export REDIS_HOME=~/develop/redis

export MONGODB_HOME=~/develop/mongodb

export PATH=$MONGODB_HOME/bin:$REDIS_HOME:$GRADLE_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$FLUTTER_HOME/bin:$M2_HOME/bin:$GOROOT/bin:$GOBIN:$NODE_HOME/bin:$PATH

export PATH=~/.krew/bin:~/.local/bin:$PATH

http='http://127.0.0.1:7890'
socks5='socks5://127.0.0.1:7890'

function reset_launchpad
{
    rm ~/Library/Application\ Support/Dock/*.db && killall Dock
    defaults write com.apple.dock ResetLaunchPad -bool true && killall Dock
}

function redis-start
{
    $REDIS_HOME/redis-server $REDIS_HOME/redis.conf
    echo "redis server started on 6379"
}

function redis-stop
{
    ps -ef | grep redis | grep -v 'grep' | awk '{print $2}' | xargs kill -9
    echo "redis server stoped"
}

function noproxy
{
    unset http_proxy HTTP_PROXY https_proxy HTTPS_PROXY all_proxy ALL_PROXY
    echo "clear proxy done"
}

function proxy
{
    http_proxy=$http
    HTTP_PROXY=$http_proxy
    https_proxy=$http_proxy
    HTTPS_PROXY=$https_proxy
    all_proxy=$http_proxy
    ALL_PROXY=$http_proxy
    echo "current proxy is ${http_proxy}"
    export http_proxy HTTP_PROXY https_proxy HTTPS_PROXY all_proxy ALL_PROXY
}

function show
{
    defaults write com.apple.finder AppleShowAllFiles -boolean true ; killall Finder
}

function hide
{
    defaults write com.apple.finder AppleShowAllFiles -boolean false ; killall Finder
}

[[ -r "/usr/local/etc/profile.d/bash_completion.sh" ]] && . "/usr/local/etc/profile.d/bash_completion.sh" 
