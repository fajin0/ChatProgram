# ChatProgram
2.1系统概述
实现了简易聊天程序，系统采用C/S结构聊天信息、用户状态信息通过服务器转发，文件采用P2P技术传输，即不需经过服务器转发，客户端之间直接传输文件。采用数据库保存用户注册信息(要求数据库中保存用户名和口令加盐的SM3散列值)。采用多线程、线程池、非阻塞通信技术提高并发性能。
2.2开发环境
Eclipse、jdk13、bcpkix、bcpprov、derby
2.3系统设计
•用户注册(用数据库管理用户注册信息)
–用户登录(示例中已经基本实现)
–在线用户维护(示例中已经实现)
–广播(示例中已经实现)
–私聊
–文件传输
•用进度条显示文件传输进度和传输速度
•传输文件的过程中可以同时聊天，两者互不影响
•文件接收方可以选择拒绝接受文件
•用长文件测试（大于100M）
–信息加密传输
•基于D-H密钥交换，采用AES算法加密；或者基于JSSE加密

2.4协议设计
应用层协议由消息构成，每种消息用一个对象来实现：
聊天消息：包括广播消息、用户状态消息、私聊消息、群聊消息、文件消息
（1）聊天消息：用户之间以及与服务器之间的普通聊天消息

（2）用户状态消息：包括用户上线消息、在线用户消息、用户下线消息
（3）广播消息：
作用：将聊天信息广播给所有在线用户
通信方：用户->服务器，服务器->其它在线用户
（4）用户上线消息
作用：新用户登录时，通知服务器和其它在线用户
通信方：新上线用户->服务器，服务器->其它在线用户
（5）用户下线消息
作用：用户下线时，通知服务器和其它在线用户
通信方：下线用户->服务器，服务器->其它在线用户
（6）在线用户消息
作用：当新上线用户登录时，由服务器把当前在线的用户发给新登录的用户
通信方：服务器->新登录用户
（7）用户注册信息：向服务器申请注册首先在数据库中查找该用户是否已存在，并向用户提示该用户已存在，如果不存在，再插入用户，返回插入成功信息
（8）用户注册判断信息：先在数据库中查找该用户是否已存在，如果不存在，再插入用户
（9）用户登录信息：用户向服务器发送请求登录信息
（10）用户登录判断信息：服务器先在数据库中查找在数据库中是否存在该用户，如果存在，返回登录判断成功，否则，失败。
（11）文件发送请求消息：用户直接给另一用户发送文件请求消息，如果对方确认接收，就开始传文件
（12）文件消息：文件发送方和文件接受方用来交换信息的消息
信息类的主要关系如下图所示：


2.5主要类介绍
客户端：客户端代码由两个类构成，Client类是GUI类，负责与用户交互，在Client类内部，用一个内部Runnable类来实现“后台监听线程”，监听并处理服务器传来的信息，类名为ListeningHandler，客户端成功登录服务器后，就启动客户端的“后台监听线程”












客户端后台监听线程，用来处理收到的各种消息：
a)“后台监听线程”收到了服务器转发来的用户上线消息
用绿色文字将用户名和用户上线时间添加到“消息记录”文本框中
在“在线用户”列表中增加新上线的用户名
b)“后台监听线程”收到了服务器转发来的在线用户消息
在“在线用户”列表中增加在线用户名
c)“后台监听线程”收到了服务器转发来的广播消息
用黑色文字将收到消息的时间、发送消息的用户名和消息内容添加到“消息记录”文本框中
d)“后台监听线程”收到了服务器转发来的用户下线消息
用绿色文字将用户名和用户下线时间添加到“消息记录”文本框中
在“在线用户”列表中删除下线的用户名
e)“后台监听线程”收到了其他客户端发来的文件发送请求消息
被请求方弹框显示选择是否接受请求，选择后将信息返回给客户端
A开启“文件发送线程”，向B发送“文件发送请求消息”，此消息由服务器转发给B
“文件发送请求消息”中包含A的用户名，B的用户名，文件对应的File对象等信息
B收到“文件发送请求消息”后，开启“文件接收处理线程”，向A发送“文件发送响应消息”，此消息亦由服务器转发给A
如果B同意接收文件， 就向A发送同意接收文件的“文件发送响应消息”，并在用ServerSocket打开一个本机端口，并通过“文件发送响应消息”把ServerSocket的地址和端口发给A，等待A来建立连接。
如果B不同意接收文件， 就向A发送不同意接收文件的“文件发送响应消息”。
A收到“文件发送响应消息”后，开启“文件发送处理线程”
如果B同意接收文件， 就创建一个Socket于B连接，并通过这个Socket把文件内容发给B。
如果B不同意接收文件，打印提示信息直接退出即可
f)“后台监听线程”收到其他客户端发来的文件信息
如果用户同意接收文件， 就创建一个Socket于B连接，并通过这个Socket把文件内容发给B。创建文件接收线程，开始接收文件。









下图为客户端监听处理线程，对收到的消息进行分类并作出正确的处理




客户端验证服务器的证书，建立安全套接字连接。
发送文件函数：文件接收方同意接收文件，并建立一个serversocket，将端口连接等信息发给发送方，发送方请求连接，完成文件传输。






处理用户状态信息：将用户上线消息、下线消息输出在面板上











聊天信息处理：将用户发出的信息打印在面板上
class RecieveFileHandler implements Runnable用来处理对文件的发送
计算文件总长度，并发送文件

服务器：服务器代码由主要由四个类构成，Server类是GUI类，负责与用户交互，在Server类内部，用一个匿名内部Thread类来实现“接受用户连接线程”，接受并处理客户端连接请求，在Server类内部，用一个内部Runnable类来实现“用户服务线程”，类名为UserHandler，每一个新用户登录服务器，服务器就为其创建一个“用户服务线程”，UserManager类用来管理在线用户，User类对应一个在线用户


g)“用户服务线程”收到客户端发来的用户上线消息
向新上线的用户转发当前在线用户消息列表
向所有其它在线用户发送用户上线消息
用绿色文字将用户名和用户上线时间添加到“消息记录”文本框中
将用户信息加入到“在线用户”列表中
h)“用户服务线程”收到客户端发来的广播消息
用黑色文字将收到消息的时间、发送消息的用户名和消息内容添加到“消息记录”文本框中
将消息转发给所有其它在线用户
i)“用户服务线程”收到客户端发来的用户下线消息
用绿色字在“消息记录”文本框中显示用户下线消息及下线时间
在“在线用户列表”中删除下线用户
将用户下线消息转发给所有其它在线用户
j)“用户服务线程”收到客户端发来的用户注册消息
服务器首先在数据库中查找该用户是否已存在，并向用户提示该	用户已	存在，如果不存在，再插入用户，返回插入成功信息
k)“用户服务线程”收到客户端发来的用户登录消息
服务器先在数据库中查找在数据库中是否存在该用户，如果存在，返回登	录判断成功，否则，失败。
l)“用户服务线程”收到客户端发来的给其他用户发送的文件请求传输消息
服务器根据文件发送请求方的信息，转发这个请求给文件接收方

对用户的操作信息的判别并作出相应的处理

处理用户状态消息，上线

处理用户状态消息，下线后通知客户端删除下线用户，客户端也删除下线用户

处理聊天消息，对用户消息识别并发给用户或大家

处理用户注册消息，并返回注册结果是否成功

处理用户的文件发送请求消息



2.6系统使用说明
启动服务端等待用户连接

用户进行注册

用户登录
用户aaa、bbb、nnn登录、并更新你在线用户信息
服务器端更新用户上线信息


用户私聊

用户公聊





用户aaa给用户bbb发送文件，用户bbb接收文件


用户bbb收到请求，同意并选择保存路径
文件发送进度



用户bbb退出后，其他客户端和服务端删除bbb在线信息
注册失败之用户已存在


2.7项目分析总结
通过编写网络聊天程序，使我对java的界面设计进一步熟练，以及各个控件的使用。服务器与客户端的通信使用SSLServerSocket和SSLSocket，对比以前的ServerSocket和Socket更加安全，因为使用了SSL协议进行加密传输，并且在进行各种消息的传递过程中，使用多线程完成。在项目进行过程中，我认为最主要的问题是协议设计，各个功能的业务逻辑分析。此代码只是简单的模拟网络信息传输，许多功能没有实现。
3课程设计总结
实践是学习语言的一个重要途径，通过课设设计，我们使用各种语言完成程序，养成良好的代码习惯和规范使用函数代码，使我受益颇深。
参考文献
可以是图书、期刊、网页等，借鉴过的源代码也请在这里加以说
