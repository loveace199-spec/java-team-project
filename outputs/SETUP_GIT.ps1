$ErrorActionPreference = 'Stop'
$taskRepo = Split-Path -Parent $PSScriptRoot

function Invoke-ProjectGit {
    & git -c "safe.directory=$taskRepo" -C $taskRepo @args
    if ($LASTEXITCODE -ne 0) {
        throw "Git command failed. Exit code: $LASTEXITCODE"
    }
}

if (-not (Test-Path -LiteralPath (Join-Path $taskRepo '.git'))) {
    Invoke-ProjectGit init -b main
}

Invoke-ProjectGit config --local pull.ff only
Invoke-ProjectGit config --local fetch.prune true
Invoke-ProjectGit add .gitignore .gitattributes README.md .github/pull_request_template.md outputs/GIT_WORKFLOW.md outputs/SETUP_GIT.ps1

& git -c "safe.directory=$taskRepo" -C $taskRepo diff --cached --quiet
if ($LASTEXITCODE -eq 1) {
    Invoke-ProjectGit commit -m 'chore: set up project Git workflow'
} elseif ($LASTEXITCODE -ne 0) {
    throw 'Unable to inspect staged changes.'
}

& git -c "safe.directory=$taskRepo" -C $taskRepo show-ref --verify --quiet refs/heads/develop
if ($LASTEXITCODE -eq 1) {
    Invoke-ProjectGit switch -c develop
} elseif ($LASTEXITCODE -eq 0) {
    Invoke-ProjectGit switch develop
} else {
    throw 'Unable to inspect develop branch.'
}

Invoke-ProjectGit status --short
Invoke-ProjectGit branch -v
Write-Host 'Local Git setup complete. Remote repository is not connected yet.'
