**本文章将介绍使用该工具释放page blob租约，完成文件删除**

#### **先决条件**
1. Framework 4.0+
2. Visual Studio 2012+
3. 删除Page Blob文件时遇到报错信息："There is currently a lease on the blob and no lease ID was specified in the request. ..."，可用该工具解决。



#### **使用语法**
```python

breaklease <accountName> <accountKey> <page blob url>

```



#### **测试示例**
```python

breaklease portalvhds70gssrgbwgbn3 dvxTERq99Ws8EkGbmRK4VThrclJ2THAK1gd051gFK9z6k29NR4L51BFrm5Q4NPJt7qUKRwq33nMSlRjpAX1BnA== https://portalvhds70gssrgbwgbn3.blob.core.chinacloudapi.cn/vhds/team-server-team-server-0530-1.vhd


```


