param(
    [string]$RepositoryRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

Add-Type -AssemblyName System.Drawing

$brandRoot = Join-Path $RepositoryRoot 'assets/brand'
$primaryMasterPath = Join-Path $brandRoot 'masters/soda-project-logo-master.png'
$iconMasterPath = Join-Path $brandRoot 'masters/soda-icon-master.png'
$consoleRoot = Join-Path $RepositoryRoot 'apps/console'

foreach ($path in @($primaryMasterPath, $iconMasterPath)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Brand master not found: $path"
    }
}

$outputDirectories = @(
    (Join-Path $brandRoot 'logo'),
    (Join-Path $brandRoot 'icon'),
    (Join-Path $brandRoot 'lockup'),
    (Join-Path $brandRoot 'app'),
    (Join-Path $brandRoot 'favicon'),
    (Join-Path $consoleRoot 'public/icons'),
    (Join-Path $consoleRoot 'src/assets/images')
)

foreach ($directory in $outputDirectories) {
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
}

function Get-VisibleBounds {
    param([System.Drawing.Bitmap]$Bitmap)

    $left = $Bitmap.Width
    $top = $Bitmap.Height
    $right = -1
    $bottom = -1

    for ($y = 0; $y -lt $Bitmap.Height; $y++) {
        for ($x = 0; $x -lt $Bitmap.Width; $x++) {
            if ($Bitmap.GetPixel($x, $y).A -gt 4) {
                if ($x -lt $left) { $left = $x }
                if ($x -gt $right) { $right = $x }
                if ($y -lt $top) { $top = $y }
                if ($y -gt $bottom) { $bottom = $y }
            }
        }
    }

    if ($right -lt $left -or $bottom -lt $top) {
        throw 'The source image has no visible pixels.'
    }

    return [System.Drawing.Rectangle]::FromLTRB($left, $top, $right + 1, $bottom + 1)
}

function Initialize-Graphics {
    param([System.Drawing.Graphics]$Graphics)

    $Graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceOver
    $Graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $Graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $Graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $Graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
}

function Draw-ContainedImage {
    param(
        [System.Drawing.Graphics]$Graphics,
        [System.Drawing.Image]$Source,
        [System.Drawing.Rectangle]$SourceBounds,
        [System.Drawing.RectangleF]$TargetBounds
    )

    $scale = [Math]::Min(
        $TargetBounds.Width / $SourceBounds.Width,
        $TargetBounds.Height / $SourceBounds.Height
    )
    $width = [single]($SourceBounds.Width * $scale)
    $height = [single]($SourceBounds.Height * $scale)
    $x = [single]($TargetBounds.X + (($TargetBounds.Width - $width) / 2))
    $y = [single]($TargetBounds.Y + (($TargetBounds.Height - $height) / 2))
    $destination = [System.Drawing.RectangleF]::new($x, $y, $width, $height)
    $sourceRectangle = [System.Drawing.RectangleF]::new(
        [single]$SourceBounds.X,
        [single]$SourceBounds.Y,
        [single]$SourceBounds.Width,
        [single]$SourceBounds.Height
    )
    $Graphics.DrawImage($Source, $destination, $sourceRectangle, [System.Drawing.GraphicsUnit]::Pixel)
}

function Export-TransparentSquare {
    param(
        [System.Drawing.Bitmap]$Source,
        [System.Drawing.Rectangle]$SourceBounds,
        [int]$Size,
        [double]$PaddingRatio,
        [string]$OutputPath
    )

    $canvas = [System.Drawing.Bitmap]::new($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    try {
        Initialize-Graphics $graphics
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $padding = [single]($Size * $PaddingRatio)
        $target = [System.Drawing.RectangleF]::new($padding, $padding, $Size - (2 * $padding), $Size - (2 * $padding))
        Draw-ContainedImage $graphics $Source $SourceBounds $target
        $canvas.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $graphics.Dispose()
        $canvas.Dispose()
    }
}

function New-RoundedRectanglePath {
    param(
        [System.Drawing.RectangleF]$Rectangle,
        [single]$Radius
    )

    $diameter = $Radius * 2
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $path.AddArc($Rectangle.X, $Rectangle.Y, $diameter, $diameter, 180, 90)
    $path.AddArc($Rectangle.Right - $diameter, $Rectangle.Y, $diameter, $diameter, 270, 90)
    $path.AddArc($Rectangle.Right - $diameter, $Rectangle.Bottom - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($Rectangle.X, $Rectangle.Bottom - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    return $path
}

function Export-AppIcon {
    param(
        [System.Drawing.Bitmap]$Source,
        [System.Drawing.Rectangle]$SourceBounds,
        [int]$Size,
        [double]$SafeAreaRatio,
        [string]$OutputPath
    )

    $canvas = [System.Drawing.Bitmap]::new($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    try {
        Initialize-Graphics $graphics
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $background = [System.Drawing.RectangleF]::new(0, 0, $Size, $Size)
        $path = New-RoundedRectanglePath $background ([single]($Size * 0.2))
        $brush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml('#0D2B5B'))
        try {
            $graphics.FillPath($brush, $path)
        }
        finally {
            $brush.Dispose()
            $path.Dispose()
        }

        $padding = [single]($Size * $SafeAreaRatio)
        $target = [System.Drawing.RectangleF]::new($padding, $padding, $Size - (2 * $padding), $Size - (2 * $padding))
        Draw-ContainedImage $graphics $Source $SourceBounds $target
        $canvas.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $graphics.Dispose()
        $canvas.Dispose()
    }
}

function Export-HorizontalLockup {
    param(
        [System.Drawing.Bitmap]$Source,
        [System.Drawing.Rectangle]$SourceBounds,
        [System.Drawing.Color]$TextColor,
        [string]$OutputPath
    )

    $width = 640
    $height = 180
    $canvas = [System.Drawing.Bitmap]::new($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    try {
        Initialize-Graphics $graphics
        $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
        $graphics.Clear([System.Drawing.Color]::Transparent)
        Draw-ContainedImage $graphics $Source $SourceBounds ([System.Drawing.RectangleF]::new(8, 8, 164, 164))

        $font = [System.Drawing.Font]::new('Segoe UI', 86, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
        $brush = [System.Drawing.SolidBrush]::new($TextColor)
        try {
            $graphics.DrawString('Soda', $font, $brush, [single]194, [single]34)
        }
        finally {
            $brush.Dispose()
            $font.Dispose()
        }
        $canvas.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $graphics.Dispose()
        $canvas.Dispose()
    }
}

function Export-MultiSizeIco {
    param(
        [string[]]$PngPaths,
        [string]$OutputPath
    )

    $images = @($PngPaths | ForEach-Object {
        [pscustomobject]@{ Bytes = [System.IO.File]::ReadAllBytes($_) }
    })
    $stream = [System.IO.File]::Create($OutputPath)
    $writer = [System.IO.BinaryWriter]::new($stream)
    try {
        $writer.Write([uint16]0)
        $writer.Write([uint16]1)
        $writer.Write([uint16]$images.Count)

        $offset = 6 + (16 * $images.Count)
        for ($index = 0; $index -lt $images.Count; $index++) {
            $bitmap = [System.Drawing.Bitmap]::FromFile($PngPaths[$index])
            try {
                $iconWidth = if ($bitmap.Width -ge 256) { 0 } else { $bitmap.Width }
                $iconHeight = if ($bitmap.Height -ge 256) { 0 } else { $bitmap.Height }
                $writer.Write([byte]$iconWidth)
                $writer.Write([byte]$iconHeight)
            }
            finally {
                $bitmap.Dispose()
            }
            $writer.Write([byte]0)
            $writer.Write([byte]0)
            $writer.Write([uint16]1)
            $writer.Write([uint16]32)
            $writer.Write([uint32]$images[$index].Bytes.Length)
            $writer.Write([uint32]$offset)
            $offset += $images[$index].Bytes.Length
        }

        foreach ($image in $images) {
            $writer.Write($image.Bytes)
        }
    }
    finally {
        $writer.Dispose()
        $stream.Dispose()
    }
}

$primaryMaster = [System.Drawing.Bitmap]::FromFile($primaryMasterPath)
$iconMaster = [System.Drawing.Bitmap]::FromFile($iconMasterPath)
try {
    $primaryBounds = Get-VisibleBounds $primaryMaster
    $iconBounds = Get-VisibleBounds $iconMaster

    foreach ($size in @(1024, 512, 256)) {
        Export-TransparentSquare $primaryMaster $primaryBounds $size 0.04 (Join-Path $brandRoot "logo/soda-project-logo-$size.png")
    }

    foreach ($size in @(1024, 512, 256, 192, 180, 128, 64, 48, 32, 16)) {
        Export-TransparentSquare $iconMaster $iconBounds $size 0.05 (Join-Path $brandRoot "icon/soda-icon-$size.png")
    }

    foreach ($size in @(1024, 512, 192, 180)) {
        Export-AppIcon $iconMaster $iconBounds $size 0.12 (Join-Path $brandRoot "app/soda-app-icon-$size.png")
    }
    Export-AppIcon $iconMaster $iconBounds 512 0.22 (Join-Path $brandRoot 'app/soda-maskable-icon-512.png')

    Export-HorizontalLockup $iconMaster $iconBounds ([System.Drawing.ColorTranslator]::FromHtml('#0D2B5B')) (Join-Path $brandRoot 'lockup/soda-lockup-on-light.png')
    Export-HorizontalLockup $iconMaster $iconBounds ([System.Drawing.Color]::White) (Join-Path $brandRoot 'lockup/soda-lockup-on-dark.png')
}
finally {
    $primaryMaster.Dispose()
    $iconMaster.Dispose()
}

$faviconPngs = @(16, 32, 48) | ForEach-Object { Join-Path $brandRoot "icon/soda-icon-$_.png" }
$faviconPath = Join-Path $brandRoot 'favicon/favicon.ico'
Export-MultiSizeIco $faviconPngs $faviconPath
Copy-Item -LiteralPath (Join-Path $brandRoot 'logo/soda-project-logo-1024.png') -Destination (Join-Path $brandRoot 'soda-logo.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'icon/soda-icon-512.png') -Destination (Join-Path $brandRoot 'soda-icon.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'icon/soda-icon-16.png') -Destination (Join-Path $brandRoot 'favicon/favicon-16x16.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'icon/soda-icon-32.png') -Destination (Join-Path $brandRoot 'favicon/favicon-32x32.png') -Force

Copy-Item -LiteralPath $faviconPath -Destination (Join-Path $consoleRoot 'public/favicon.ico') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'favicon/favicon-16x16.png') -Destination (Join-Path $consoleRoot 'public/favicon-16x16.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'favicon/favicon-32x32.png') -Destination (Join-Path $consoleRoot 'public/favicon-32x32.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'app/soda-app-icon-180.png') -Destination (Join-Path $consoleRoot 'public/apple-touch-icon.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'app/soda-app-icon-192.png') -Destination (Join-Path $consoleRoot 'public/icons/soda-app-icon-192.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'app/soda-app-icon-512.png') -Destination (Join-Path $consoleRoot 'public/icons/soda-app-icon-512.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'app/soda-maskable-icon-512.png') -Destination (Join-Path $consoleRoot 'public/icons/soda-maskable-icon-512.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'lockup/soda-lockup-on-dark.png') -Destination (Join-Path $consoleRoot 'src/assets/images/soda-nav-logo.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'icon/soda-icon-128.png') -Destination (Join-Path $consoleRoot 'src/assets/images/soda-nav-mark.png') -Force
Copy-Item -LiteralPath (Join-Path $brandRoot 'logo/soda-project-logo-256.png') -Destination (Join-Path $consoleRoot 'src/assets/images/soda-project-logo.png') -Force

Write-Host "Generated Soda brand assets under $brandRoot"
