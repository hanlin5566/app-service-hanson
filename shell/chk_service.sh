#!/bin/bash

#      ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
#      + 1、此脚本为服务监控脚本,与系统定时任务配合使用    
#      + 2、执行crontab -e进入监控任务配置,配置探测脚本执行时间      
#      + 3、创建此脚本后请赋予可执行权限chmod +x chk_service.sh     
#      ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

#
set -i 
source ~/.bashrc 

#获取当前时间
DATE=`date +%Y%m%d`
appName=ufs-adam
#检测地址
#httpCode=`curl -s -o /dev/null -w "%{http_code}" "https://community-stg.unileverfoodsolutions.com.cn/api/v21/adam/member/health"` 
httpCode=`curl --connect-timeout 10  -m 10  -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:29001/api/v21/adam/member/health"`

#检查服务是否正常
function checkService()
{
echo "执行时间: $(date +%Y'-'%m'-'%d' '%H':'%M':'%S) "
if (( "$httpCode"=="200" ));then
        echo "*************************"
        echo "服务正常运行，服务可用！"
		exit
else
        echo "*************************"
        echo "服务不可用！"
        echo "重启服务...预计两分钟"
        source ../deploy.sh restart && sleep 1
        sleep 30s
		status=`ps -ef | grep ${appName} | grep -v "grep"`
		if [ -z "${status}" ];then
		  echo "检测服务重启失败..."
		  echo "再次重启服务..."
		  source ../deploy.sh restart && sleep 1
		  exit
		else
		  echo "服务重启完成..." 
		  exit

		fi		
fi

}

checkService	#调用函数
