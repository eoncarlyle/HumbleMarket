#mvn -f backend clean package spring-boot:repackage
java -jar $(find backend/target -regex '.*jar') --spring.config.location=file://$(pwd)/configuration/dev-application.properties
