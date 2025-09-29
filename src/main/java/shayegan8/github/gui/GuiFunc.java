package shayegan8.github.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import me.devnatan.inventoryframework.context.RenderContext;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GuiFunc {

    @SneakyThrows
    private static ItemStack getSkull(String base64) {
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

    private static ItemStack itemCreation(Player player, ConfigurationSection section, String name, int amount) {
        ItemStack item = new ItemStack(Material.valueOf(name), amount);
        ItemMeta meta = item.getItemMeta();
        String text = ColorUtils.C(player, section.getString("text"));
        List<String> lore = section.getStringList("list").stream().map(each -> ColorUtils.C(player, each)).collect(Collectors.toList());
        if(lore != null)
            meta.setLore(lore);
        if(text != null)
            meta.setDisplayName(text);
        item.setItemMeta(meta);
        return item;
    }

    private static void rendering(RenderContext render, String eachSection) {
        ConfigurationSection section = ChatPlugin.configuration_menu.getConfigurationSection(eachSection);
        Player player = render.getPlayer();
        char shape = section.getString("shape").charAt(0);
        String name = section.getString("name");
        int amount = section.getInt("amount");
        ItemStack item = itemCreation(player, section, name, amount);
        render.layoutSlot(shape, (amount_, builder) -> {
            switch (shape) {
                case '1', '2', '3':
                    builder.withItem(item);
                    break;
                case 'a':
                    builder.withItem(item).onClick(click -> click.openForPlayer(GInvite.class));
                    break;
                case 'b':
                    builder.withItem(item).onClick(click -> click.openForPlayer(GGroup.class));
                    break;
                case 'c':
                    builder.withItem(item).onClick(click -> click.openForPlayer(GDelete.class));
                    break;
                case 'd':
                    builder.withItem(item).onClick(click -> click.openForPlayer(GMute.class));
                    break;
                case 'e':
                    builder.withItem(item).onClick(click -> click.openForPlayer(GHelp.class));
                    break;
                case 'f':
                    builder.withItem(item).onClick(click -> {
                        click.closeForPlayer();
                        player.performCommand("chatp help");
                    });
                    break;
                }
        });
    }

    public static void rendererMenu(@NotNull RenderContext render, String path) {
        try(ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            executorService.submit(() -> {
                ChatPlugin.configuration_menu.getConfigurationSection(path).getKeys(false).stream()
                        .filter(eachSection -> !eachSection.equalsIgnoreCase("layout") && !eachSection.equalsIgnoreCase("size") && !eachSection.equalsIgnoreCase("title"))
                        .forEach(eachSection -> rendering(render, eachSection));
            });
        }
    }
}
