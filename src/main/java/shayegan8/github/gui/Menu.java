package shayegan8.github.gui;

import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.context.RenderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import shayegan8.github.ChatPlugin;


public class Menu extends View {

    @Override
    public void onInit(@NotNull ViewConfigBuilder config) {
        Thread.ofVirtual().start(() -> {
            config.title(Component.text(ChatPlugin.configuration_menu.getString("gui.menu.title.text", "ChatPlugin menu"))
                .color(TextColor.fromCSSHexString(ChatPlugin.configuration_menu.getString("gui.menu.title.color", "#5f976b"))))
                .size(ChatPlugin.configuration_menu.getInt("gui.menu.size", 54));
            config.layout(ChatPlugin.configuration_menu.getStringList("gui.menu.layout").toArray(new String[0]));
        });
    }

    @Override
    public void onFirstRender(@NotNull RenderContext render) {
        GuiFunc.renderer(render, "gui.menu");
    }
}
