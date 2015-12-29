
#user  nobody;
worker_processes  8;
worker_rlimit_nofile 100000;
#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  65535;#worker_connections设置可由一个worker进程同时打开的最大连接数。如果设置了上面提到的worker_rlimit_nofile，我们可以将这个值设得很高。记住，最大客户数也由系统的可用socket连接数限制（~ 64K），所以设置不切实际的高没什么好处。
    multi_accept on;#multi_accept 告诉nginx收到一个新连接通知后接受尽可能多的连接。
    use epoll;##参考事件模型，use [ kqueue | rtsig | epoll | /dev/poll | select | poll ]; epoll模型是Linux 2.6以上版本内核中的高性能网络I/O模型，对于BSD系统，kqueue模型是首选。
}

http {
   	include       mime.types;#include只是一个在当前文件中包含另一个文件内容的指令。这里我们使用它来加载稍后会用到的一系列的MIME类型。
    default_type  application/octet-stream;#default_type设置文件使用的默认的MIME-type。
    charset UTF-8; #charset设置我们的头文件中的默认的字符集

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';
	
		log_format  main  ' $remote_user [$time_local]  $http_x_Forwarded_for $remote_addr  $request '  
            '$http_x_forwarded_for '  
            '$upstream_addr '  
            'ups_resp_time: $upstream_response_time '  
            'request_time: $request_time';  
		access_log  logs/access.log  main;
		
		server_tokens off;#server_tokens 并不会让nginx执行的速度更快，但它可以关闭在错误页面中的nginx版本数字，这样对于安全性是有好处的。
    sendfile        on;
    tcp_nopush    	on;#tcp_nopush 告诉nginx在一个数据包里发送所有头文件，而不一个接一个的发送
		tcp_nodelay			on;#tcp_nodelay 告诉nginx不要缓存数据，而是一段一段的发送--当需要及时发送数据时，就应该给应用设置这个属性，这样发送一小块数据信息时就不能立即得到返回值。

    keepalive_timeout  65;#keepalive_timeout 给客户端分配keep-alive链接超时时间。服务器将在这个超时时间过后关闭链接。我们将它设置低些可以让ngnix持续工作的时间更长。
    #client_header_timeout 和client_body_timeout 设置请求头和请求体(各自)的超时时间。我们也可以把这个设置低些。
		#client_header_timeout 10;
		#client_body_timeout 10;

    reset_timedout_connection on;#reset_timeout_connection告诉nginx关闭不响应的客户端连接。这将会释放那个客户端所占有的内存空间。
		send_timeout 10;	#send_timeout 指定客户端的响应超时时间。这个设置不会用于整个转发器，而是在两次客户端读取操作之间。如果在这段时间内，客户端没有读取任何数据，nginx就会关闭连接。
		#limit_conn_zone $binary_remote_addr zone=addr:5m;#limit_conn_zone设置用于保存各种key（比如当前连接数）的共享内存的参数。5m就是5兆字节，这个值应该被设置的足够大以存储（32K*5）32byte状态或者（16K*5）64byte状态。
		#limit_conn addr 100;#limit_conn为给定的key设置最大连接数。这里key是addr，我们设置的值是100，也就是说我们允许每一个IP地址最多同时打开有100个连接。
    gzip on;#gzip是告诉nginx采用gzip压缩的形式发送数据。这将会减少我们发送的数据量。
		gzip_disable "msie6";#gzip_disable为指定的客户端禁用gzip功能。我们设置成IE6或者更低版本以使我们的方案能够广泛兼容。
		# gzip_static on;#gzip_static告诉nginx在压缩资源之前，先查找是否有预先gzip处理过的资源。这要求你预先压缩你的文件（在这个例子中被注释掉了），从而允许你使用最高压缩比，这样nginx就不用再压缩这些文件了
		gzip_proxied any;#gzip_proxied允许或者禁止压缩基于请求和响应的响应流。我们设置为any，意味着将会压缩所有的请求。
		gzip_min_length 1000;#gzip_min_length设置对数据启用压缩的最少字节数。如果一个请求小于1000字节，我们最好不要压缩它，因为压缩这些小的数据会降低处理此请求的所有进程的速度。
		gzip_comp_level 4;#gzip_comp_level设置数据的压缩等级。这个等级可以是1-9之间的任意数值，9是最慢但是压缩比最大的。我们设置为4，这是一个比较折中的设置。
		gzip_types text/plain text/css application/json application/x-javascript text/xml application/xml application/xml+rss text/javascript;#gzip_type设置需要压缩的数据格式。上面例子中已经有一些了，你也可以再添加更多的格式。
   # can boost performance, but you need to test those values，没有验证先注释掉
		#open_file_cache max=100000 inactive=20s;#open_file_cache打开缓存的同时也指定了缓存最大数目，以及缓存的时间。我们可以设置一个相对高的最大时间，这样我们可以在它们不活动超过20秒后清除掉。
		#open_file_cache_valid 30s;#open_file_cache_valid 在open_file_cache中指定检测正确信息的间隔时间。
		#open_file_cache_min_uses 2;#open_file_cache_min_uses 定义了open_file_cache中指令参数不活动时间期间里最小的文件数。
		#open_file_cache_errors on;#open_file_cache_errors指定了当搜索一个文件时是否缓存错误信息，也包括再次给配置中添加文件。我们也包括了服务器模块，这些是在不同文件中定义的。如果你的服务器模块不在这些位置，你就得修改这一行来指定正确的位置。
		upstream apps-cluster {
			#upstream的负载均衡，weight是权重，可以根据机器配置定义权重。weigth参数表示权值，权值越高被分配到的几率越大。
			server 192.168.157.211:8081 weight=3 max_fails=3 fail_timeout=10s;
			server 192.168.157.211:8082 weight=3 max_fails=3 fail_timeout=10s;
			keepalive 20;
     		 # 设置持久连接时间。
		}

    server {
        listen       80;
				listen 443 ssl;
        server_name  openapi.tianhong.cn;
		    if ($server_port = 80) {
		         rewrite ^http://$host https://$host permanent;
		         rewrite ^(.*)$ https://$host$1 permanent;
		    }
		    ssl_certificate /cert/1__.tianhong.cn_bundle.crt;  
				ssl_certificate_key /cert/2__.tianhong.cn.key; 
		
		    ssl_session_timeout 5m;
		
		    ssl_protocols SSLv2 SSLv3 TLSv1;  
				ssl_ciphers ALL:!ADH:!EXPORT56:RC4+RSA:+HIGH:+MEDIUM:+LOW:+SSLv2:+EXP;  
        #charset koi8-r;
        #access_log  logs/host.access.log  main;

        location / {
            root   html;
            index  index.html index.htm;
        }

        error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
        #本地动静分离反向代理配置
        
				#所有jsp的页面均交由tomcat或resin处理
				location ~ .*.(jsp|jspx|do|action)$ {
					proxy_set_header Host $host;
					proxy_set_header X-Real-IP $remote_addr;
					proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
					proxy_pass http://apps-cluster;
				}
				#所有静态文件由nginx直接读取不经过tomcat或resin
				location ~ .*.(htm|html|gif|jpg|jpeg|png|bmp|swf|ioc|rar|zip|txt|flv|mid|doc|ppt|pdf|xls|mp3|wma)$ { 
					#root html;
					##proxy_pass http://apps-cluster;
					##proxy_set_header Host  $host;
					##proxy_set_header X-Real-IP  $remote_addr;
					##proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
					proxy_redirect off;
					proxy_set_header Host $host;
					#proxy_cache cache_one;
					proxy_cache_valid 200 302 1h;
					proxy_cache_valid 301 1d;
					proxy_cache_valid any 1m;
					expires 30d;
				}
				location ~ .*.(js|css)$ { 
					proxy_redirect off;
					proxy_set_header Host $host;
					#proxy_cache cache_one;
					proxy_cache_valid 200 302 1h;
					proxy_cache_valid 301 1d;
					proxy_cache_valid any 1m;
					expires 1h; 
				}
				#所有其他的页面均交由tomcat或resin处理
				location ~ .* {
					proxy_set_header Host $host;
					proxy_set_header X-Real-IP $remote_addr;
					proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
					proxy_pass http://apps-cluster;
				}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


    # HTTPS server
  		server {  
					listen 443 ssl;  
					server_name localhost;  
					  
					ssl on;  
					ssl_certificate /cert/1__.tianhong.cn_bundle.crt;  
					ssl_certificate_key /cert/2__.tianhong.cn.key;  
					  
					ssl_session_timeout 5m;  
					  
					ssl_protocols SSLv2 SSLv3 TLSv1;  
					ssl_ciphers ALL:!ADH:!EXPORT56:RC4+RSA:+HIGH:+MEDIUM:+LOW:+SSLv2:+EXP;  
					ssl_prefer_server_ciphers on;  
					  
					location / {      
			        root html;      
			        index  index.html index.htm; 
			    }  
					#所有jsp的页面均交由tomcat或resin处理
					location ~ .(jsp|jspx|do|action)?$ {
						proxy_set_header Host $host;
						proxy_set_header X-Real-IP $remote_addr;
						proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
						proxy_pass http://apps-cluster;
					}
					#所有静态文件由nginx直接读取不经过tomcat或resin
					location ~ .*.(htm|html|gif|jpg|jpeg|png|bmp|swf|ioc|rar|zip|txt|flv|mid|doc|ppt|pdf|xls|mp3|wma)$ { 
						proxy_pass http://apps-cluster;
						##proxy_set_header Host  $host;
						##proxy_set_header X-Real-IP  $remote_addr;
						##proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
						proxy_redirect off;
						proxy_set_header Host $host;
						#proxy_cache cache_one;
						proxy_cache_valid 200 302 1h;
						proxy_cache_valid 301 1d;
						proxy_cache_valid any 1m;
						expires 30d;
					}
					location ~ .*.(js|css)?$
					{ expires 1h; }
					
				#所有其他的页面均交由tomcat或resin处理
				location ~ .* {
					proxy_set_header Host $host;
					proxy_set_header X-Real-IP $remote_addr;
					proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
					proxy_pass http://apps-cluster;
				}
			}
			
}
