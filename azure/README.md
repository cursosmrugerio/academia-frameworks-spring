# Despliegue en Azure (andamiaje — todavía sin desplegar)

Lleva el ejemplo (`../ejemplo/`) a **Azure Container Apps**, con **Azure Database for PostgreSQL Flexible Server** y **Cosmos DB for MongoDB**, e imágenes en **Azure Container Registry**. La aplicación **no cambia**: en la nube solo se inyectan las conexiones por variables de entorno.

> ⚠️ Desplegar **crea recursos reales que cobran**. Nada de esto corre por sí solo: tú ejecutas los comandos tras `az login`. Para borrar todo: `az group delete -n <rg> --yes`.

## Contenido

```
azure/
├── infra/
│   ├── main.bicep            # ACR + PostgreSQL + Cosmos(Mongo) + Container Apps + Log Analytics
│   └── main.parameters.json  # parámetros (pon una contraseña segura al desplegar)
└── README.md                 # esta guía
../ejemplo/Dockerfile          # imagen de la app (build multietapa)
../.github/workflows/deploy-azure.yml  # CI/CD opcional (manual, OIDC)
```

## Requisitos

- Una suscripción de Azure y la **CLI `az`** instalada.
- `az login` y selección de suscripción: `az account set --subscription <ID>`.
- Bicep (la CLI lo instala solo: `az bicep install`).
- **No necesitas Docker local**: la imagen se construye en el ACR con `az acr build`.

## Opción A — Despliegue manual (recomendado para empezar)

```bash
# 0) Variables
RG=rg-banca-academia
LOC=eastus
PG_PASS='PonUnaContrasenaSegura1!'

# 1) Grupo de recursos
az group create -n "$RG" -l "$LOC"

# 2) Infraestructura (ACR, PostgreSQL, Cosmos, entorno y app con imagen placeholder)
az deployment group create -g "$RG" -n main \
  -f azure/infra/main.bicep \
  -p pgAdminPassword="$PG_PASS"

# 3) Construir y publicar la imagen en el ACR (sin Docker local)
ACR=$(az deployment group show -g "$RG" -n main --query properties.outputs.acrName.value -o tsv)
az acr build -r "$ACR" -t banca/ejemplo:v1 ejemplo

# 4) Apuntar la Container App a la imagen recién publicada
ACR_LOGIN=$(az acr show -n "$ACR" --query loginServer -o tsv)
az containerapp update -g "$RG" -n banca-app --image "$ACR_LOGIN/banca/ejemplo:v1"

# 5) URL pública
echo "https://$(az containerapp show -g "$RG" -n banca-app --query properties.configuration.ingress.fqdn -o tsv)"
```

Verifica: `curl https://<fqdn>/actuator/health` → `{"status":"UP"}`.

> Tras el paso 2, la app queda con una imagen *placeholder* y puede aparecer **no saludable** hasta el paso 4 — es esperado.

## Opción B — CI/CD con GitHub Actions (OIDC, sin contraseñas)

El workflow `../.github/workflows/deploy-azure.yml` hace los pasos 1–5 al lanzarlo manualmente (Actions → *Deploy a Azure* → *Run workflow*).

Configuración previa (una vez):

1. **Identidad para GitHub (federada, sin secretos de cliente):**
   ```bash
   az ad app create --display-name banca-academia-gh
   # Toma el appId; crea un service principal y asígnale Contributor en la suscripción:
   az ad sp create --id <appId>
   az role assignment create --assignee <appId> --role Contributor \
     --scope /subscriptions/<SUB_ID>
   # Credencial federada para el repo (rama main):
   az ad app federated-credential create --id <appId> --parameters '{
     "name":"gh-main","issuer":"https://token.actions.githubusercontent.com",
     "subject":"repo:<owner>/<repo>:ref:refs/heads/main",
     "audiences":["api://AzureADTokenExchange"]}'
   ```
2. **Secretos del repo** (Settings → Secrets → Actions):
   `AZURE_CLIENT_ID` (=appId), `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`, `PG_ADMIN_PASSWORD`.
3. Lanza el workflow.

## Qué cambia respecto a local (y qué no)

| | Local (lo que ya corre) | Azure |
|---|---|---|
| Cómputo | `java -jar` / `spring-boot:run` | Container Apps |
| PostgreSQL | contenedor `docker-compose` | Azure Database for PostgreSQL Flexible |
| MongoDB | contenedor `docker-compose` | Cosmos DB for MongoDB |
| Imagen | local | Azure Container Registry |
| Conexión | `application-postgres.properties` | variables de entorno (`SPRING_DATASOURCE_URL`, `SPRING_DATA_MONGODB_URI`) |

**El código de la app es el mismo.** Solo cambia de dónde toma la configuración.

## Notas de producción (fuera del alcance del curso)

- **Secretos:** aquí van como *secrets* de la Container App; en producción, **Azure Key Vault** + identidad administrada.
- **ACR pull:** se usa el usuario admin del ACR por simplicidad; lo recomendable es **identidad administrada** con rol `AcrPull`.
- **PostgreSQL/Cosmos:** sin HA y en SKU mínimos (Burstable / serverless) para abaratar el demo; producción requiere HA y dimensionamiento real.
- **Red:** el firewall de PostgreSQL permite "servicios de Azure"; producción usa redes privadas (VNet/Private Endpoint).

## Limpieza (evita costos)

```bash
az group delete -n rg-banca-academia --yes --no-wait
```
