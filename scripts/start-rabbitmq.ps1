param(
    [string]$ContainerName = "roomos-rabbitmq",
    [string]$Image = "rabbitmq:3-management",
    [int]$AmqpPort = 5672,
    [int]$ManagementPort = 15672
)

$ErrorActionPreference = "Stop"

function Ensure-Docker {
    docker version | Out-Null
}

function Ensure-Image([string]$imageName) {
    $exists = docker images --format "{{.Repository}}:{{.Tag}}" | Where-Object { $_ -eq $imageName }
    if (-not $exists) {
        Write-Host "Pulling image $imageName ..."
        docker pull $imageName
    }
}

function Start-Or-CreateContainer {
    $container = docker ps -a --filter "name=^/${ContainerName}$" --format "{{.Names}}"
    if ($container) {
        $running = docker ps --filter "name=^/${ContainerName}$" --format "{{.Names}}"
        if ($running) {
            Write-Host "Container $ContainerName is already running."
            return
        }
        Write-Host "Starting existing container $ContainerName ..."
        docker start $ContainerName | Out-Null
        return
    }

    Write-Host "Creating container $ContainerName ..."
    docker run -d --name $ContainerName -p "${AmqpPort}:5672" -p "${ManagementPort}:15672" $Image | Out-Null
}

function Show-Status {
    Write-Host ""
    Write-Host "RabbitMQ status:"
    docker ps --filter "name=^/${ContainerName}$"
    Write-Host ""
    Write-Host "Management UI: http://127.0.0.1:$ManagementPort"
    Write-Host "Default username: guest"
    Write-Host "Default password: guest"
}

Ensure-Docker
Ensure-Image -imageName $Image
Start-Or-CreateContainer
Show-Status