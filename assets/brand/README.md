# Soda 品牌资产

Soda 使用两套互补图形组成品牌系统：细节丰富的苏打杯作为项目主视觉，简化版
苏打杯作为小尺寸产品图标。所有公开使用的文件均由 `masters/` 下的透明 PNG
母版生成。

## 资产预览

| 类型 | 预览 | 主要用途 |
| --- | --- | --- |
| 项目 Logo | ![Soda project logo](logo/soda-project-logo-256.png) | README、官网、发布页、海报 |
| 产品图标 | ![Soda product icon](icon/soda-icon-128.png) | 文档、头像、导航栏、应用入口 |
| 浅色背景横版 | ![Soda lockup on light](lockup/soda-lockup-on-light.png) | 白色或浅色页面 |
| 深色背景横版 | ![Soda lockup on dark](lockup/soda-lockup-on-dark.png) | 深色侧边栏和页脚 |

## 目录说明

| 目录或文件 | 内容 |
| --- | --- |
| `soda-logo.png` | 1024px 项目 Logo 便捷入口 |
| `soda-icon.png` | 512px 产品图标便捷入口 |
| `masters/` | 两套经过确认的透明高清母版 |
| `logo/` | 1024、512、256px 项目 Logo |
| `icon/` | 16 至 1024px 的透明产品图标 |
| `lockup/` | 浅色、深色背景横向组合 Logo |
| `favicon/` | 多尺寸 ICO 与 16/32px PNG |
| `app/` | Apple Touch、PWA 和 maskable 应用图标 |

## 使用规则

- README、项目主页和宣传主视觉优先使用 `logo/` 下的第一版图形。
- 小于 128px 的位置统一使用 `icon/` 下的第二版图形。
- 深色导航栏使用 `lockup/soda-lockup-on-dark.png`；浅色页面使用
  `lockup/soda-lockup-on-light.png`。
- 不拉伸、不旋转、不改变配色，不在图标周围添加无关装饰。
- 小图标四周至少保留图形宽度 5% 的安全空间。
- 小于 32px 时只使用图标，不使用横向组合 Logo。

## 基础色板

| 名称 | 色值 | 用途 |
| --- | --- | --- |
| Soda Navy | `#0D2B5B` | 轮廓、深色背景、主文字 |
| Soda Blue | `#168CE3` | 杯体主色 |
| Soda Aqua | `#20C7C9` | 苏打水与气泡 |
| Soda Cream | `#FFF4D6` | 高光和浅色拼片 |
| Soda Caramel | `#D98224` | 小狗主色 |

## 重新生成

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File tools/generate_brand_assets.ps1
```

脚本会根据两张母版重新输出全部尺寸，并同步更新前端 favicon、PWA 图标和导航栏
资源。不要直接覆盖生成结果；需要调整图形时先替换 `masters/` 中的对应母版。
