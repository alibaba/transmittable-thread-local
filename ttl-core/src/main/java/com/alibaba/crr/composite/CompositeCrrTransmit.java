package com.alibaba.crr.composite;

import com.alibaba.crr.CrrTransmit;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CompositeCrrTransmit transmit all {@link CrrTransmit}
 * registered by {@link #registerCrrTransmit(CrrTransmit)}.
 * <p>
 * Transmittance is completed by methods {@link #capture()} =&gt;
 * {@link #replay(Capture)} =&gt; {@link #restore(Backup)} (aka {@code CRR} operations).
 * <p>
 * <B><I>CAUTION:</I></B><br>
 * This implementation just ignore all exception thrown by
 * {@code CRR} operations of registered {@link CrrTransmit}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class CompositeCrrTransmit implements CrrTransmit<Capture, Backup> {
    private static final Logger logger = Logger.getLogger(CompositeCrrTransmit.class.getName());

    private static final Set<CrrTransmit<Object, Object>> registeredCrrTransmitSet = new CopyOnWriteArraySet<>();

    /**
     * Capture all {@link CrrTransmit}.
     *
     * @return the captured values
     */
    @NonNull
    public Capture capture() {
        final HashMap<CrrTransmit<Object, Object>, Object> crrTransmit2Value = new HashMap<>(registeredCrrTransmitSet.size());
        for (CrrTransmit<Object, Object> crrTransmit : registeredCrrTransmitSet) {
            try {
                crrTransmit2Value.put(crrTransmit, crrTransmit.capture());
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when capture for crrTransmit " + crrTransmit +
                            "(class " + crrTransmit.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return new Snapshot(crrTransmit2Value);
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
        final Snapshot capturedSnapshot = (Snapshot) captured;

        final HashMap<CrrTransmit<Object, Object>, Object> crrTransmit2Value = new HashMap<>(capturedSnapshot.crrTransmit2Value.size());
        for (Map.Entry<CrrTransmit<Object, Object>, Object> entry : capturedSnapshot.crrTransmit2Value.entrySet()) {
            CrrTransmit<Object, Object> crrTransmit = entry.getKey();
            try {
                Object transmitCaptured = entry.getValue();
                crrTransmit2Value.put(crrTransmit, crrTransmit.replay(transmitCaptured));
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when replay for crrTransmit " + crrTransmit +
                            "(class " + crrTransmit.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return new Snapshot(crrTransmit2Value);
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
        final HashMap<CrrTransmit<Object, Object>, Object> crrTransmit2Value = new HashMap<>(registeredCrrTransmitSet.size());
        for (CrrTransmit<Object, Object> crrTransmit : registeredCrrTransmitSet) {
            try {
                crrTransmit2Value.put(crrTransmit, crrTransmit.clear());
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when clear for crrTransmit " + crrTransmit +
                            "(class " + crrTransmit.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return new Snapshot(crrTransmit2Value);
    }

    /**
     * Restore the backup values from {@link #replay(Capture)} )}/{@link #clear()}.
     *
     * @param backup the backup values from {@link #replay(Capture)} )}/{@link #clear()}
     * @see #replay(Capture)
     * @see #clear()
     */
    public void restore(@NonNull Backup backup) {
        for (Map.Entry<CrrTransmit<Object, Object>, Object> entry : ((Snapshot) backup).crrTransmit2Value.entrySet()) {
            CrrTransmit<Object, Object> crrTransmit = entry.getKey();
            try {
                Object transmitBackup = entry.getValue();
                crrTransmit.restore(transmitBackup);
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when restore for crrTransmit " + crrTransmit +
                            "(class " + crrTransmit.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
    }

    private static class Snapshot implements Capture, Backup {
        final HashMap<CrrTransmit<Object, Object>, Object> crrTransmit2Value;

        Snapshot(HashMap<CrrTransmit<Object, Object>, Object> crrTransmit2Value) {
            this.crrTransmit2Value = crrTransmit2Value;
        }
    }


    /**
     * Register the CrrTransmit.
     *
     * @param <C> the CrrTransmit capture data type
     * @param <B> the CrrTransmit backup data type
     * @return true if the input CrrTransmit is not registered
     * @see #unregisterCrrTransmit(CrrTransmit)
     */
    @SuppressWarnings("unchecked")
    public <C, B> boolean registerCrrTransmit(@NonNull CrrTransmit<C, B> crrTransmit) {
        return registeredCrrTransmitSet.add((CrrTransmit<Object, Object>) crrTransmit);
    }

    /**
     * Unregister the CrrTransmit.
     *
     * @param <C> the CrrTransmit capture data type
     * @param <B> the CrrTransmit backup data type
     * @return true if the input crrTransmit is registered
     * @see #registerCrrTransmit(CrrTransmit)
     */
    @SuppressWarnings("unchecked")
    public <C, B> boolean unregisterCrrTransmit(@NonNull CrrTransmit<C, B> crrTransmit) {
        return registeredCrrTransmitSet.remove((CrrTransmit<Object, Object>) crrTransmit);
    }
}
