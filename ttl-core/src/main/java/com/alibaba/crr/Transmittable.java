package com.alibaba.crr;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Transmittance process is represented by methods {@link #capture()} =&gt;
 * {@link #replay(Object)} =&gt; {@link #restore(Object)} (aka {@code CRR} operations).
 *
 * @param <C> the capture data type of transmittance
 * @param <B> the backup data type of transmittance
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public interface Transmittable<C, B> {
    /**
     * Capture.
     * <p>
     * <B><I>NOTE:</I></B><br>
     * do NOT return {@code null}.
     *
     * @return the capture data of transmittance
     */
    @NonNull
    C capture();

    /**
     * Replay.
     * <p>
     * <B><I>NOTE:</I></B><br>
     * do NOT return {@code null}.
     *
     * @param captured the capture data of transmittance, the return value of method {@link #capture()}
     * @return the backup data of transmittance
     */
    @NonNull
    B replay(@NonNull C captured);

    /**
     * Clear.
     * <p>
     * <B><I>NOTE:</I></B><br>
     * do NOT return {@code null}.
     * <p>
     * Semantically, the code {@code `B backup = clear();`} is same as {@code `B backup = replay(EMPTY_CAPTURE);`}.
     * <p>
     * The reason for providing this method is:
     * <ol>
     * <li>lead to more readable code</li>
     * <li>need not provide the constant {@code EMPTY_CAPTURE}.</li>
     * </ol>
     *
     * @return the backup data of transmittance
     */
    @NonNull
    B clear();

    /**
     * Restore.
     *
     * @param backup the backup data of transmittance, the return value of methods {@link #replay(Object)} or {@link #clear()}
     * @see #replay(Object)
     * @see #clear()
     */
    void restore(@NonNull B backup);
}
