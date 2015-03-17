package org.junit.contrib.java.lang.system.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.apache.commons.io.IOUtils.write;

public class PrintStreamRule implements TestRule {
	private final PrintStreamHandler printStreamHandler;
	private final MutableLogStream mutableLogStream;

	public PrintStreamRule(PrintStreamHandler printStreamHandler) {
		this.printStreamHandler = printStreamHandler;
		try {
			this.mutableLogStream = new MutableLogStream(printStreamHandler.getStream());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void enableLog() {
		mutableLogStream.enableLog();
	}

	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					printStreamHandler.createRestoreStatement(new Statement() {
						@Override
						public void evaluate() throws Throwable {
							printStreamHandler.replaceCurrentStreamWithStream(mutableLogStream);
							base.evaluate();
						}
					}).evaluate();
				} catch(Throwable e) {
					write(mutableLogStream.getFailureLog(), printStreamHandler.getStream());
					throw e;
				}
			}
		};
	}

	public String getLog() {
		return mutableLogStream.getLog();
	}

	public void mute() {
		mutableLogStream.mute();
	}

	public void muteForSuccessfulTests() {
		mute();
		mutableLogStream.enableFailureLog();
	}

	public void clearLog() {
		mutableLogStream.clearLog();
	}

	private static class MutableLogStream extends PrintStream {
		private static final boolean AUTO_FLUSH = true;
		private static final String ENCODING = "UTF-8";
		private final ByteArrayOutputStream failureLog;
		private final ByteArrayOutputStream log;
		private final MutableOutputStream mutableOriginalStream;
		private final MutableOutputStream mutableFailureLog;
		private final MutableOutputStream mutableLog;

		MutableLogStream(OutputStream out) throws UnsupportedEncodingException {
			this(out, new ByteArrayOutputStream(), new ByteArrayOutputStream());
		}

		MutableLogStream(OutputStream out, ByteArrayOutputStream failureLog, ByteArrayOutputStream log)
				throws UnsupportedEncodingException {
			this(new MutableOutputStream(out),
				failureLog, new MutableOutputStream(failureLog),
				log, new MutableOutputStream(log));
		}

		MutableLogStream(MutableOutputStream mutableOriginalStream,
				ByteArrayOutputStream failureLog, MutableOutputStream mutableFailureLog,
				ByteArrayOutputStream log, MutableOutputStream mutableLog)
				throws UnsupportedEncodingException {
			super(new TeeOutputStream(
					new PrintStream(mutableOriginalStream),
					new TeeOutputStream(
						new PrintStream(mutableFailureLog), new PrintStream(mutableLog))),
				!AUTO_FLUSH, ENCODING);
			this.failureLog = failureLog;
			this.mutableFailureLog = mutableFailureLog;
			this.log = log;
			this.mutableOriginalStream = mutableOriginalStream;
			this.mutableLog = mutableLog;
			this.mutableLog.mute();
		}

		void mute() {
			mutableOriginalStream.mute();
		}

		void clearLog() {
			log.reset();
		}

		void enableLog() {
			mutableLog.turnOutputOn();
		}

		String getLog() {
			try {
				return log.toString(ENCODING);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		void enableFailureLog() {
			mutableFailureLog.turnOutputOn();
		}

		String getFailureLog() {
			try {
				return failureLog.toString(ENCODING);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class MutableOutputStream extends OutputStream {
		private final OutputStream originalStream;
		private boolean muted = false;

		MutableOutputStream(OutputStream originalStream) {
			this.originalStream = originalStream;
		}

		void mute() {
			muted = true;
		}

		void turnOutputOn() {
			muted = false;
		}

		@Override
		public void write(int b) throws IOException {
			if (!muted)
				originalStream.write(b);
		}
	}
}
