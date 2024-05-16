- 发布新版本时，需要新构建脚手架
- 构建脚手架
```
mvn archetype:create-from-project
#切换项目路径到目录 
cd .\target\generated-sources\archetype
#执行 
mvn install
```

- 使用脚手架生成项目
```
mvn archetype:generate -DgroupId="com.iss.ufs" -Dversion="1.0.0-SNAPSHOT" -DartifactId="ufs-adam-schedule" -Dpackage="com.iss.ufs" -DarchetypeGroupId="com.iss.hanson" -DarchetypeArtifactId="hanson-archetype-archetype"

如果报current project as it is not of packaging type ‘pom‘，把生成target的根目录的pom.xml packaging改为pom

```


# 项目介绍

## 目录介绍
```
├─sql # 发布上线的脚本 根据 ${yyyyMMdd}_CR_XXX.sql | ${yyyyMMdd}_HOTFIX_XXX.sql 命名
├─YYY-XXX-api 供三方服务调用的api接口
├─YYY-XXX-client 调用三方的cliet接口
├─YYY-XXX-common 通用的工具类、常量等
├─YYY-XXX-dao 数据层
└─YYY-XXX-job 整合xxljob的定时任务模块
├─YYY-XXX-service 服务层
└─YYY-XXX-start 供展示层调用的接口

client 添加了带有获取token的Resttemplate authRestTemplate，内部调用了authClient的方法，需要重新编写对应业务的authClient方法获取token。
后续 使用指定的 authRestTemplate 即可每次请求，自动获取token，并完成请求。
@Resource(name = "authRestTemplate")
private RestTemplate restTemplate;

```

## 打包
```
#执行 
cd $workspace/YYY-XXX
mvn install
```

## 版本介绍
```
1.0.0 基础的脚手架，继承了mybatis-plus 和 MBG 增删改查demo
1.0.1 在1.0.0版本上，增加了traceFilter、MBG-BO、cache manager
1.0.2 在1.0.1版本上，增加了userInfoInterceptor,signatureFilter
1.0.3 在1.0.2版本上，增加了国际化、job模块、派发 jwt token、企微集成。 
```


