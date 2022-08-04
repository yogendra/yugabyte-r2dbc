# Yugabtye R2DBC

This is a sample applicaiton that shows how YugabyteDB can be used with Spring Data R2DBC
connectivity

## Get Started

1. Clone Project

    ```shell
    git clone git@github.com:yogendra/yugabyte-r2dbc
    ```

2. Change to `yugendra-r2dbc` directory

3. Run YugabyteDB database

    ```shell
    ./mvnw exec:exec@cluster-create
    ```

4. Build and run Application. Press Ctrl + C to quit

    ```shell
    ./mvnw clean install
    ```

5. Stop YugabyteDB database

      ```shell
      ./mvnw exec:exec@cluster-destroy
      ```

## Local Development

