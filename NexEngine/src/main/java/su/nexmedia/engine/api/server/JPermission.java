package su.nexmedia.engine.api.server;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JPermission extends Permission {

    public JPermission(@NotNull String name) {
        this(name, null);
    }

    public JPermission(@NotNull String name, @Nullable String description) {
        this(name, description, PermissionDefault.OP);
    }

    public JPermission(@NotNull String name, @Nullable String description, @Nullable PermissionDefault defaultValue) {
        super(name, description, defaultValue);
    }

    public void addChildren(@NotNull Permission... childrens) {
        for (Permission children : childrens) {
            children.addParent(this, true);
        }
    }
}
