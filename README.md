

## 作业一（2 选 1）：

1. 用你熟悉的编程语言实现一致性 hash 算法。
2. 编写测试用例测试这个算法，测试 100 万 KV 数据，10 个服务器节点的情况下，计算这些 KV 数据在服务器上分布数量的标准差，以评估算法的存储负载不均衡性。



### 作业一：算法实现

- Interface: [com.app.hash.ConsistentHashing](src/main/java/com/app/hash/ConsistentHashing.java)
- 验证代码: [com.app.hash.Main](src/main/java/com/app/hash/Main.java)
- 物理和虚拟节点的定义:[com.app.hash.Node](src/main/java/com/app/hash/Node.java)
- 环形一致性hash的实现: [com.app.hash.CycleConsistentHashing](src/main/java/com/app/hash/CycleConsistentHashing.java)


### 作业二：数据分布分析

基于Main.java的测试，可用得到10个物理节点，100万数据，在不同虚拟节点数量下的分布情况

```text
Physical Nodes: 10
Replicated factor: 3
Virtual Nodes: 30
positions: 40
duplicated positions: 0
Size of K-V pairs: 1000000
Physical Nodes Information: 
Node id: Physical_6,Node type: Physical,Size: 144006
Node id: Physical_9,Node type: Physical,Size: 55599
Node id: Physical_0,Node type: Physical,Size: 145714
Node id: Physical_5,Node type: Physical,Size: 239999
Node id: Physical_3,Node type: Physical,Size: 21760
Node id: Physical_7,Node type: Physical,Size: 72346
Node id: Physical_2,Node type: Physical,Size: 25752
Node id: Physical_8,Node type: Physical,Size: 98867
Node id: Physical_4,Node type: Physical,Size: 8047
Node id: Physical_1,Node type: Physical,Size: 187910
```

利用[extract.py](extract.py)脚本分析得到

| Physical Nodes | Virtual Nodes | Records | STD      | details                                                      |
| -------------- | ------------- | ------- | -------- | ------------------------------------------------------------ |
| 10             | 10            | 1000000 | 84361.59 | 147843\|23883\|274210\|119822\|211107\|54957\|21338\|48153\|4833\|93854 |
| 10             | 30            | 1000000 | 73607.93 | 144006\|55599\|145714\|239999\|21760\|72346\|25752\|98867\|8047\|187910 |
| 10             | 50            | 1000000 | 74432.19 | 1651\|109151\|290070\|99681\|123474\|95464\|29736\|83971\|123413\|43389 |
| 10             | 70            | 1000000 | 48330.31 | 33491\|67816\|219552\|61103\|129074\|112307\|71466\|93601\|96462\|115128 |
| 10             | 100           | 1000000 | 92630.97 | 3409\|22053\|322583\|26953\|132196\|173843\|57394\|33161\|148444\|79964 |
| 10             | 120           | 1000000 | 66235.42 | 79469\|161604\|198834\|24139\|16035\|176291\|96186\|163679\|27114\|56649 |
| 10             | 150           | 1000000 | 72684.34 | 4992\|181414\|175610\|226832\|20974\|130942\|85164\|99950\|40727\|33395 |

从标准差上可用看到，本文实现的一致性哈希算法，在分布上不够均匀，不同物理节点上存在着1个数量级的差异，标准差占总样本数量的10%左右。



文中源码可用在[github](https://github.com/jackeylu/homework-consistent-hashing)获取.

