package me.jellysquid.mods.sodium.client.gui;

import com.google.common.collect.ImmutableList;
import com.sun.org.apache.xpath.internal.operations.Bool;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.binding.compat.VanillaBooleanOptionBinding;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
//import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.MultidrawChunkRenderBackend;
//import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.MultidrawChunkRenderBackend;
import me.jellysquid.mods.sodium.client.util.UnsafeUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.shader.Framebuffer;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gl.Framebuffer;
//import net.minecraft.client.options.AttackIndicator;
//import net.minecraft.client.options.GraphicsMode;
//import net.minecraft.client.options.Option;
//import net.minecraft.client.options.ParticlesMode;
//import net.minecraft.client.util.Window;

import java.util.ArrayList;
import java.util.List;

public class SodiumGameOptionPages {
    private static final SodiumOptionsStorage sodiumOpts = new SodiumOptionsStorage();
    private static final MinecraftOptionsStorage vanillaOpts = new MinecraftOptionsStorage();

    public static OptionPage general() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("视距")
                        .setTooltip("视图距离控制将渲染多远的地形。更短的距离意味着将渲染更少的地形，从而提高帧速率。")
                        .setControl(option -> new SliderControl(option, 2, 32, 1, ControlValueFormatter.quantity("区块")))
                        .setBinding((options, value) -> options.renderDistanceChunks = value, options -> options.renderDistanceChunks)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("亮度")
                        .setTooltip("控制游戏的亮度 (gamma)。")
                        .setControl(opt -> new SliderControl(opt, 0, 100, 1, ControlValueFormatter.brightness()))
                        .setBinding((opts, value) -> opts.gamma = value * 0.01D, (opts) -> (int) (opts.gamma / 0.01D))
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("云")
                        .setTooltip("控制云是否可见。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> {
                            opts.quality.enableClouds = value;

                            if (Minecraft.isFabulousGraphicsEnabled()) {
                                Framebuffer framebuffer = Minecraft.getInstance().worldRenderer.getCloudFrameBuffer();
                                if (framebuffer != null) {
                                    framebuffer.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
                                }
                            }
                        }, (opts) -> opts.quality.enableClouds)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("GUI 比例")
                        .setTooltip("设置游戏界面大小， 如果设置为“自动”，则将使用最大的比例")
                        .setControl(option -> new SliderControl(option, 0, 4, 1, ControlValueFormatter.guiScale()))
                        .setBinding((opts, value) -> {
                            opts.guiScale = value;

                            Minecraft client = Minecraft.getInstance();
                            client.updateWindowSize();
                        }, opts -> opts.guiScale)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("全屏")
                        .setTooltip("如果启用，游戏将全屏显示（如果支持）。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> {
                            opts.fullscreen = value;

                            Minecraft client = Minecraft.getInstance();
                            MainWindow window = client.getMainWindow();

                            if (window != null && window.isFullscreen() != opts.fullscreen) {
                                window.toggleFullscreen();

                                // The client might not be able to enter full-screen mode
                                opts.fullscreen = window.isFullscreen();
                            }
                        }, (opts) -> opts.fullscreen)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("垂直同步")
                        .setTooltip("如果启用该项，游戏的帧率将与显示器的刷新率同步。这会将整体输入延迟作为牺牲以获得更流畅的体验。如果您的电脑运行速度缓慢，此选项可能会降低性能。")
                        .setControl(TickBoxControl::new)
                        .setBinding(new VanillaBooleanOptionBinding(BooleanOption.VSYNC))
                        .setImpact(OptionImpact.VARIES)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("FPS 限制")
                        .setTooltip("限制最大帧率。启用后，此选项会限制游戏渲染，因此开启此选项有利于节省电池寿命或多任务处理。如果启用垂直同步，此选项会被自动忽略，除非该值低于显示器的刷新率。")
                        .setControl(option -> new SliderControl(option, 5, 260, 5, ControlValueFormatter.fpsLimit()))
                        .setBinding((opts, value) -> {
                            opts.framerateLimit = value;
                            Minecraft.getInstance().getMainWindow().setFramerateLimit(value);
                        }, opts -> opts.framerateLimit)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("视角摇晃")
                        .setTooltip("启用后，玩家移动时的视角将会摇晃摆动。禁用该选项可缓解晕动症症状。")
                        .setControl(TickBoxControl::new)
                        .setBinding(new VanillaBooleanOptionBinding(BooleanOption.VIEW_BOBBING))
                        .build())
                .add(OptionImpl.createBuilder(AttackIndicatorStatus.class, vanillaOpts)
                        .setName("攻击指示器")
                        .setTooltip("控制攻击指示器在屏幕上的显示位置。")
                        .setControl(opts -> new CyclingControl<>(opts, AttackIndicatorStatus.class, new String[] { "关", "十字准星", "快捷栏" }))
                        .setBinding((opts, value) -> opts.attackIndicator = value, (opts) -> opts.attackIndicator)
                        .build())
                .build());

        return new OptionPage("通用", ImmutableList.copyOf(groups));
    }

    public static OptionPage quality() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(GraphicsFanciness.class, vanillaOpts)
                        .setName("图形质量")
                        .setTooltip("默认图像品质控制一些原版选项或旧选项，且对于Mod兼容性是必要的。若下方的选项保留为“默认”，则将会使用此选项的品质。")
                        .setControl(option -> new CyclingControl<>(option, GraphicsFanciness.class, new String[] { "快速", "高品质", "极佳" }))
                        .setBinding(
                                (opts, value) -> opts.graphicFanciness = value,
                                opts -> opts.graphicFanciness)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
                        .setName("云的品质")
                        .setTooltip("控制云在空中的渲染品质。")
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
                        .setBinding((opts, value) -> opts.quality.cloudQuality = value, opts -> opts.quality.cloudQuality)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .add(OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
                        .setName("天气品质")
                        .setTooltip("控制雨、雪的渲染品质。")
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
                        .setBinding((opts, value) -> opts.quality.weatherQuality = value, opts -> opts.quality.weatherQuality)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
                        .setName("粒子品质")
                        .setTooltip("控制每次可以在屏幕上出现的粒子的最大数量。")
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
                        .setBinding((opts, value) -> opts.quality.leavesQuality = value, opts -> opts.quality.leavesQuality)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(ParticleStatus.class, vanillaOpts)
                        .setName("粒子品质")
                        .setTooltip("控制每次可以在屏幕上出现的粒子的最大数量。")
                        .setControl(opt -> new CyclingControl<>(opt, ParticleStatus.class, new String[] { "高", "中", "低" }))
                        .setBinding((opts, value) -> opts.particles = value, (opts) -> opts.particles)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(SodiumGameOptions.LightingQuality.class, sodiumOpts)
                        .setName("平滑光照")
                        .setTooltip("控制平滑光照效果的质量。\n" +
                                "\n关 - 没有光滑的光照效果" +
                                "\n低 - 只有方块平滑光照" +
                                "\n高(新!) - 平滑的方块和实体光照")
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.LightingQuality.class))
                        .setBinding((opts, value) -> opts.quality.smoothLighting = value, opts -> opts.quality.smoothLighting)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("群系颜色过渡")
                        .setTooltip("控制生物群系之间方块颜色的采样范围。较高的值会极大地增加渲染区块所需的时间，但带来的画质增加甚微。")
                        .setControl(option -> new SliderControl(option, 0, 7, 1, ControlValueFormatter.quantityOrDisabled("方块", "无")))
                        .setBinding((opts, value) -> opts.biomeBlendRadius = value, opts -> opts.biomeBlendRadius)
                        .setImpact(OptionImpact.LOW)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("实体距离")
                        .setTooltip("控制实体的显示距离。较高的值会以牺牲帧率为代价增加渲染距离。")
                        .setControl(option -> new SliderControl(option, 50, 500, 25, ControlValueFormatter.percentage()))
                        .setBinding((opts, value) -> opts.entityDistanceScaling = value / 100.0F, opts -> Math.round(opts.entityDistanceScaling * 100.0F))
                        .setImpact(OptionImpact.MEDIUM)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("实体阴影")
                        .setTooltip("启用后，在生物和其他实体下面渲染简单的阴影。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.entityShadows = value, opts -> opts.entityShadows)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("晕影")
                        .setTooltip("启用后，屏幕四角处会轻微变暗。除非设备GPU的像素填充率过低，否则基本不影响帧率。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.quality.enableVignette = value, opts -> opts.quality.enableVignette)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());


        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("Mipmap Levels")
                        .setTooltip("控制方块模型材质的多级渐远纹理（Mipmap）的数量。较高的值可使远处的物体获得更好的渲染效果，但在渲染很多动态材质时可能产生严重的性能下降。")
                        .setControl(option -> new SliderControl(option, 0, 4, 1, ControlValueFormatter.multiplier()))
                        .setBinding((opts, value) -> opts.mipmapLevels = value, opts -> opts.mipmapLevels)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build())
                .build());


        return new OptionPage("质量", ImmutableList.copyOf(groups));
    }

    public static OptionPage advanced() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("使用区块多重渲染")
                        .setTooltip("多重渲染允许使用更少的绘制调用来渲染多个块，在渲染世界时大大减少 CPU 开销，同时还可能允许更有效的 GPU 利用率。\n此优化可能会导致某些图形驱动程序出现问题，因此如果您遇到故障，应尝试禁用它。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.useChunkMultidraw = value, opts -> opts.advanced.useChunkMultidraw)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .setImpact(OptionImpact.EXTREME)
                        //.setEnabled(MultidrawChunkRenderBackend.isSupported(sodiumOpts.getData().advanced.ignoreDriverBlacklist))
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("使用顶点数组对象")
                        .setTooltip("通过将有关应如何渲染顶点数据的信息移动到驱动程序中来帮助提高性能，使其能够更好地优化相同对象的重复渲染。 \n除非您使用不兼容的模组，否则通常没有理由禁用此功能。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.useVertexArrayObjects = value, opts -> opts.advanced.useVertexArrayObjects)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("使用方块面剔除")
                        .setTooltip("如果启用，只有面向玩家的方块的侧面将被提交用于渲染。 这可以在渲染过程的早期消除大量方块面，从而节省 GPU 上的内存带宽和时间。 \n某些资源包可能存在此选项的问题，因此如果您看到方块中有洞，请尝试禁用它。")
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.advanced.useBlockFaceCulling = value, opts -> opts.advanced.useBlockFaceCulling)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("使用紧凑顶点格式")
                        .setTooltip("如果启用，将使用更紧凑的顶点格式来渲染块。 这可以显着减少图形内存使用和带宽需求，尤其是对于集成显卡，\n但由于它如何降低位置和纹理坐标属性的精度，可能会导致与某些资源包发生 z-fighting。")
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.advanced.useCompactVertexFormat = value, opts -> opts.advanced.useCompactVertexFormat)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("使用雾遮挡")
                        .setTooltip("如果启用，确定被雾效果完全隐藏的块将不会被渲染，有助于提高性能。 \n当雾效果较重时（例如在水下），改进可能会更加显着，但在某些情况下可能会导致天空和雾之间出现不良的视觉伪影。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.useFogOcclusion = value, opts -> opts.advanced.useFogOcclusion)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("使用实体剔除")
                        .setTooltip("如果启用，则在渲染期间将跳过确定不在任何可见块中的实体。\n这可以通过避免渲染位于地下或墙后的实体来帮助提高性能。")
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.advanced.useEntityCulling = value, opts -> opts.advanced.useEntityCulling)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("使用粒子剔除")
                        .setTooltip("如果启用，将仅渲染确定为可见的粒子。 当附近有许多粒子时，这可以显着提高帧速率。")
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.advanced.useParticleCulling = value, opts -> opts.advanced.useParticleCulling)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("仅对可见纹理进行动画处理")
                        .setTooltip("如果启用，将仅更新确定为可见的动画纹理。这可以显着提高某些硬件的帧速率，尤其是对于较重的资源包。如果您遇到某些纹理未设置动画的问题，请尝试禁用此选项。")
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.advanced.animateOnlyVisibleTextures = value, opts -> opts.advanced.animateOnlyVisibleTextures)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("允许直接内存访问")
                        .setTooltip("如果启用，将允许一些关键代码路径使用直接内存访问来提高性能。这通常会大大降低块和实体渲染的 CPU 开销，\n但会使诊断某些错误和崩溃变得更加困难。只有当您被要求或知道您在做什么时，您才应该禁用此功能。")
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setEnabled(UnsafeUtil.isSupported())
                        .setBinding((opts, value) -> opts.advanced.allowDirectMemoryAccess = value, opts -> opts.advanced.allowDirectMemoryAccess)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("忽略驱动程序黑名单")
                        .setTooltip("如果选中，Sodium 将忽略内置驱动程序黑名单并启用已知会因您的系统配置而损坏的选项。 这可能会导致严重的问题，除非您确实更了解，否则不应开启。\n更改此选项后，必须保存、关闭和重新打开设置屏幕，才能显示以前隐藏的选项。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.ignoreDriverBlacklist = value, opts -> opts.advanced.ignoreDriverBlacklist)
                        .build()
                )
                .build());

        return new OptionPage("高级", ImmutableList.copyOf(groups));
    }

    public static OptionPage experimental() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName("显示 FPS")
                        .setTooltip("显示当前客户端 FPS。主要用于基准测试。")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.experimental.displayFps = value, opts -> opts.experimental.displayFps)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName("FPS显示位置")
                        .setTooltip("FPS 显示的位置")
                        .setControl(option -> new SliderControl(option, 2, 20, 2, ControlValueFormatter.quantity("像素")))
                        .setImpact(OptionImpact.LOW)
                        .setBinding((opts, value) -> opts.experimental.displayFpsPos = value, opts -> opts.experimental.displayFpsPos)
                        .build()
                )
                .build());

        return new OptionPage("实验性", ImmutableList.copyOf(groups));
    }
}
