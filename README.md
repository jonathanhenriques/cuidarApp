cuidarApp
===============

## Sobre o projeto

Criado para facilitar o cadastro de pacientes da ONG instituto Cuidar e substituir o sistema atual.

o sistema atual é regido por fichas em papel, o que torna o gerenciamento dos registros complexo e demorado.
A aplicação se mostra necessária diante da quantidade de tempo e trabalho que é despendida todos os meses.


De acordo com os 3 ambientes ```teste```, ```desenvolvimento``` e ```producao```
*Ambiente de teste usa banco com massa de dados*

# Tecnologias usadas

- Java 11 (Java Development Kit - JDK: 11.0.9)
- Spring Boot 2.5.0
- JPA / Hibernate
- Maven
- JUnit 5
- PostgreSQL 15
- H2 para testes em geral
- Flyway 
- Swagger
- Model Mapper 
- Railway

# Diagrama:

<img src="https://github.com/jonathanhenriques/cuidarAppBack/assets/65731881/87d306cb-2906-4bcd-b1d8-6162947a4143" alt="Descrição da imagem" width="400" height="300" />



  ************************************

# Rodando local:<br>
*Usando Ide e container postgres*<br>
1. criando o container postgres:<br>
```
 docker run -d -e POSTGRES_PASSWORD=159951 -e POSTGRES_USER=postgres  -p 5432:5432 -v /c/dataCuidarapp:/var/lib/postgresql/data --name postgres-A postgres
```
*será criada uma pasta para os dados do banco chamada "dataCuidarapp"*
<br>

#### *IMPORTANTE*
As variáveis de conexão entre Banco e aplicação foram criadas como ```variáveis de ambiente``` no windows(export) no linux(set) e no Railway(Deploy)<br>
São elas:
```
set DB_URL=jdbc:postgresql://localhost:5432/
set POSTGRES_USER=postgres
set POSTGRES_PASSWORD=159951
set POSTGRES_PORT=5432
set POSTGRES_DB=cuidarapp
```

a falta delas pode acarretar em erros de conexão:
![image](https://github.com/jonathanhenriques/cuidarAppBack/assets/65731881/813957e1-9669-4467-957a-5be58cc15015)



2. Necessário passar o comando como argumento na IDE
```sql
-Dspring.profiles.active=producao
```

### Acessando o terminal do container postgres:<br>
temos acesso ao terminal postgres com o utilitário ```psql```
```sql
docker exec -it postgres-A psql -U postgres -W cuidarapp
```

- Para listar os bancos usar: ```\l``` *equivalente a SHOW DATABASES;*<br>
- Para selecionar o schema de consultas: ```SET search_path TO cuidarapp_schema;```<br>
- Para listar as tabelas usar:  ```SELECT * FROM tb_paciente;```  ou  ```SELECT * FROM cuidarapp_schema.tb_paciente;```<br>

*Esse ambiente foi pensado para replicar o Banco de produção usado no Deploy, por isso quando rodando
localmente, é necessário fazer um insert do Dump do Banco do Deploy*

*****************

# Rodando a API em container Docker


Caso encontre erros ao executar Docker no windows 11 com wsl:
1. Criação de variável de ambiente no Windows: `DOCKER_HOST=tcp://127.0.0.1:2375`
2. Habilitando *Expose daemon on tcp://localhost:2375 without TLS* no Docker Desktop<br><br>
![image](https://github.com/jonathanhenriques/cuidarAppBack/assets/65731881/d7fb678d-f583-43cf-a507-7b54e0dbd587)



## Subindo containeres do projeto usando Docker-file<br>
```sql
PROFILE=producao docker-compose up --scale cuidarapp-api=1
```

<p>Arquivo docker-compose é composto por 4 imagens:</p>
<ul>
  <li>Imagem do Postgres</li>
  <li>Imagem da aplicação cuidarapp-api</li>
  <li>Imagem do Java openJdk 11</li>
  <li>Imagem do nginx</li>
</ul>

Ao realizar modificações na aplicação, é necessário refazer o jar.<br>
Existe uma comfiguração no pom.xml que recria a imagem docker e faz upload automaticamente para o Docker hub:<br>

```sql
mvn clean package -Pdocker
```
*************************************

### Alternativa - Criando as imagens manualmente

#### criar a imagem da api manualmente:

```sql
docker image build -t cuidarapp-api .
```

#### Criando uma network

* ```network``` = criar uma rede
* ```--driver``` = indica qual driver usar
* ```bridge``` = tipo de driver (acesso para todos os containers locais)
```sql
docker network create —driver bridge cuidarapp-network
```

*************************************

#### Criando  container do Banco manualmente(ALTERAR PARA POSTGRES)<br>
Container MYSQL:>br>
```sql
docker container run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=suaSenha --network cuidarapp-network --name mysqldev mysql:8.0
```
Container POSTGRES:<br>
```sql
docker container run -d -p 5432:5432 -e POSTGRES_PASSWORD=senha_desejada --network cuidarapp-network --name pgdev postgres:latest
```

##### Implantando dados no banco:<br>

Para container local individual usar:<br>
```sql
docker run --name pgdev -e POSTGRES_PASSWORD=senha -d -p 5432:5432 -v /c/docker-volumes/postgres-data:/var/lib/postgresql/data postgres
```
*-v /c/docker-volumes/postgres-data:/var/lib/postgresql/data = cria uma ponte entre pasta local : pasta do postgres dentro do container*

###### Pegando dados persistidos no container e usando no host: container > host

Fazendo dump do container postgres:<br>
```sql
docker exec -t pgdev pg_dump -U postgres -d cuidarapp > dump1.sql

```
*estando dentro do conitainer, utilizar a partir de psql...*

Executando o restore do dump na máquina local:<br>
```sql
psql -U postgres -d cuidarapp < dump1.sql

```

*Também é possível zipar toda a pasta de dados e transferi-la para o host*
*aTmabém é possível usar o Docker Desktop para copiar os arquivos de dados individualmente, mais fácil*

###### Pegando dados persistidos no host e usando no container: host > container

Fazendo dump do banco postgres local:<br>
```sql
pg_dump -U postgres -d cuidarapp > dump.sql
```

Copiando o dump1.sql para dentro do container recém-criado:<br>
```sql
docker cp dump1.sql pgdev:/var/lib/postgresql/data 
```
*ou pgdev:/ raiz*

*******************************************************

###### Acessando postgres do railway pela cli<br>
*usando terminal linux do wsl2 no windows*

1. railway login --browserless<br>
2. railway link (selecione o projeto)<br>
3. railway connect Postgres<br>
4. instalando psql<br>
  4.1. sudo apt-get update<br>
  4.2. sudo apt-get install postgresql-client<br>
5. base.
```sql
pg_dump -U <username> -h <host> -p <port> -W -F t <db_name> > <output_filename>.dump (pegar valores no railway)
```
*************************************************

Para subir os containeres manualmente usando dockerfile (e não o docker-compose, logo sem load balance nginx):
```sql
docker container run --rm -p 8080:8080 -e DB_HOST=cuidarapp-postgres --network cuidarapp-network cuidarapp-api
```
***********************************************************************************************************************************************
## Adendos:<br>
Arquivo ```wait-for-it.sh``` garante que os containeres subam na ordem adequada -> ```cria a rede``` -> ```postgres``` -> ```cuidarapp-api```</p>
Pssíveis erros desse arquivo:
erro " /usr/bin/env: ‘bash\r’: No such file or directory" 
*Solução 1*<br>
1.
usar no terminal: `$ dos2unix wait-for-it.sh`<br>
isso vai formatar o arquivo para sistemas linux (retirando \r usado em windows)
*Solução 2*
2.Mudar de CRLF PARA LF
![image](https://github.com/jonathanhenriques/cuidarAppBack/assets/65731881/46048a52-3388-4965-b431-1082b52330f8)

Após isso refazer a imagem e os containers e irá funcionar.

******************

Run
Para rodar a api localmente na pasta do projeto:

```
java -jar cuidarApp0.0.1-inicial.jar --spring.profiles.active=teste
```
ou
```
mvn spring-boot:run -Dspring.profiles.active=teste
```

Por padrão, a API está disponível em http://localhost:80

Documentation
Swagger (development environment): http://localhost:80/swagger-ui/index.html

******************************************

Autor
Jonathan Henrique da Silva

https://www.linkedin.com/in/jonathanhenriques/
