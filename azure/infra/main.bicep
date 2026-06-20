// Infraestructura del ejemplo en Azure (sin desplegar todavía).
// Target: Azure Container Apps + Azure Container Registry + PostgreSQL Flexible + Cosmos DB (Mongo).
// Despliegue: ver ../README.md (requiere `az login`; crea recursos con costo).

@description('Prefijo para nombrar los recursos')
param namePrefix string = 'banca'

@description('Región de Azure (por defecto, la del resource group)')
param location string = resourceGroup().location

@description('Usuario administrador de PostgreSQL')
param pgAdminUser string = 'banca'

@secure()
@description('Contraseña del administrador de PostgreSQL (pásala en el despliegue, no la guardes en texto)')
param pgAdminPassword string

@description('Imagen inicial del contenedor (placeholder; se reemplaza al publicar la imagen real)')
param containerImage string = 'mcr.microsoft.com/k8se/quickstart:latest'

@description('Puerto en el que escucha la aplicación')
param targetPort int = 8080

var suffix = uniqueString(resourceGroup().id)
var acrName = toLower('${namePrefix}acr${suffix}')
var pgName = toLower('${namePrefix}-pg-${suffix}')
var cosmosName = toLower('${namePrefix}-mongo-${suffix}')
var lawName = '${namePrefix}-law'
var envName = '${namePrefix}-cae'
var appName = '${namePrefix}-app'
var dbName = 'banca'

// --- Azure Container Registry ---
resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: acrName
  location: location
  sku: { name: 'Basic' }
  properties: {
    adminUserEnabled: true // simple para el curso; en producción, identidad administrada + AcrPull
  }
}

// --- Log Analytics (logs de Container Apps) ---
resource law 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: lawName
  location: location
  properties: {
    sku: { name: 'PerGB2018' }
    retentionInDays: 30
  }
}

// --- Entorno de Container Apps ---
resource env 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: envName
  location: location
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: law.properties.customerId
        sharedKey: law.listKeys().primarySharedKey
      }
    }
  }
}

// --- PostgreSQL Flexible Server (Burstable, pequeño) ---
resource pg 'Microsoft.DBforPostgreSQL/flexibleServers@2024-08-01' = {
  name: pgName
  location: location
  sku: { name: 'Standard_B1ms', tier: 'Burstable' }
  properties: {
    version: '16'
    administratorLogin: pgAdminUser
    administratorLoginPassword: pgAdminPassword
    storage: { storageSizeGB: 32 }
    backup: { backupRetentionDays: 7, geoRedundantBackup: 'Disabled' }
    highAvailability: { mode: 'Disabled' } // en producción: ZoneRedundant
    authConfig: { passwordAuth: 'Enabled', activeDirectoryAuth: 'Disabled' }
  }
}

resource pgDb 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2024-08-01' = {
  parent: pg
  name: dbName
  properties: { charset: 'UTF8', collation: 'en_US.utf8' }
}

// Permitir a los servicios de Azure (Container Apps) conectarse a PostgreSQL
resource pgFirewall 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2024-08-01' = {
  parent: pg
  name: 'AllowAzureServices'
  properties: { startIpAddress: '0.0.0.0', endIpAddress: '0.0.0.0' }
}

// --- Cosmos DB para MongoDB (RU, serverless) ---
resource cosmos 'Microsoft.DocumentDB/databaseAccounts@2024-05-15' = {
  name: cosmosName
  location: location
  kind: 'MongoDB'
  properties: {
    databaseAccountOfferType: 'Standard'
    apiProperties: { serverVersion: '6.0' }
    capabilities: [ { name: 'EnableServerless' } ]
    locations: [ { locationName: location, failoverPriority: 0 } ]
  }
}

resource cosmosDb 'Microsoft.DocumentDB/databaseAccounts/mongodbDatabases@2024-05-15' = {
  parent: cosmos
  name: dbName
  properties: { resource: { id: dbName } }
}

// --- Container App ---
var jdbcUrl = 'jdbc:postgresql://${pg.properties.fullyQualifiedDomainName}:5432/${dbName}?sslmode=require'
var acrCreds = acr.listCredentials()
var mongoConn = cosmos.listConnectionStrings().connectionStrings[0].connectionString

resource app 'Microsoft.App/containerApps@2024-03-01' = {
  name: appName
  location: location
  properties: {
    managedEnvironmentId: env.id
    configuration: {
      ingress: {
        external: true
        targetPort: targetPort
        transport: 'auto'
      }
      registries: [
        {
          server: acr.properties.loginServer
          username: acrCreds.username
          passwordSecretRef: 'acr-password'
        }
      ]
      secrets: [
        { name: 'acr-password', value: acrCreds.passwords[0].value }
        { name: 'pg-password', value: pgAdminPassword }
        { name: 'mongo-conn', value: mongoConn }
      ]
    }
    template: {
      containers: [
        {
          name: appName
          image: containerImage
          resources: { cpu: json('0.5'), memory: '1Gi' }
          env: [
            { name: 'SPRING_DATASOURCE_URL', value: jdbcUrl }
            { name: 'SPRING_DATASOURCE_USERNAME', value: pgAdminUser }
            { name: 'SPRING_DATASOURCE_PASSWORD', secretRef: 'pg-password' }
            { name: 'SPRING_DATA_MONGODB_URI', secretRef: 'mongo-conn' }
            { name: 'SPRING_JPA_HIBERNATE_DDL_AUTO', value: 'update' }
          ]
        }
      ]
      scale: { minReplicas: 1, maxReplicas: 3 }
    }
  }
}

output acrName string = acr.name
output acrLoginServer string = acr.properties.loginServer
output appName string = app.name
output appUrl string = 'https://${app.properties.configuration.ingress.fqdn}'
output postgresHost string = pg.properties.fullyQualifiedDomainName
output cosmosAccount string = cosmos.name
