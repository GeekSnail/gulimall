FROM nginx:1.17.1
EXPOSE 80 443

ADD html.tar.gz /usr/share/nginx/html/
ADD default.conf /etc/nginx/conf.d/

ENTRYPOINT nginx -g "daemon off;"