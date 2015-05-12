package org.junit.contrib.java.lang.system;

import static org.junit.contrib.java.lang.system.internal.PrintStreamHandler.SYSTEM_OUT;

import org.junit.contrib.java.lang.system.internal.PrintStreamRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * The {@code SystemOutRule} intercepts the writes to
 * {@code System.out}. It is used to make assertions about the text
 * that is written to {@code System.out} or to mute {@code System.out}.
 * <h2>Assertions</h2>
 * <p>{@code SystemOutRule} may be used for verifying the text that is
 * written to {@code System.out}.
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.out.print("some text");
 *     assertEquals("some text", systemOutRule.getLog());
 *   }
 * }
 * </pre>
 * <p>You don't have to enable logging for every test. It can be enabled for
 * specific tests only.
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule();
 *
 *   &#064;Test
 *   public void testWithLogging() {
 *     systemOutRule.enableLog()
 *     System.out.print("some text");
 *     assertEquals("some text", systemOutRule.getLog());
 *   }
 *
 *   &#064;Test
 *   public void testWithoutLogging() {
 *     System.out.print("some text");
 *   }
 * }
 * </pre>
 * <p>If you want to verify parts of the output only then you can clear the log
 * during a test.
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.out.print("uninteresting things");
 *     systemOutRule.clearLog()
 *     System.out.print("interesting things");
 *     assertEquals("interesting things", systemOutRule.getLog());
 *   }
 * }
 * </pre>
 * <h2>Muting</h2>
 * <p>Usually the output of a test to {@code System.out} does not have to be
 * visible. It may even slowdown the test. {@code SystemOutRule} can
 * be used to suppress this output.
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule().mute();
 *
 *   &#064;Test
 *   public void test() {
 *     System.out.print("some text"); //will not be visible on the console
 *   }
 * }
 * </pre>
 * <p>You don't have to mute {@code System.out} for every test. It can be muted for
 * specific tests only.
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule();
 *
 *   &#064;Test
 *   public void testWithSuppressedOutput() {
 *     systemOutRule.mute()
 *     System.out.print("some text");
 *   }
 *
 *   &#064;Test
 *   public void testWithNormalOutput() {
 *     System.out.print("some text");
 *   }
 * }
 * </pre>
 * <p>In case of a failed test it is sometimes helpful to see the output. This
 * is when the method {@link #muteForSuccessfulTests()} comes into play.
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule().muteForSuccessfulTests();
 *
 *   &#064;Test
 *   public void testWithSuppressedOutput() {
 *     System.out.print("some text");
 *   }
 *
 *   &#064;Test
 *   public void testWithNormalOutput() {
 *     System.out.print("some text");
 *     fail();
 *   }
 * }
 * </pre>
 * <h2>Combine Logging and Muting</h2>
 * <p>Logging and muting can be combined. No output is actually written to
 * {@code System.out} but everything that would have been written is available
 * from the log.
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule().mute().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.out.print("some text"); //will not be visible on the console
 *     assertEquals("some text", systemOutRule.getLog()); //will succeed
 *   }
 * }
 * </pre>
 */
public class SystemOutRule implements TestRule {
	private PrintStreamRule printStreamRule = new PrintStreamRule(SYSTEM_OUT);

	/**
	 * Suppress the output to {@code System.out}.
	 * @return the rule itself.
	 */
	public SystemOutRule mute() {
		printStreamRule.mute();
		return this;
	}

	/**
	 * Suppress the output to {@code System.out} for successful tests only.
	 * The output is still written to {@code System.out} for failing tests.
	 * @return the rule itself.
	 */
	public SystemOutRule muteForSuccessfulTests() {
		printStreamRule.muteForSuccessfulTests();
		return this;
	}

	/**
	 * Clears the current log.
	 */
	public void clearLog() {
		printStreamRule.clearLog();
	}

	/**
	 * Returns the text that is written to {@code System.out} since
	 * {@link #enableLog} (respectively {@link #clearLog()} has been called.
	 *
	 * @return the text that is written to {@code System.out} since
	 * {@link #enableLog} (respectively {@link #clearLog()} has been called.
	 */
	public String getLog() {
		return printStreamRule.getLog();
	}

	/**
	 * Start logging of everything that is written to {@code System.out}.
	 * @return the rule itself.
	 */
	public SystemOutRule enableLog() {
		printStreamRule.enableLog();
		return this;
	}

	public Statement apply(Statement base, Description description) {
		return printStreamRule.apply(base, description);
	}
}
