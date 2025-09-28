package shayegan8.github.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import me.devnatan.inventoryframework.RootView;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.context.RenderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import shayegan8.github.ChatPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class Menu extends View {

    @SneakyThrows
    private ItemStack getSkull(String base64) {
        ItemStack skuss = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skuss.getItemMeta();
        GameProfile gProf = new GameProfile(UUID.randomUUID(), null);
        gProf.getProperties().put("textures", new Property("textures", base64));

        Field profField = meta.getClass().getField("profile");
        profField.setAccessible(true);
        profField.set(meta, gProf);
        skuss.setItemMeta(meta);
        return skuss;
    }

    @Override
    public void onInit(@NotNull ViewConfigBuilder config) {
        Thread.ofVirtual().start(() -> config.title(Component.text(ChatPlugin.configuration_menu.getString("gui.menu.title.text", "ChatPlugin menu"))
                .color(TextColor.fromCSSHexString(ChatPlugin.configuration_menu.getString("gui.menu.title.color", "#5f976b"))))
                .size(ChatPlugin.configuration_menu.getInt("gui.menu.size", 54)));
    }

    @Override
    public void onFirstRender(@NotNull RenderContext render) {
        ItemStack skuss = getSkull("");
        render.slot(23, skuss).onClick((click -> click.openForPlayer(PlayerList.class)));
        Thread.ofVirtual().start(() -> {
            ChatPlugin.configuration_menu.getConfigurationSection("gui.menu").getKeys(false).parallelStream().forEach(key -> {
                ConfigurationSection eachSection = ChatPlugin.configuration_menu.getConfigurationSection(key);
                String name = eachSection.getString("name");
                int slot = eachSection.getInt("slot");
                int amount = eachSection.getInt("amount");
                if(eachSection.getString("id") != null) {
                    String id = eachSection.getString("id");
                    try {
                        Class<?> idClass = Class.forName(id);
                        if(!RootView.class.isAssignableFrom(idClass))
                            return;
                        @SuppressWarnings("unchecked")
                        Class<? extends RootView> obtainedFConf = (Class<? extends RootView>) idClass.getDeclaredConstructor().newInstance().getClass();
                        render.slot(slot, new ItemStack(Material.valueOf(name), amount)).onClick(click -> click.openForPlayer( obtainedFConf));
                    } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                    render.slot(slot, new ItemStack(Material.valueOf(name), amount)).onClick(click -> click.openForPlayer(GMute.class));
                    return;
                }
                if(name.equals("PLAYER_HEAD")) {
                    ItemStack stork = getSkull(eachSection.getString("texture"));
                    stork.setAmount(amount);
                    render.slot(slot, stork);
                    return;
                }
                render.slot(slot, new ItemStack(Material.valueOf(name), amount));
            });
        });
    }
}
