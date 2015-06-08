package org.project.neutrino.nfvo.core.cli;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.BannerProvider;
import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.shell.plugin.PromptProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

/**
 * Configures the Spring-Shell library (by override) with individual openbaton start messages and logs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpenBatonConfigurationProvider implements BannerProvider, HistoryFileNameProvider, PromptProvider {

	/**
	 * Gets the banner
	 * 
	 * @return the banner as string
	 */
	@Override
	public String getBanner() {
		StringBuffer buf = new StringBuffer();
		buf.append("=======================================" + OsUtils.LINE_SEPARATOR);
		buf.append("*                                     *"+ OsUtils.LINE_SEPARATOR);
		buf.append("*             OpenBaton               *" +OsUtils.LINE_SEPARATOR);
		buf.append("*                                     *"+ OsUtils.LINE_SEPARATOR);
		buf.append("=======================================" + OsUtils.LINE_SEPARATOR);
		buf.append("Version:" + this.getVersion());
		return buf.toString();
	}

	/**
	 * Provides the version of the cli
	 * 
	 * @return the version of the cli as string
	 */
	@Override
	public String getVersion() {
		return "0.1";
	}

	/**
	 * Provides the welcome message of the cli
	 * 
	 * @return the welcome message of the cli as string
	 */
	@Override
	public String getWelcomeMessage() {
		return "Welcome to OpenBaton CLI";
	}
	
	/**
	 * Provides the history filename of the cli
	 * 
	 * @return the history filename of the cli as string
	 */
	@Override
	public String getHistoryFileName() {
		return "openbaton-shell.log";
	}
	
	/**
	 * Provides the prompt message of the cli
	 * 
	 * @return the prompt message of the cli as string
	 */
	@Override
	public String getPrompt() {
		return System.getProperty("user.name") + "@" + "openbaton-shell>";
	}

	/**
	 * Provides the provider name for this configuration
	 * 
	 * @return the provider name for this configuration as string
	 */
	@Override
	public String getProviderName() {
		return "OpenBaton Configuration provider";
	}
}