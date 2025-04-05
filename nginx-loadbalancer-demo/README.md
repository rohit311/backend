Source - https://medium.com/javarevisited/using-nginx-as-loadbalancer-between-two-spring-boot-applications-a02011a3531


Execute these commands to run the docker containers for both (service1 & service2):

- mvn clean install
- docker-compose build
- docker-compose up -d
- for i in {1..30}; do curl http://localhost:9090; done
