package shayegan8.github.gui;

import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.context.RenderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import shayegan8.github.ChatPlugin;


public class GMute extends View {

    @Override
    public void onInit(@NotNull ViewConfigBuilder config) {
        Thread.ofVirtual().start(() -> config.title(Component.text(ChatPlugin.configuration_menu.getString("gui.gmute.title.text", "mute players in your group"))
                        .color(TextColor.fromCSSHexString(ChatPlugin.configuration_menu.getString("gui.gmute.title.color", "#5f976b"))))
                .size(ChatPlugin.configuration_menu.getInt("gui.gmute.size", 54)));
    }

    @Override
    public void onFirstRender(@NotNull RenderContext render) {


    }
}
