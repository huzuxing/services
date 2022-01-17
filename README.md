# services
提供一些通用，可用组件
#redis实现分布式锁：
1、不支持可重入
2、配置集群模式 
2.1集群模式下，满足 N/2 + 1 实例加锁成功，则加锁成功
2.2代价稍微高
3、支持哨兵模式、主从
#分布式ID生成器
基于雪花算法，生成分布式唯一ID，保证顺序递增，支持ID反解
long 类型，高位为0，40位时间戳（毫秒），10位机器ID，12位序列号

支持2^10-1=1023台机器，同一毫秒最大峰值，2^12-1=4095个ID产生

支持ID反解，通过ID可以知道ID是什么时候，由哪台机器产生。

机器ID供应方： 1、配置文件 2、ip 3、DB 4、zookeeper

说明： 当同一毫秒，序列号用完后，必须等下一毫秒产生


#test模块
测试项目，各个代码组件测试用例等
