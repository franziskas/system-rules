package org.junit.contrib.java.lang.system;

import static java.lang.System.err;
import static java.lang.System.setErr;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

public class SystemErrRuleTest {
	private static final String ARBITRARY_TEXT = "arbitrary text";

	private final PrintStream originalErr = err;

	@After
	public void restoreSystemErr() {
		setErr(originalErr);
	}

	@Test
	public void restoresSystemErr() {
		final PrintStream otherOut = new PrintStream(new ByteArrayOutputStream());
		executeRuleWithStatement(new SystemErrRule(), new Statement() {
			@Override
			public void evaluate() throws Throwable {
				setErr(otherOut);
			}
		});
		assertThat(err, is(sameInstance(originalErr)));
	}

	@Test
	public void doesNotMuteSystemErrByDefault() {
		ByteArrayOutputStream captureErr = new ByteArrayOutputStream();
		setErr(new PrintStream(captureErr));
		SystemErrRule rule = new SystemErrRule();
		executeRuleWithStatement(rule, new WriteTextToSystemErr());
		assertThat(captureErr.toString(), is(equalTo(ARBITRARY_TEXT)));
	}

	@Test
	public void doesNotWriteToSystemErrIfMutedGlobally() {
		SystemErrRule rule = new SystemErrRule().mute();
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setErr(new PrintStream(captureOutputStream));
		executeRuleWithStatement(rule, new WriteTextToSystemErr());
		assertThat(captureOutputStream, hasToString(isEmptyString()));
	}

	@Test
	public void doesNotWriteToSystemErrIfMutedLocaly() {
		final SystemErrRule rule = new SystemErrRule();
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setErr(new PrintStream(captureOutputStream));
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				rule.mute();
				err.print(ARBITRARY_TEXT);
			}
		});
		assertThat(captureOutputStream, hasToString(isEmptyString()));
	}

	@Test
	public void doesNotWriteToSystemErrForSuccessfulTestIfMutedGloballyForSuccessfulTests() {
		SystemErrRule rule = new SystemErrRule().muteForSuccessfulTests();
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setErr(new PrintStream(captureOutputStream));
		executeRuleWithStatement(rule, new WriteTextToSystemErr());
		assertThat(captureOutputStream, hasToString(isEmptyString()));
	}

	@Test
	public void writesToSystemErrForFailingTestIfMutedGloballyForSuccessfulTests() {
		SystemErrRule rule = new SystemErrRule().muteForSuccessfulTests();
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setErr(new PrintStream(captureOutputStream));
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				err.print(ARBITRARY_TEXT);
				fail();
			}
		});
		assertThat(captureOutputStream, hasToString(ARBITRARY_TEXT));
	}

	@Test
	public void doesNotWriteToSystemErrForSuccessfulTestIfMutedLocallyForSuccessfulTests() {
		final SystemErrRule rule = new SystemErrRule();
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setErr(new PrintStream(captureOutputStream));
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				rule.muteForSuccessfulTests();
				err.print(ARBITRARY_TEXT);
			}
		});
		assertThat(captureOutputStream, hasToString(isEmptyString()));
	}

	@Test
	public void writesToSystemErrForFailingTestIfMutedLocallyForSuccessfulTests() {
		final SystemErrRule rule = new SystemErrRule();
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setErr(new PrintStream(captureOutputStream));
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				rule.muteForSuccessfulTests();
				err.print(ARBITRARY_TEXT);
				fail();
			}
		});
		assertThat(captureOutputStream, hasToString(ARBITRARY_TEXT));
	}

	@Test
	public void doesNotLogByDefault() {
		SystemErrRule rule = new SystemErrRule();
		executeRuleWithStatement(rule, new WriteTextToSystemErr());
		assertThat(rule.getLog(), isEmptyString());
	}

	@Test
	public void logsIfEnabledGlobally() {
		SystemErrRule rule = new SystemErrRule().enableLog();
		executeRuleWithStatement(rule, new WriteTextToSystemErr());
		assertThat(rule.getLog(), is(equalTo(ARBITRARY_TEXT)));
	}

	@Test
	public void logsIfEnabledLocally() {
		final SystemErrRule rule = new SystemErrRule();
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				rule.enableLog();
				err.print(ARBITRARY_TEXT);
			}
		});
		assertThat(rule.getLog(), is(equalTo(ARBITRARY_TEXT)));
	}

	@Test
	public void collectsLogAfterClearing() {
		final SystemErrRule rule = new SystemErrRule().enableLog();
		executeRuleWithStatement(rule, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				err.print(ARBITRARY_TEXT);
				rule.clearLog();
				err.print(ARBITRARY_TEXT);
			}
		});
		assertThat(rule.getLog(), is(equalTo(ARBITRARY_TEXT)));
	}

	@Test
	public void logsIfMuted() {
		SystemErrRule rule = new SystemErrRule().enableLog().mute();
		executeRuleWithStatement(rule, new WriteTextToSystemErr());
		assertThat(rule.getLog(), is(equalTo(ARBITRARY_TEXT)));
	}

	private void executeRuleWithStatement(TestRule rule, Statement statement) {
		try {
			rule.apply(statement, null).evaluate();
		} catch (Throwable ignored) {
		}
	}

	private class WriteTextToSystemErr extends Statement {
		@Override
		public void evaluate() throws Throwable {
			err.print(ARBITRARY_TEXT);
		}
	}
}
