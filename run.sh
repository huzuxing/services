#!/bin/bash
source /etc/profile

set -e

BASE_PATH=$(cd `dirname $0`; pwd)

_SELF=$0
node=$1

config='config-location'
CMD_TIP="[start|stop|restart|status]"

devopts="-Dspring.config.location=application.yml"
prodopts="-Dspring.config.additional-location=../_conf/global-props.yml -Dio.netty.eventLoopThreads=4 -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"

opts=$devopts

if [[ $node == "-h" ]]; then
	
	echo -e "execute script fun desc:\n./run.sh (node), eg: ./run.sh service|admin|api|gateway|pgsql ..."
	exit -1
fi

function usage() {
    if [[ -n $1 ]]; then
        ct=$1
    else
        ct=${CMD_TIP}
    fi
    nodes=$(ls -l | grep '^d' | awk '{print $9}')
    echo "${_SELF} ${ct} [${nodes}]"
}

abPath=/root/server/node/${node}

# node dir
if [[ ! -n "$node" ]]; then
  echo `usage ${cmd}`
  exit -1
fi

if [[ ! -d ${abPath} ]]; then
  echo "$node don't exist!"
  exit -1
fi

# enter dir
cd ${abPath}

# set var
pidf="server.pid"
# jar & config
nodeFile="node-$node.jar"
pidc="s0"

if [[ -f "$config" ]]; then
    confUrl=$(cat ${config})
    addConfig="-Dspring.config.additional-location=$confUrl"
else
    addConfig=""
fi


if [[ -f ${pidf} ]]; then
    pid=$(cat ${pidf})
else
    pid=-1
fi

log="/var/log/trade/$node/info.log"

function start() {
    cr=$(check)
    if [[ "$cr" -gt "0" ]]; then
        echo "$RUNNING_TIP"
      exit -1
    fi
    optsFile='node.vm'
    if [[ -f "$optsFile" ]]; then
        optsF=$(cat ${optsFile})
        opts="$opts $optsF"
        echo "jvm options: $opts"
    fi
    echo "start ${node} ..."
	cat /dev/null > $log
    echo "cmd: nohup java ${opts} -jar ${nodeFile} > /dev/null 2>&1 &"
    nohup java ${opts} -jar ${nodeFile} > /dev/null 2>&1 &
	echo "-1000" > /proc/$!/oom_score_adj
    echo -n "$!" > ${pidf}
	row=1
	pageSize=50
	totalTime=0.0
	# -1 表示失败
	success=-1
	while true;do
		if [ `echo "$totalTime >= 15"|bc` -eq 1 ]; then
			pid=$(cat $pidf)
			count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
			if [ $count -gt 0 ];then
				echo "Application Started successfully"
				success=0
			fi
			break
		fi
		content=$(tail -n +$row "$log" | head -n $pageSize | sed '/Started .* second/q')
		if [ "$content" != "" ]; then
			# echo $content 会被当作字符串，格式不利于查看
			tail -n +$row "$log" | head -n $pageSize | sed '/Started .* second/q'
			lineCount=$(tail -n +$row "$log" | head -n $pageSize | sed '/Started .* second/q'|wc -l)
			success=$(echo "$content" | grep "Started .* second" | wc -l)	
			if [ $success -gt 0 ]; then
				success=0
				break
			else
				row=$(($row + $lineCount))
			fi
		fi
		sleep 0.005
		totalTime=$(echo "$totalTime + 0.005"|bc)
	done
	#tail -f "$log" | sed '/Started .* second\|Application run failed/q'
	#启动失败处理
	if [ $success -ne 0 ];then
		echo "Application Started failed"
		if [[ -f "$pidf" ]];then
			pid=$(cat $pidf)
			count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
			if [ $count -gt 0 ];then
				kill -15 $pid
			fi
		fi
		exit -1
	fi
	#启动成功处理
	exit 0
    
}

function stop() {
    # 杀死进程
    if [[ ${pid} -gt 0 ]]; then
        echo "stop ${node} pid: ${pid}"
        count=$(checkProc)
        if [[ ${count} -gt 0 ]]; then
            kill -15 ${pid}
        fi
        pid=-1
    fi
    
}


function checkProc() {
    count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
    echo ${count}
}

function check() {
    if [[ ${pid} -le 0 ]]; then
        if [[ -f ${pidf} ]]; then
            rm -f ${pidf}
        fi
        echo "0"
    else
        echo $(checkProc)
    fi
}

function common_build() {
	stop
	sleep 1
	start
}

function service_build() {
    if [[ -f $pidf ]]; then
		echo "mv $pidf $pidc -f"
		mv $pidf $pidc -f
	fi
	optsFile='node.vm'
    if [[ -f "$optsFile" ]]; then
        optsF=$(cat ${optsFile})
        opts="$opts $optsF"
        echo "jvm options: $opts"
    fi
	cat /dev/null > $log
    echo "start ${node} ..."
    echo "cmd: nohup java ${opts} -jar ${nodeFile} > /dev/null 2>&1 &"
    nohup java ${opts} -jar ${nodeFile} > /dev/null 2>&1 &
	echo "-1000" > /proc/$!/oom_score_adj
    echo -n "$!" > ${pidf}
	row=1
	pageSize=50
	totalTime=0.0
	# -1 表示失败
	success=-1
	while true;do
		if [ `echo "$totalTime >= 15"|bc` -eq 1 ]; then
			pid=$(cat $pidf)
			count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
			if [ $count -gt 0 ];then
				echo "Application Started successfully"
				success=0
			fi
			break
		fi
		content=$(tail -n +$row "$log" | head -n $pageSize | sed '/Started .* second/q')
		if [ "$content" != "" ]; then
			# echo $content 会被当作字符串，格式不利于查看
			tail -n +$row "$log" | head -n $pageSize | sed '/Started .* second/q'
			lineCount=$(tail -n +$row $log | head -n $pageSize | sed '/Started .* second/q'|wc -l)
			success=$(echo "$content" | grep "Started .* second" | wc -l)	
			if [ $success -gt 0 ]; then
				success=0
				break
			else
				row=$(($row + $lineCount))
			fi
		fi
		sleep 0.005
		totalTime=$(echo "$totalTime + 0.005"|bc)
	done
    #tail -f $log | sed '/Started .* second\|Application run failed/q'
	#启动失败处理
	if [ $success -ne 0 ];then
		echo "Application Started failed"
		if [[ -f "$pidf" ]];then
			pid=$(cat $pidf)
			count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
			if [ $count -gt 0 ];then
				echo "kill failed process[$pid]"
				kill -9 $pid
			fi
		fi
		exit -1
	fi
	#启动成功处理
	if [[ -f "$pidc" ]];then
		pid=$(cat $pidc)
		tryCount=1
		while true;do
			count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
			if [ $count -eq 0 ];then
				break
			fi
			if [ $tryCount -gt 2 ];then
				kill -9 $pid #强制
				break
			fi
			kill -15 $pid
			sleep 1
			tryCount=$(($tryCount + 1))
		done
		echo "stoped old service[$pid]"
	fi
	
}

function get_valid_port() {
	oldPort=$1
	newPort=$oldPort
	isProRun=$(netstat -anp|grep $newPort|awk '{printf $7}'|cut -d/ -f1)
	if [ "$isProRun" == "" ]; then
		echo $newPort
	else
		newPort=$(($1 + 1))
		r=$(get_valid_port $newPort)
		echo $r
	fi
}

function api_build() {
	## 修改yml配置文件端口号
	oldPort=$(cat application.yml | grep "^  port:" | awk '{print $2}')
	newPort=$(get_valid_port $oldPort)
	echo "change port from ($oldPort) to ($newPort)"
	## 替换 application.yml 端口
	sed -i "s/port: $oldPort/port: $newPort/g" application.yml
	## 启动程序
	optsFile='node.vm'
    if [[ -f "$optsFile" ]]; then
        optsF=$(cat ${optsFile})
        opts="$opts $optsF"
        echo "jvm options: $opts"
    fi
	cat /dev/null > $log
    echo "start ${node} ..."
    echo "cmd: nohup java ${opts} -jar ${nodeFile} > /dev/null 2>&1 &"
    nohup java ${opts} -jar ${nodeFile} > /dev/null 2>&1 &
	echo "-1000" > /proc/$!/oom_score_adj
    echo -n "$!" > ${pidf}
	row=1
	pageSize=50
	totalTime=0.0
	# -1 表示失败
	success=-1
	while true;do
		if [ `echo "$totalTime >= 15"|bc` -eq 1 ]; then
			pid=$(cat $pidf)
			count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
			if [ $count -gt 0 ];then
				echo "Application Started successfully"
				success=0
			fi
			break
		fi
		content=$(tail -n +$row "$log" | head -n $pageSize | sed '/Started .* second/q')
		if [ "$content" != "" ]; then
			# echo $content 会被当作字符串，格式不利于查看
			tail -n +$row "$log" | head -n $pageSize | sed '/Started .* second/q'
			lineCount=$(tail -n +$row $log | head -n $pageSize | sed '/Started .* second/q'|wc -l)
			success=$(echo "$content" | grep "Started .* second" | wc -l)	
			if [ $success -gt 0 ]; then
				success=0
				break
			else
				row=$(($row + $lineCount))
			fi
		fi
		sleep 0.005
		totalTime=$(echo "$totalTime + 0.005"|bc)
	done
    #tail -f $log | sed '/Started .* second\|Application run failed/q'
	#启动失败处理
	if [ $success -ne 0 ];then
		echo "Application Started failed"
		if [[ -f "$pidf" ]];then
			pid=$(cat $pidf)
			count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
			if [ $count -gt 0 ];then
				echo "kill failed process[$pid]"
				kill -15 $pid
			fi
		fi
		exit -1
	fi
	## 修改nginx 配置，并reload
	nginx_conf="/usr/local/program/nginx/conf/nginx.conf"
	sed -i "s/server 127.0.0.1:$oldPort/server 127.0.0.1:$newPort/g" $nginx_conf
	res="$(echo $(nginx -t 2>&1 ))"
	echo $res
	## 修改下一次启动日志文件名
	success="$(echo $res|grep 'successful')"
	if [ "$success" == "" ]; then
		echo $res
		sed -i "s/port: $newPort/port: $oldPort/g" application.yml
		sed -i "s/server 127.0.0.1:$newPort/server 127.0.0.1:$oldPort/g" $nginx_conf
		exit -1
	fi
	reloadNg=$(echo $(nginx -s reload))
	if [[ "$reloadNg" == "" ]] && [[ $oldPort -ne $newPort ]]; then
		## kill掉老进程
		oldPid=$(netstat -anp|grep $oldPort|awk '{printf $7}'|cut -d/ -f1)
		if [ "$oldPid" != "" ]; then
			tryCount=1
			while true;do
				count=$(ps -ef | awk '{print $2}' | grep -w "$pid" | wc -l)
				if [ $count -eq 0 ];then
					break
				fi
				if [ $tryCount -gt 2 ];then
					kill -9 $pid #强制
					break
				fi
				kill -15 $pid
				sleep 1
				tryCount=$(($tryCount + 1))
			done
			echo "stoped old service[$oldPid]"
		fi
	fi
	
}

#jenkins 构建部署用
function copyJar() {
	rootDir=/root/server/node
	baseDir=$rootDir/$node
	jar_source_path=$(find /home/server -type f -name "node-$node.jar")
	jar_target_path=$baseDir/node-$node.jar
	jar_target_path_bak=$baseDir/node-$node.jar.bak

	### 
	if [[ -f "$jar_target_path" ]]; then 
		\cp -p $jar_target_path $jar_target_path_bak
		echo "backup jar from($jar_target_path) to($jar_target_path_bak) successfully "
	fi
	if [[ -f "$jar_source_path" ]]; then 
		echo "$jar_source_path "
		\cp -p $jar_source_path $jar_target_path
		echo "copy jar from($jar_source_path) to($jar_target_path) successfully "
		echo "remove source jar : $jar_source_path"
		rm -rf /home/server/*
	fi
}

function build() {
	copyJar
	
	case "$node" in
		"pgsql")
			common_build
		;;
		"service")
			service_build
		;;
		"admin")
			common_build
		;;
		"api")
			api_build
		;;
		"rate")
			common_build
		;;
		"gateway")
			common_build
		;;
		*)
	esac
}

build


