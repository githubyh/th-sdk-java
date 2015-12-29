
#user  nobody;
worker_processes  8;
worker_rlimit_nofile 100000;
#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  65535;#worker_connections���ÿ���һ��worker����ͬʱ�򿪵��������������������������ᵽ��worker_rlimit_nofile�����ǿ��Խ����ֵ��úܸߡ���ס�����ͻ���Ҳ��ϵͳ�Ŀ���socket���������ƣ�~ 64K�����������ò���ʵ�ʵĸ�ûʲô�ô���
    multi_accept on;#multi_accept ����nginx�յ�һ��������֪ͨ����ܾ����ܶ�����ӡ�
    use epoll;##�ο��¼�ģ�ͣ�use [ kqueue | rtsig | epoll | /dev/poll | select | poll ]; epollģ����Linux 2.6���ϰ汾�ں��еĸ���������I/Oģ�ͣ�����BSDϵͳ��kqueueģ������ѡ��
}

http {
   	include       mime.types;#includeֻ��һ���ڵ�ǰ�ļ��а�����һ���ļ����ݵ�ָ���������ʹ�����������Ժ���õ���һϵ�е�MIME���͡�
    default_type  application/octet-stream;#default_type�����ļ�ʹ�õ�Ĭ�ϵ�MIME-type��
    charset UTF-8; #charset�������ǵ�ͷ�ļ��е�Ĭ�ϵ��ַ���

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';
	
		log_format  main  ' $remote_user [$time_local]  $http_x_Forwarded_for $remote_addr  $request '  
            '$http_x_forwarded_for '  
            '$upstream_addr '  
            'ups_resp_time: $upstream_response_time '  
            'request_time: $request_time';  
		access_log  logs/access.log  main;
		
		server_tokens off;#server_tokens ��������nginxִ�е��ٶȸ��죬�������Թر��ڴ���ҳ���е�nginx�汾���֣��������ڰ�ȫ�����кô��ġ�
    sendfile        on;
    tcp_nopush    	on;#tcp_nopush ����nginx��һ�����ݰ��﷢������ͷ�ļ�������һ����һ���ķ���
		tcp_nodelay			on;#tcp_nodelay ����nginx��Ҫ�������ݣ�����һ��һ�εķ���--����Ҫ��ʱ��������ʱ����Ӧ�ø�Ӧ������������ԣ���������һС��������Ϣʱ�Ͳ��������õ�����ֵ��

    keepalive_timeout  65;#keepalive_timeout ���ͻ��˷���keep-alive���ӳ�ʱʱ�䡣���������������ʱʱ�����ر����ӡ����ǽ������õ�Щ������ngnix����������ʱ�������
    #client_header_timeout ��client_body_timeout ��������ͷ��������(����)�ĳ�ʱʱ�䡣����Ҳ���԰�������õ�Щ��
		#client_header_timeout 10;
		#client_body_timeout 10;

    reset_timedout_connection on;#reset_timeout_connection����nginx�رղ���Ӧ�Ŀͻ������ӡ��⽫���ͷ��Ǹ��ͻ�����ռ�е��ڴ�ռ䡣
		send_timeout 10;	#send_timeout ָ���ͻ��˵���Ӧ��ʱʱ�䡣������ò�����������ת���������������οͻ��˶�ȡ����֮�䡣��������ʱ���ڣ��ͻ���û�ж�ȡ�κ����ݣ�nginx�ͻ�ر����ӡ�
		#limit_conn_zone $binary_remote_addr zone=addr:5m;#limit_conn_zone�������ڱ������key�����統ǰ���������Ĺ����ڴ�Ĳ�����5m����5���ֽڣ����ֵӦ�ñ����õ��㹻���Դ洢��32K*5��32byte״̬���ߣ�16K*5��64byte״̬��
		#limit_conn addr 100;#limit_connΪ������key�������������������key��addr���������õ�ֵ��100��Ҳ����˵��������ÿһ��IP��ַ���ͬʱ����100�����ӡ�
    gzip on;#gzip�Ǹ���nginx����gzipѹ������ʽ�������ݡ��⽫��������Ƿ��͵���������
		gzip_disable "msie6";#gzip_disableΪָ���Ŀͻ��˽���gzip���ܡ��������ó�IE6���߸��Ͱ汾��ʹ���ǵķ����ܹ��㷺���ݡ�
		# gzip_static on;#gzip_static����nginx��ѹ����Դ֮ǰ���Ȳ����Ƿ���Ԥ��gzip���������Դ����Ҫ����Ԥ��ѹ������ļ�������������б�ע�͵��ˣ����Ӷ�������ʹ�����ѹ���ȣ�����nginx�Ͳ�����ѹ����Щ�ļ���
		gzip_proxied any;#gzip_proxied������߽�ֹѹ�������������Ӧ����Ӧ������������Ϊany����ζ�Ž���ѹ�����е�����
		gzip_min_length 1000;#gzip_min_length���ö���������ѹ���������ֽ��������һ������С��1000�ֽڣ�������ò�Ҫѹ��������Ϊѹ����ЩС�����ݻή�ʹ������������н��̵��ٶȡ�
		gzip_comp_level 4;#gzip_comp_level�������ݵ�ѹ���ȼ�������ȼ�������1-9֮���������ֵ��9����������ѹ�������ġ���������Ϊ4������һ���Ƚ����е����á�
		gzip_types text/plain text/css application/json application/x-javascript text/xml application/xml application/xml+rss text/javascript;#gzip_type������Ҫѹ�������ݸ�ʽ�������������Ѿ���һЩ�ˣ���Ҳ��������Ӹ���ĸ�ʽ��
   # can boost performance, but you need to test those values��û����֤��ע�͵�
		#open_file_cache max=100000 inactive=20s;#open_file_cache�򿪻����ͬʱҲָ���˻��������Ŀ���Լ������ʱ�䡣���ǿ�������һ����Ըߵ����ʱ�䣬�������ǿ��������ǲ������20����������
		#open_file_cache_valid 30s;#open_file_cache_valid ��open_file_cache��ָ�������ȷ��Ϣ�ļ��ʱ�䡣
		#open_file_cache_min_uses 2;#open_file_cache_min_uses ������open_file_cache��ָ��������ʱ���ڼ�����С���ļ�����
		#open_file_cache_errors on;#open_file_cache_errorsָ���˵�����һ���ļ�ʱ�Ƿ񻺴������Ϣ��Ҳ�����ٴθ�����������ļ�������Ҳ�����˷�����ģ�飬��Щ���ڲ�ͬ�ļ��ж���ġ������ķ�����ģ�鲻����Щλ�ã���͵��޸���һ����ָ����ȷ��λ�á�
		upstream apps-cluster {
			#upstream�ĸ��ؾ��⣬weight��Ȩ�أ����Ը��ݻ������ö���Ȩ�ء�weigth������ʾȨֵ��ȨֵԽ�߱����䵽�ļ���Խ��
			server 192.168.157.211:8081 weight=3 max_fails=3 fail_timeout=10s;
			server 192.168.157.211:8082 weight=3 max_fails=3 fail_timeout=10s;
			keepalive 20;
     		 # ���ó־�����ʱ�䡣
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
        #���ض������뷴���������
        
				#����jsp��ҳ�������tomcat��resin����
				location ~ .*.(jsp|jspx|do|action)$ {
					proxy_set_header Host $host;
					proxy_set_header X-Real-IP $remote_addr;
					proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
					proxy_pass http://apps-cluster;
				}
				#���о�̬�ļ���nginxֱ�Ӷ�ȡ������tomcat��resin
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
				#����������ҳ�������tomcat��resin����
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
					#����jsp��ҳ�������tomcat��resin����
					location ~ .(jsp|jspx|do|action)?$ {
						proxy_set_header Host $host;
						proxy_set_header X-Real-IP $remote_addr;
						proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
						proxy_pass http://apps-cluster;
					}
					#���о�̬�ļ���nginxֱ�Ӷ�ȡ������tomcat��resin
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
					
				#����������ҳ�������tomcat��resin����
				location ~ .* {
					proxy_set_header Host $host;
					proxy_set_header X-Real-IP $remote_addr;
					proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
					proxy_pass http://apps-cluster;
				}
			}
			
}
