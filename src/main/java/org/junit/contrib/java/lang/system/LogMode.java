package org.junit.contrib.java.lang.system;

/**
 * Mode of the {@link org.junit.contrib.java.lang.system.StandardErrorStreamLog}
 * and the {@link org.junit.contrib.java.lang.system.StandardOutputStreamLog}.
 * @deprecated This enum is no longer needed, because both rules have been
 * replaced with rules that don't need the enum.
 */
@Deprecated
public enum LogMode {
	/**
	 * Capture the writes to the stream. Nothing is written to the stream itself.
	 *
	 * @deprecated Please use
	 * {@link SystemErrRule#enableLog()}.{@link SystemErrRule#mute() mute()} or
	 * {@link SystemOutRule#enableLog()}.{@link SystemOutRule#mute() mute()}.
	 */
	LOG_ONLY,

	/**
	 * Record the writes while they are still written to the stream.
	 *
	 * @deprecated Please use {@link SystemErrRule#enableLog()} or
	 * {@link SystemOutRule#enableLog()}.
	 */
	LOG_AND_WRITE_TO_STREAM
}
