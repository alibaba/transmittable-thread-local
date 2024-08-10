package com.alibaba.crr.composite;

import com.alibaba.crr.Transmittable;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.alibaba.ttl3.internal.util.Utils.newHashMap;
import static com.alibaba.ttl3.internal.util.Utils.propagateIfFatal;

/**
 * {@link CompositeTransmittable} transmit all {@link Transmittable}
 * registered by {@link #registerTransmittable(Transmittable)}.
 * <p>
 * Transmittance is completed by methods {@link #capture()} =&gt;
 * {@link #replay(Capture)} =&gt; {@link #restore(Backup)} (aka {@code CRR} operations).
 * <p>
 * <B><I>CAUTION:</I></B><br>
 * This implementation just ignore all exception thrown by
 * {@code CRR} operations of registered {@link Transmittable}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public final class CompositeTransmittable implements Transmittable<Capture, Backup> {
    private static final Logger logger = Logger.getLogger(CompositeTransmittable.class.getName());

    private final Set<Transmittable<Object, Object>> registeredTransmittableSet = new CopyOnWriteArraySet<>();

    private final CompositeTransmitCallback callback;

    public CompositeTransmittable(CompositeTransmitCallback callback) {
        this.callback = callback;
    }

    /**
     * Capture all {@link Transmittable}.
     *
     * @return the captured values
     */
    @NonNull
    public Capture capture() {
        final HashMap<Transmittable<Object, Object>, Object> transmit2Value = newHashMap(registeredTransmittableSet.size());
        for (Transmittable<Object, Object> transmittable : registeredTransmittableSet) {
            try {
                transmit2Value.put(transmittable, transmittable.capture());
            } catch (Throwable t) {
                propagateIfFatal(t);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when capture for transmittable " + transmittable +
                            "(class " + transmittable.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return new Snapshot(transmit2Value, null);
    }

    /**
     * Replay the captured values from {@link #capture()},
     * and return the backup values before replay.
     *
     * @param captured captured values {@link #capture()}
     * @return the backup values before replay
     * @see #capture()
     */
    @NonNull
    public Backup replay(@NonNull Capture captured) {
        final Object data = callback.beforeReplay();

        final Snapshot capturedSnapshot = (Snapshot) captured;
        final HashMap<Transmittable<Object, Object>, Object> transmit2Value = newHashMap(capturedSnapshot.transmit2Value.size());
        for (Map.Entry<Transmittable<Object, Object>, Object> entry : capturedSnapshot.transmit2Value.entrySet()) {
            Transmittable<Object, Object> transmittable = entry.getKey();
            try {
                Object transmitCaptured = entry.getValue();
                transmit2Value.put(transmittable, transmittable.replay(transmitCaptured));
            } catch (Throwable t) {
                propagateIfFatal(t);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when replay for transmittable " + transmittable +
                            "(class " + transmittable.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }

        final Object afterData = callback.afterReplay(data);
        return new Snapshot(transmit2Value, afterData);
    }

    /**
     * Clear all values, and return the backup values before clear.
     * <p>
     * Semantically, the code {@code `Object backup = clear();`} is same as {@code `Object backup = replay(EMPTY_CAPTURE);`}.
     * <p>
     * The reason for providing this method is:
     *
     * <ol>
     * <li>lead to more readable code</li>
     * <li>need not provide the constant {@code EMPTY_CAPTURE}.</li>
     * </ol>
     *
     * @return the backup values before clear
     * @see #replay(Capture)
     */
    @NonNull
    public Backup clear() {
        final Object data = callback.beforeReplay();

        final HashMap<Transmittable<Object, Object>, Object> transmit2Value = newHashMap(registeredTransmittableSet.size());
        for (Transmittable<Object, Object> transmittable : registeredTransmittableSet) {
            try {
                transmit2Value.put(transmittable, transmittable.clear());
            } catch (Throwable t) {
                propagateIfFatal(t);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when clear for transmittable " + transmittable +
                            "(class " + transmittable.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }

        final Object afterData = callback.afterReplay(data);
        return new Snapshot(transmit2Value, afterData);
    }

    /**
     * Restore the backup values from {@link #replay(Capture)}/{@link #clear()}.
     *
     * @param backup the backup values from {@link #replay(Capture)}/{@link #clear()}
     * @see #replay(Capture)
     * @see #clear()
     */
    public void restore(@NonNull Backup backup) {
        final Snapshot snapshot = (Snapshot) backup;
        final Object data = callback.beforeRestore(snapshot.data);

        for (Map.Entry<Transmittable<Object, Object>, Object> entry : snapshot.transmit2Value.entrySet()) {
            Transmittable<Object, Object> transmittable = entry.getKey();
            try {
                Object transmitBackup = entry.getValue();
                transmittable.restore(transmitBackup);
            } catch (Throwable t) {
                propagateIfFatal(t);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when restore for transmittable " + transmittable +
                            "(class " + transmittable.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }

        callback.afterRestore(data);
    }

    private static class Snapshot implements Capture, Backup {
        final HashMap<Transmittable<Object, Object>, Object> transmit2Value;
        final Object data;

        Snapshot(HashMap<Transmittable<Object, Object>, Object> transmit2Value, Object data) {
            this.transmit2Value = transmit2Value;
            this.data = data;
        }
    }


    /**
     * Register the Transmittable.
     *
     * @param <C> the Transmittable capture data type
     * @param <B> the Transmittable backup data type
     * @return true if the input Transmittable is not registered
     * @see #unregisterTransmittable(Transmittable)
     */
    @SuppressWarnings("unchecked")
    public <C, B> boolean registerTransmittable(@NonNull Transmittable<C, B> transmittable) {
        return registeredTransmittableSet.add((Transmittable<Object, Object>) transmittable);
    }

    /**
     * Unregister the Transmittable.
     *
     * @param <C> the Transmittable capture data type
     * @param <B> the Transmittable backup data type
     * @return true if the input transmittable is registered
     * @see #registerTransmittable(Transmittable)
     */
    @SuppressWarnings("unchecked")
    public <C, B> boolean unregisterTransmittable(@NonNull Transmittable<C, B> transmittable) {
        return registeredTransmittableSet.remove((Transmittable<Object, Object>) transmittable);
    }
}
