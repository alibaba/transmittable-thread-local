package com.alibaba.ttl3.transmitter;

import com.alibaba.crr.CrrTransmit;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The transmittee is the extension point for other {@code ThreadLocal}s
 * which are registered by {@link Transmitter#registerTransmittee(Transmittee) registerTransmittee} method.
 * Transmittance is completed by by methods {@link #capture() capture()} =&gt;
 * {@link #replay(Object)}  replay(Object)} =&gt; {@link #restore(Object) restore(Object)} (aka {@code CRR} operations),
 *
 * @param <C> the transmittee capture data type
 * @param <B> the transmittee backup data type
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see Transmitter#registerTransmittee(Transmittee)
 * @see Transmitter#unregisterTransmittee(Transmittee)
 */
public interface Transmittee<C, B> extends CrrTransmit<C, B> {
    /**
     * Capture.
     * <p>
     * <B><I>NOTE:</I></B>
     * <ul>
     * <li>do NOT return {@code null}.</li>
     * <li>do NOT throw any exceptions, just ignored.</li>
     * </ul>
     *
     * @return the capture data of transmittee
     */
    @NonNull
    C capture();

    /**
     * Replay.
     * <p>
     * <B><I>NOTE:</I></B>
     * <ul>
     * <li>do NOT return {@code null}.</li>
     * <li>do NOT throw any exceptions, just ignored.</li>
     * </ul>
     *
     * @param captured the capture data of transmittee, the return value of method {@link #capture()}
     * @return the backup data of transmittee
     */
    @NonNull
    B replay(@NonNull C captured);

    /**
     * Clear.
     * <p>
     * <B><I>NOTE:</I></B>
     * <ul>
     * <li>do NOT return {@code null}.</li>
     * <li>do NOT throw any exceptions, just ignored.</li>
     * </ul>
     * <p>
     * Semantically, the code {@code `B backup = clear();`} is same as {@code `B backup = replay(EMPTY_CAPTURE);`}.
     * <p>
     * The reason for providing this method is:
     * <ol>
     * <li>lead to more readable code</li>
     * <li>need not provide the constant {@code EMPTY_CAPTURE}.</li>
     * </ol>
     *
     * @return the backup data of transmittee
     */
    @NonNull
    B clear();

    /**
     * Restore.
     * <p>
     * <B><I>NOTE:</I></B><br>
     * do NOT throw any exceptions, just ignored.
     *
     * @param backup the backup data of transmittee, the return value of methods {@link #replay(Object)} or {@link #clear()}
     * @see #replay(Object)
     * @see #clear()
     */
    void restore(@NonNull B backup);
}
