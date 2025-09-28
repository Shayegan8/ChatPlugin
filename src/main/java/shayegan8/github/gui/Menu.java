package shayegan8.github.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.context.RenderContext;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import shayegan8.github.ChatPlugin;

import java.lang.reflect.Field;
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
        config.title("ChatPlugin Menu").size(54);
    }

    @Override
    public void onFirstRender(@NotNull RenderContext render) {
        ItemStack skuss = getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI0NDJiYmY3MTcxYjVjYWZjYTIxN2M5YmE0NGNlMjc2NDcyMjVkZjc2Y2RhOTY4OWQ2MWE5ZjFjMGE1ZjE3NiJ9fX0");
        render.slot(23, skuss).onClick((click -> click.openForPlayer(PlayerList.class)));
        Thread.ofVirtual().start(() -> {
            ChatPlugin.configuration_menu.getConfigurationSection("gui.menu").getKeys(false).parallelStream().forEach(key -> {
                ConfigurationSection eachSection = ChatPlugin.configuration_menu.getConfigurationSection(key);
                String name = eachSection.getString("name");
                int slot = eachSection.getInt("slot");
                int amount = eachSection.getInt("amount");
                render.slot(slot, new ItemStack(Material.valueOf(name), amount));
            });
        });
    }
}
