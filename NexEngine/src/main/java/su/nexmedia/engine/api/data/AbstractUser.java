package su.nexmedia.engine.api.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;

import java.util.UUID;

public abstract class AbstractUser<P extends NexPlugin<P>> {

    protected final P plugin;
    protected final UUID uuid;

    protected String name;
    protected long   dateCreated;
    protected long   lastOnline;
    protected long   cachedUntil;

    @Deprecated private boolean isRecent = false;

    public AbstractUser(@NotNull P plugin, @NotNull UUID uuid, @NotNull String name, long dateCreated, long lastOnline) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.name = name;
        this.setDateCreated(dateCreated);
        this.setLastOnline(lastOnline);
        this.setCachedUntil(-1);
    }

    public void onLoad() {

    }

    public void onUnload() {

    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public <U extends AbstractUser<P>> void saveData(@NotNull UserDataHolder<P, U> dataHolder) {
        this.plugin.runTaskAsync(task -> dataHolder.getData().saveUser((U) this));
    }

    @Deprecated
    public boolean isRecentlyCreated() {
        return isRecent;
    }

    @Deprecated
    public void setRecentlyCreated(boolean recent) {
        isRecent = recent;
    }

    public boolean isCacheExpired() {
        return this.getCachedUntil() > 0 && System.currentTimeMillis() > this.getCachedUntil();
    }

    public long getCachedUntil() {
        return cachedUntil;
    }

    public void setCachedUntil(long cachedUntil) {
        this.cachedUntil = cachedUntil;
    }

    @NotNull
    public final UUID getId() {
        return this.uuid;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    public final long getDateCreated() {
        return dateCreated;
    }

    public final void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public final long getLastOnline() {
        return this.lastOnline;
    }

    public final void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public final boolean isOnline() {
        return this.getPlayer() != null;
    }

    @NotNull
    public final OfflinePlayer getOfflinePlayer() {
        return this.plugin.getServer().getOfflinePlayer(this.getId());
    }

    @Nullable
    public final Player getPlayer() {
        return this.plugin.getServer().getPlayer(this.getId());
    }

    @Override
    public String toString() {
        return "AbstractUser [uuid=" + this.uuid + ", name=" + this.name + ", lastOnline=" + this.lastOnline + "]";
    }
}
