package su.nexmedia.engine.api.manager;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public interface IPlaceholder {

    String DELIMITER_DEFAULT = "\n" + ChatColor.GREEN;

    @NotNull UnaryOperator<String> replacePlaceholders();
}
